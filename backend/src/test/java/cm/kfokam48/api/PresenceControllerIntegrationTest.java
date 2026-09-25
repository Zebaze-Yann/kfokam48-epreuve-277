package cm.kfokam48.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PresenceControllerIntegrationTest {
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Test
        void marquePresenceAvecCodeValide() throws Exception {
                String session = mockMvc.perform(post("/api/sessions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"titre\":\"Présence\",\"promotionId\":1}"))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();
                String code = session.replaceAll(".*\\\"code\\\":\\\"([^\\\"]+)\\\".*", "$1");

                mockMvc.perform(post("/api/presences")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"code\":\"" + code + "\",\"etudiantId\":1}"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").isNumber())
                                .andExpect(jsonPath("$.sessionId").isNumber())
                                .andExpect(jsonPath("$.etudiantId").value(1))
                                .andExpect(jsonPath("$.source").value("ETUDIANT"));
        }

        @Test
        void refuseUneDeuxiemePresenceAvec409() throws Exception {
                String session = mockMvc.perform(post("/api/sessions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"titre\":\"Présence doublon\",\"promotionId\":1}"))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();
                String code = session.replaceAll(".*\\\"code\\\":\\\"([^\\\"]+)\\\".*", "$1");
                String body = "{\"code\":\"" + code + "\",\"etudiantId\":2}";

                mockMvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON).content(body))
                                .andExpect(status().isCreated());
                mockMvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON).content(body))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code").value("DEJA_PRESENT"));
        }

        @Test
        void refuseUnCodeExpireAvec410() throws Exception {
                String code = "E" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
                OffsetDateTime ouvertureAt = OffsetDateTime.now().minusMinutes(16);
                OffsetDateTime expirationAt = OffsetDateTime.now().minusMinutes(1);
                jdbcTemplate.update(
                                "INSERT INTO sessions(titre, promotion_id, code, ouverture_at, expiration_at) VALUES (?, ?, ?, ?, ?)",
                                "Session expirée", 1, code, ouvertureAt, expirationAt);

                mockMvc.perform(post("/api/presences")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"code\":\"" + code + "\",\"etudiantId\":3}"))
                                .andExpect(status().isGone())
                                .andExpect(jsonPath("$.code").value("CODE_EXPIRE"))
                                .andExpect(jsonPath("$.message").value("Le code de présence a expiré."));
        }
}