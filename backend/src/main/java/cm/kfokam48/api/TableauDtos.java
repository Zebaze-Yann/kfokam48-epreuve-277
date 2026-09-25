package cm.kfokam48.api;

public final class TableauDtos {
    private TableauDtos() {
    }

    public record LigneTableau(
            Long etudiantId,
            String nom,
            int presences,
            int exercicesDeposes,
            Double moyenne,
            int relecturesEnAttente) {
    }
}