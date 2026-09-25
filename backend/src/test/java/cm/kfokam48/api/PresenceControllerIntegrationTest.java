package cm.kfokam48.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PresenceControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

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
}