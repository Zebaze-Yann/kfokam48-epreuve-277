package cm.kfokam48.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.PreparedStatement;
import java.time.OffsetDateTime;
import java.util.Objects;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NoteControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void exposeNoteEtCommentaireSansIdentiteRelecteur() throws Exception {
        String sessionJson = mockMvc.perform(post("/api/sessions")
                .contentType(APPLICATION_JSON)
                .content("{\"titre\":\"Note publiée\",\"promotionId\":1}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long sessionId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(sessionJson).get("id").asLong();

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO exercices(session_id, etudiant_id, lien, statut, depose_at) "
                            + "VALUES (?, ?, ?, ?, ?)",
                    new String[] { "ID" });
            statement.setLong(1, sessionId);
            statement.setLong(2, 1);
            statement.setString(3, "https://example.com/note");
            statement.setString(4, "RELU");
            statement.setObject(5, OffsetDateTime.now());
            return statement;
        }, keyHolder);
        Long exerciceId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        jdbcTemplate.update(
                "INSERT INTO relectures(exercice_id, relecteur_id, note, commentaire, rendue_at) "
                        + "VALUES (?, 2, 17, 'Travail solide.', ?)",
                exerciceId, OffsetDateTime.now());

        mockMvc.perform(get("/api/exercices/" + exerciceId + "/note"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value(17))
                .andExpect(jsonPath("$.commentaire").value("Travail solide."))
                .andExpect(jsonPath("$.relecteurId").doesNotExist());
    }
}