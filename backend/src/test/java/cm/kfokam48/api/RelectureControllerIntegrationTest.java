package cm.kfokam48.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.PreparedStatement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RelectureControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void rendUneNoteEtPasseExerciceARelu() throws Exception {
        long exerciceId = creerExercice(1);
        long relectureId = creerRelecture(exerciceId, 2);

        mockMvc.perform(post("/api/relectures/" + relectureId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"note\":18,\"commentaire\":\"Très bon travail.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value(18))
                .andExpect(jsonPath("$.commentaire").value("Très bon travail."));

        String statut = jdbcTemplate.queryForObject(
                "SELECT statut FROM exercices WHERE id = ?", String.class, exerciceId);
        assertThat(statut).isEqualTo("RELU");
    }

    @Test
    void refuseUneNoteHorsLimites() throws Exception {
        long exerciceId = creerExercice(1);
        long relectureId = creerRelecture(exerciceId, 2);

        mockMvc.perform(post("/api/relectures/" + relectureId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"note\":21,\"commentaire\":\"Impossible\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DONNEES_INVALIDES"));
    }

    @Test
    void refuseAutoRelectureEtRelectureDejaRendue() throws Exception {
        long exerciceId = creerExercice(1);
        long autoRelectureId = creerRelecture(exerciceId, 1);

        mockMvc.perform(post("/api/relectures/" + autoRelectureId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"note\":10,\"commentaire\":\"Auto\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTO_RELECTURE"));

        long rendueId = creerRelecture(creerExercice(2), 3);
        jdbcTemplate.update(
                "UPDATE relectures SET note = ?, commentaire = ?, rendue_at = CURRENT_TIMESTAMP WHERE id = ?",
                12, "Déjà rendue", rendueId);
        mockMvc.perform(post("/api/relectures/" + rendueId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"note\":13,\"commentaire\":\"Nouvelle note\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RELECTURE_DEJA_RENDUE"));
    }

    private long creerExercice(long etudiantId) throws Exception {
        String sessionJson = mockMvc.perform(post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titre\":\"Notation\",\"promotionId\":1}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long sessionId = objectMapper.readTree(sessionJson).get("id").asLong();
        String exerciseJson = mockMvc.perform(post("/api/exercices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":" + sessionId
                        + ",\"etudiantId\":" + etudiantId
                        + ",\"lien\":\"https://github.com/kfokam48/notation-"
                        + sessionId + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(exerciseJson).get("id").asLong();
    }

    private long creerRelecture(long exerciceId, long relecteurId) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO relectures(exercice_id, relecteur_id) VALUES (?, ?)",
                    new String[] { "ID" });
            statement.setLong(1, exerciceId);
            statement.setLong(2, relecteurId);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}