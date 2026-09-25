package cm.kfokam48.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public final class SessionDtos {
    private SessionDtos() {
    }

    public record OuvertureRequest(
            @NotBlank(message = "Le titre est obligatoire.") String titre,
            @NotNull(message = "La promotion est obligatoire.") Long promotionId) {
    }

    public record OuvertureResponse(
            Long id,
            String code,
            OffsetDateTime ouvertureAt,
            OffsetDateTime expirationAt) {
    }
}