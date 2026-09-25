package cm.kfokam48.api;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {}
    public record SessionRequest(@NotBlank String titre, @NotNull Long promotionId) {}
    public record SessionResponse(Long id, String code, OffsetDateTime ouvertureAt, OffsetDateTime expirationAt) {}
    public record PresenceRequest(@NotBlank String code, @NotNull Long etudiantId) {}
    public record PresenceResponse(Long id, Long sessionId, Long etudiantId, String source) {}
    public record ExerciseRequest(@NotNull Long sessionId, @NotNull Long etudiantId, @NotBlank String lien) {}
    public record ExerciseResponse(Long id, String statut) {}
    public record ReviewRequest(@NotNull @Min(0) @Max(20) Integer note, @NotBlank String commentaire) {}
    public record ReviewResponse(Integer note, String commentaire) {}
    public record BoardRow(Long etudiantId, String nom, int presences, int exercicesDeposes, Double moyenne, int relecturesEnAttente) {}
    public record AssignedReview(Long id, Long exerciceId, String lien, String auteur) {}
    public record ErrorResponse(String code, String message) {}
}