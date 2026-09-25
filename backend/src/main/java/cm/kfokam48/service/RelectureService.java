package cm.kfokam48.service;

import cm.kfokam48.api.ErreurMetierException;
import cm.kfokam48.api.RelectureDtos;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class RelectureService {
    private final JdbcTemplate jdbcTemplate;

    public RelectureService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public RelectureDtos.NoteResponse rendre(Long relectureId, RelectureDtos.NoteRequest request) {
        Relecture relecture = trouverRelecture(relectureId);
        if (relecture == null) {
            throw new ErreurMetierException(
                    "RELECTURE_INCONNUE", 404, "La relecture est inconnue.");
        }
        if (relecture.auteurId().equals(relecture.relecteurId())) {
            throw new ErreurMetierException(
                    "AUTO_RELECTURE", 403, "Un étudiant ne peut pas relire son propre exercice.");
        }
        if (relecture.rendueAt() != null) {
            throw new ErreurMetierException(
                    "RELECTURE_DEJA_RENDUE", 409, "La relecture a déjà été rendue.");
        }

        jdbcTemplate.update(
                "UPDATE relectures SET note = ?, commentaire = ?, rendue_at = ? WHERE id = ?",
                request.note(), request.commentaire(), OffsetDateTime.now(), relectureId);
        jdbcTemplate.update(
                "UPDATE exercices SET statut = 'RELU' WHERE id = ?", relecture.exerciceId());
        return new RelectureDtos.NoteResponse(request.note(), request.commentaire());
    }

    private Relecture trouverRelecture(Long relectureId) {
        return jdbcTemplate.query(
                "SELECT r.exercice_id, r.relecteur_id, r.rendue_at, e.etudiant_id "
                        + "FROM relectures r JOIN exercices e ON e.id = r.exercice_id "
                        + "WHERE r.id = ?",
                resultSet -> resultSet.next()
                        ? new Relecture(
                                resultSet.getLong("exercice_id"),
                                resultSet.getLong("relecteur_id"),
                                resultSet.getObject("rendue_at", OffsetDateTime.class),
                                resultSet.getLong("etudiant_id"))
                        : null,
                relectureId);
    }

    private record Relecture(Long exerciceId, Long relecteurId, OffsetDateTime rendueAt, Long auteurId) {
    }
}