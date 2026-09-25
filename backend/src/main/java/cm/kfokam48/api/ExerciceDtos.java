package cm.kfokam48.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ExerciceDtos {
    private ExerciceDtos() {
    }

    public record DepotRequest(
            @NotNull(message = "La session est obligatoire.") Long sessionId,
            @NotNull(message = "L'étudiant est obligatoire.") Long etudiantId,
            @NotBlank(message = "Le lien est obligatoire.") String lien) {
    }

    public record DepotResponse(Long id, String statut) {
    }
}