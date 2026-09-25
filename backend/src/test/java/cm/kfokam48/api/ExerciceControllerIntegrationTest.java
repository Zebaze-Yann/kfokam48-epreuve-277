package cm.kfokam48.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExerciceControllerIntegrationTest {
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Test
        void deposeUnLienValide() throws Exception {
                String sessionResponse = mockMvc.perform(post("/api/sessions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"titre\":\"Dépôt\",\"promotionId\":1}"))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();
                JsonNode session = objectMapper.readTree(sessionResponse);

                mockMvc.perform(post("/api/exercices")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"sessionId\":" + session.get("id").asLong()
                                                + ",\"etudiantId\":1,\"lien\":\"https://github.com/kfokam48/exercice\"}"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").isNumber())
                                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"));
        }

        @Test
        void refuseUnLienInvalide() throws Exception {
                mockMvc.perform(post("/api/exercices")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"sessionId\":1,\"etudiantId\":2,\"lien\":\"pas-un-lien\"}"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("LIEN_INVALIDE"));
        }

        @Test
        void assigneUnRelecteurPresentEtDifferentDeAuteur() throws Exception {
                String sessionResponse = mockMvc.perform(post("/api/sessions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"titre\":\"Relecture\",\"promotionId\":1}"))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();
                long sessionId = objectMapper.readTree(sessionResponse).get("id").asLong();

                jdbcTemplate.update(
                                "INSERT INTO presences(session_id, etudiant_id, source, marquee_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
                                sessionId, 1, "ETUDIANT");
                jdbcTemplate.update(
                                "INSERT INTO presences(session_id, etudiant_id, source, marquee_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
                                sessionId, 2, "ETUDIANT");

                String exerciseResponse = mockMvc.perform(post("/api/exercices")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"sessionId\":" + sessionId
                                                + ",\"etudiantId\":1,\"lien\":\"https://github.com/kfokam48/relecture\"}"))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();
                long exerciseId = objectMapper.readTree(exerciseResponse).get("id").asLong();

                Long relecteurId = jdbcTemplate.queryForObject(
                                "SELECT relecteur_id FROM relectures WHERE exercice_id = ?",
                                Long.class, exerciseId);
                org.assertj.core.api.Assertions.assertThat(relecteurId)
                                .isEqualTo(2L)
                                .isNotEqualTo(1L);
        }
}