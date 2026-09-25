package cm.kfokam48.service;

import cm.kfokam48.api.ApiDtos;
import cm.kfokam48.api.ApiException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class CourseService {
    private final JdbcTemplate db;

    public CourseService(JdbcTemplate db) {
        this.db = db;
    }

    public ApiDtos.SessionResponse open(ApiDtos.SessionRequest r) {
        requirePromotion(r.promotionId());
        var now = OffsetDateTime.now();
        var expiration = now.plusMinutes(15);
        var code = String.valueOf((int) (100000 + Math.random() * 900000));
        var keys = new GeneratedKeyHolder();
        db.update((PreparedStatementCreator) connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO sessions(titre,promotion_id,code,ouverture_at,expiration_at) VALUES(?,?,?,?,?)",
                    new String[] { "ID" });
            statement.setString(1, r.titre());
            statement.setLong(2, r.promotionId());
            statement.setString(3, code);
            statement.setObject(4, now);
            statement.setObject(5, expiration);
            return statement;
        }, keys);
        long id = Objects.requireNonNull(keys.getKey()).longValue();
        return new ApiDtos.SessionResponse(id, code, now, expiration);
    }

    public ApiDtos.PresenceResponse presence(ApiDtos.PresenceRequest r) {
        var session = db.query("SELECT id, expiration_at, cloture_at FROM sessions WHERE code=?",
                rs -> rs.next()
                        ? new Object[] { rs.getLong(1), rs.getObject(2, OffsetDateTime.class),
                                rs.getObject(3, OffsetDateTime.class) }
                        : null,
                r.code());
        if (session == null)
            throw new ApiException("CODE_INCONNU", 400, "Le code de présence est inconnu.");
        if (session[2] != null || !OffsetDateTime.now().isBefore((OffsetDateTime) session[1]))
            throw new ApiException("CODE_EXPIRE", 410, "Le code de présence a expiré.");
        if (db.queryForObject("SELECT COUNT(*) FROM etudiants WHERE id=?", Integer.class, r.etudiantId()) == 0)
            throw new ApiException("ETUDIANT_INCONNU", 400, "L'étudiant est inconnu.");
        try {
            long id = db.queryForObject(
                    "INSERT INTO presences(session_id,etudiant_id,source,marquee_at) VALUES(?,?,?,?)", Long.class,
                    session[0], r.etudiantId(), "ETUDIANT", OffsetDateTime.now());
            return new ApiDtos.PresenceResponse(id, (Long) session[0], r.etudiantId(), "ETUDIANT");
        } catch (Exception e) {
            throw new ApiException("DEJA_PRESENT", 409, "L'étudiant est déjà présent.");
        }
    }

    public ApiDtos.ExerciseResponse exercise(ApiDtos.ExerciseRequest r) {
        try {
            URI.create(r.lien());
        } catch (Exception e) {
            throw new ApiException("LIEN_INVALIDE", 400, "Le lien de l'exercice est invalide.");
        }
        var sessionExists = db.queryForObject("SELECT COUNT(*) FROM sessions WHERE id=?", Integer.class,
                r.sessionId()) > 0;
        if (!sessionExists)
            throw new ApiException("SESSION_INCONNUE", 400, "La session est inconnue.");
        var closed = db.queryForObject("SELECT cloture_at IS NOT NULL FROM sessions WHERE id=?", Boolean.class,
                r.sessionId());
        if (Boolean.TRUE.equals(closed))
            throw new ApiException("SESSION_CLOTUREE", 409, "La session est clôturée.");
        try {
            var keys = new GeneratedKeyHolder();
            db.update((PreparedStatementCreator) connection -> {
                var statement = connection.prepareStatement(
                        "INSERT INTO exercices(session_id,etudiant_id,lien,statut,depose_at) VALUES(?,?,?,?,?)",
                        new String[] { "ID" });
                statement.setLong(1, r.sessionId());
                statement.setLong(2, r.etudiantId());
                statement.setString(3, r.lien());
                statement.setString(4, "EN_ATTENTE");
                statement.setObject(5, OffsetDateTime.now());
                return statement;
            }, keys);
            long id = Objects.requireNonNull(keys.getKey()).longValue();
            assignReviewer(id, r.sessionId(), r.etudiantId());
            return new ApiDtos.ExerciseResponse(id, "EN_ATTENTE");
        } catch (Exception e) {
            throw new ApiException("EXERCICE_DEJA_DEPOSE", 409, "Un exercice existe déjà pour cet étudiant.");
        }
    }

    private void assignReviewer(long exerciseId, long sessionId, long authorId) {
        var ids = db.queryForList("SELECT etudiant_id FROM presences WHERE session_id=? AND etudiant_id<>?", Long.class,
                sessionId, authorId);
        if (!ids.isEmpty())
            db.update("INSERT INTO relectures(exercice_id,relecteur_id) VALUES(?,?)", exerciseId,
                    ids.get(new Random().nextInt(ids.size())));
    }

    public ApiDtos.ReviewResponse review(long id, ApiDtos.ReviewRequest r, boolean update) {
        var row = db.query("SELECT note, commentaire FROM relectures WHERE id=?",
                rs -> rs.next() ? new Object[] { rs.getInt(1), rs.getString(2) } : null, id);
        if (row == null)
            throw new ApiException("RELECTURE_INCONNUE", 404, "La relecture est inconnue.");
        db.update("UPDATE relectures SET note=?,commentaire=?,rendue_at=? WHERE id=?", r.note(), r.commentaire(),
                OffsetDateTime.now(), id);
        db.update("UPDATE exercices SET statut='RELU' WHERE id=(SELECT exercice_id FROM relectures WHERE id=?)", id);
        return new ApiDtos.ReviewResponse(r.note(), r.commentaire());
    }

    public List<ApiDtos.AssignedReview> assigned(long studentId) {
        return db.query(
                "SELECT r.id,e.id,e.lien,a.nom FROM relectures r JOIN exercices e ON e.id=r.exercice_id JOIN etudiants a ON a.id=e.etudiant_id WHERE r.relecteur_id=? AND r.rendue_at IS NULL",
                (rs, n) -> new ApiDtos.AssignedReview(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getString(4)),
                studentId);
    }

    public ApiDtos.ReviewResponse note(long exerciseId) {
        return db.query("SELECT note,commentaire FROM relectures WHERE exercice_id=? AND rendue_at IS NOT NULL",
                rs -> rs.next() ? new ApiDtos.ReviewResponse(rs.getInt(1), rs.getString(2)) : null, exerciseId);
    }

    public List<ApiDtos.BoardRow> board(long promotionId) {
        requirePromotion(promotionId);
        return db.query(
                "SELECT e.id,e.nom,(SELECT COUNT(*) FROM presences p WHERE p.etudiant_id=e.id) pres,(SELECT COUNT(*) FROM exercices x WHERE x.etudiant_id=e.id) ex,(SELECT AVG(r.note) FROM relectures r JOIN exercices x ON x.id=r.exercice_id WHERE x.etudiant_id=e.id AND r.rendue_at IS NOT NULL),(SELECT COUNT(*) FROM relectures r WHERE r.relecteur_id=e.id AND r.rendue_at IS NULL) pending FROM etudiants e WHERE e.promotion_id=?",
                (rs, n) -> new ApiDtos.BoardRow(rs.getLong(1), rs.getString(2), rs.getInt(3), rs.getInt(4),
                        rs.getObject(5, Double.class), rs.getInt(6)),
                promotionId);
    }

    private void requirePromotion(long id) {
        if (db.queryForObject("SELECT COUNT(*) FROM promotions WHERE id=?", Integer.class, id) == 0)
            throw new ApiException("PROMOTION_INCONNUE", 404, "La promotion est inconnue.");
    }
}