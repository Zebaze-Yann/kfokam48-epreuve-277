package cm.kfokam48.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class PresenceDtos {
    private PresenceDtos() {
    }

    public record MarquerPresenceRequest(
            @NotBlank(message = "Le code est obligatoire.") String code,
            @NotNull(message = "L'étudiant est obligatoire.") Long etudiantId) {
    }

    public record PresenceResponse(
            Long id,
            Long sessionId,
            Long etudiantId,
            String source) {
    }
}