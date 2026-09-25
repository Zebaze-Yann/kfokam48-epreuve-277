package cm.kfokam48.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SessionControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void ouvrirSessionRetourneCodeEtExpirationAQuinzeMinutes() throws Exception {
        String response = mockMvc.perform(post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titre\":\"Java avancé\",\"promotionId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.matchesPattern("[0-9]{6}")))
                .andExpect(jsonPath("$.ouvertureAt").isString())
                .andExpect(jsonPath("$.expirationAt").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var ouverture = OffsetDateTime.parse(com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(response).get("ouvertureAt").asText());
        var expiration = OffsetDateTime.parse(com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(response).get("expirationAt").asText());
        org.junit.jupiter.api.Assertions.assertEquals(Duration.ofMinutes(15), Duration.between(ouverture, expiration));
    }

    @Test
    void ouvrirSessionRefuseTitreManquantAvecErreurStandard() throws Exception {
        mockMvc.perform(post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"promotionId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DONNEES_INVALIDES"))
                .andExpect(jsonPath("$.message").isString());
    }
}