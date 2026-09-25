package cm.kfokam48.service;

import cm.kfokam48.api.ErreurMetierException;
import cm.kfokam48.api.TableauDtos;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TableauService {
    private final JdbcTemplate jdbcTemplate;

    public TableauService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TableauDtos.LigneTableau> consulter(Long promotionId) {
        verifierPromotion(promotionId);

        String sql = """
                SELECT
                    e.id AS etudiant_id,
                    e.nom AS nom,
                    (SELECT COUNT(*) FROM presences p
                        JOIN sessions s ON s.id = p.session_id
                        WHERE p.etudiant_id = e.id AND s.promotion_id = e.promotion_id) AS presences,
                    (SELECT COUNT(*) FROM exercices ex
                        JOIN sessions s2 ON s2.id = ex.session_id
                        WHERE ex.etudiant_id = e.id AND s2.promotion_id = e.promotion_id) AS exercices_deposes,
                    (SELECT AVG(r.note) FROM relectures r
                        JOIN exercices ex2 ON ex2.id = r.exercice_id
                        WHERE ex2.etudiant_id = e.id AND r.note IS NOT NULL) AS moyenne,
                    (SELECT COUNT(*) FROM relectures r2
                        WHERE r2.relecteur_id = e.id AND r2.note IS NULL) AS relectures_en_attente
                FROM etudiants e
                WHERE e.promotion_id = ?
                ORDER BY e.nom
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNum) -> new TableauDtos.LigneTableau(
                resultSet.getLong("etudiant_id"),
                resultSet.getString("nom"),
                resultSet.getInt("presences"),
                resultSet.getInt("exercices_deposes"),
                resultSet.getObject("moyenne") != null ? resultSet.getDouble("moyenne") : null,
                resultSet.getInt("relectures_en_attente")), promotionId);
    }

    private void verifierPromotion(Long promotionId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM promotions WHERE id = ?", Integer.class, promotionId);
        if (count == null || count == 0) {
            throw new ErreurMetierException(
                    "PROMOTION_INCONNUE", 404, "La promotion est inconnue.");
        }
    }
}