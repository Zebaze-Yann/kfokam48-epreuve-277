package cm.kfokam48.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class RelectureDtos {
    private RelectureDtos() {
    }

    public record NoteRequest(
            @NotNull(message = "La note est obligatoire.") @Min(value = 0, message = "La note doit être comprise entre 0 et 20.") @Max(value = 20, message = "La note doit être comprise entre 0 et 20.") Integer note,
            @NotBlank(message = "Le commentaire est obligatoire.") String commentaire) {
    }

    public record NoteResponse(Integer note, String commentaire) {
    }
}