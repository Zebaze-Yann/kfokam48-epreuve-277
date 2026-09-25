package cm.kfokam48.service;

import cm.kfokam48.api.ErreurMetierException;
import cm.kfokam48.api.ExerciceDtos;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ExerciceService {
    private static final String STATUT_EN_ATTENTE = "EN_ATTENTE";

    private final JdbcTemplate jdbcTemplate;

    public ExerciceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ExerciceDtos.DepotResponse deposer(ExerciceDtos.DepotRequest request) {
        verifierLien(request.lien());
        verifierEtudiant(request.etudiantId());
        SessionEtat session = trouverSession(request.sessionId());
        if (session == null) {
            throw new ErreurMetierException(
                    "SESSION_INCONNUE", 400, "La session est inconnue.");
        }
        if (session.cloturee()) {
            throw new ErreurMetierException(
                    "SESSION_CLOTUREE", 409, "La session est clôturée.");
        }
        if (exerciceExiste(request.sessionId(), request.etudiantId())) {
            throw new ErreurMetierException(
                    "EXERCICE_DEJA_DEPOSE", 409,
                    "L'étudiant a déjà déposé un exercice pour cette session.");
        }

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO exercices(session_id, etudiant_id, lien, statut, depose_at) VALUES (?, ?, ?, ?, ?)",
                        new String[] { "ID" });
                statement.setLong(1, request.sessionId());
                statement.setLong(2, request.etudiantId());
                statement.setString(3, request.lien());
                statement.setString(4, STATUT_EN_ATTENTE);
                statement.setObject(5, OffsetDateTime.now());
                return statement;
            }, keyHolder);
        } catch (DataIntegrityViolationException exception) {
            throw new ErreurMetierException(
                    "EXERCICE_DEJA_DEPOSE", 409,
                    "L'étudiant a déjà déposé un exercice pour cette session.");
        }

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        assignerRelecteur(id, request.sessionId(), request.etudiantId());
        return new ExerciceDtos.DepotResponse(id, STATUT_EN_ATTENTE);
    }

    private void assignerRelecteur(Long exerciceId, Long sessionId, Long auteurId) {
        List<Long> candidats = jdbcTemplate.queryForList(
                "SELECT etudiant_id FROM presences "
                        + "WHERE session_id = ? AND etudiant_id <> ?",
                Long.class, sessionId, auteurId);
        if (candidats.isEmpty()) {
            return;
        }

        Long relecteurId = candidats.get(ThreadLocalRandom.current().nextInt(candidats.size()));
        jdbcTemplate.update(
                "INSERT INTO relectures(exercice_id, relecteur_id) VALUES (?, ?)",
                exerciceId, relecteurId);
    }

    private void verifierLien(String lien) {
        try {
            URI uri = new URI(lien);
            if (!"http".equalsIgnoreCase(uri.getScheme())
                    && !"https".equalsIgnoreCase(uri.getScheme())) {
                throw new URISyntaxException(lien, "Le lien doit utiliser HTTP ou HTTPS.");
            }
            if (uri.getHost() == null) {
                throw new URISyntaxException(lien, "Le lien doit contenir un hôte.");
            }
        } catch (URISyntaxException exception) {
            throw new ErreurMetierException(
                    "LIEN_INVALIDE", 400, "Le lien de l'exercice est invalide.");
        }
    }

    private void verifierEtudiant(Long etudiantId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM etudiants WHERE id = ?", Integer.class, etudiantId);
        if (count == null || count == 0) {
            throw new ErreurMetierException(
                    "ETUDIANT_INCONNU", 400, "L'étudiant est inconnu.");
        }
    }

    private SessionEtat trouverSession(Long sessionId) {
        return jdbcTemplate.query(
                "SELECT cloture_at FROM sessions WHERE id = ?",
                resultSet -> resultSet.next()
                        ? new SessionEtat(resultSet.getObject("cloture_at") != null)
                        : null,
                sessionId);
    }

    private boolean exerciceExiste(Long sessionId, Long etudiantId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM exercices WHERE session_id = ? AND etudiant_id = ?",
                Integer.class, sessionId, etudiantId);
        return count != null && count > 0;
    }

    private record SessionEtat(boolean cloturee) {
    }
}