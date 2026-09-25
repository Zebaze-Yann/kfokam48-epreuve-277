package cm.kfokam48.service;

import cm.kfokam48.api.ErreurMetierException;
import cm.kfokam48.api.PresenceDtos;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.time.OffsetDateTime;
import java.util.Objects;

@Service
public class PresenceService {
    private final JdbcTemplate jdbcTemplate;

    public PresenceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PresenceDtos.PresenceResponse marquer(PresenceDtos.MarquerPresenceRequest request) {
        SessionCode session = trouverSession(request.code());
        if (session == null) {
            throw new ErreurMetierException(
                    "CODE_INCONNU", 400, "Le code de présence est inconnu.");
        }
        if (!OffsetDateTime.now().isBefore(session.expirationAt())) {
            throw new ErreurMetierException(
                    "CODE_EXPIRE", 410, "Le code de présence a expiré.");
        }
        verifierEtudiant(request.etudiantId());
        if (presenceExiste(session.id(), request.etudiantId())) {
            throw new ErreurMetierException(
                    "DEJA_PRESENT", 409, "L'étudiant est déjà présent.");
        }

        OffsetDateTime marqueeAt = OffsetDateTime.now();
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO presences(session_id, etudiant_id, source, marquee_at) VALUES (?, ?, ?, ?)",
                        new String[] { "ID" });
                statement.setLong(1, session.id());
                statement.setLong(2, request.etudiantId());
                statement.setString(3, "ETUDIANT");
                statement.setObject(4, marqueeAt);
                return statement;
            }, keyHolder);
        } catch (DataIntegrityViolationException exception) {
            throw new ErreurMetierException(
                    "DEJA_PRESENT", 409, "L'étudiant est déjà présent.");
        }

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return new PresenceDtos.PresenceResponse(id, session.id(), request.etudiantId(), "ETUDIANT");
    }

    private SessionCode trouverSession(String code) {
        return jdbcTemplate.query(
                "SELECT id, expiration_at FROM sessions WHERE code = ?",
                resultSet -> resultSet.next()
                        ? new SessionCode(resultSet.getLong("id"),
                                resultSet.getObject("expiration_at", OffsetDateTime.class))
                        : null,
                code);
    }

    private void verifierEtudiant(Long etudiantId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM etudiants WHERE id = ?", Integer.class, etudiantId);
        if (count == null || count == 0) {
            throw new ErreurMetierException(
                    "ETUDIANT_INCONNU", 400, "L'étudiant est inconnu.");
        }
    }

    private boolean presenceExiste(Long sessionId, Long etudiantId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM presences WHERE session_id = ? AND etudiant_id = ?",
                Integer.class, sessionId, etudiantId);
        return count != null && count > 0;
    }

    private record SessionCode(Long id, OffsetDateTime expirationAt) {
    }
}