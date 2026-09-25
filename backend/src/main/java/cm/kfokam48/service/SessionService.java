package cm.kfokam48.service;

import cm.kfokam48.api.ErreurMetierException;
import cm.kfokam48.api.SessionDtos;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SessionService {
    private static final long DUREE_CODE_MINUTES = 15;

    private final JdbcTemplate jdbcTemplate;

    public SessionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SessionDtos.OuvertureResponse ouvrir(SessionDtos.OuvertureRequest request) {
        verifierPromotion(request.promotionId());

        OffsetDateTime ouvertureAt = OffsetDateTime.now();
        OffsetDateTime expirationAt = ouvertureAt.plusMinutes(DUREE_CODE_MINUTES);
        String code = genererCodeUnique();
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update((PreparedStatementCreator) connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO sessions(titre, promotion_id, code, ouverture_at, expiration_at) VALUES (?, ?, ?, ?, ?)",
                    new String[] { "ID" });
            statement.setString(1, request.titre());
            statement.setLong(2, request.promotionId());
            statement.setString(3, code);
            statement.setObject(4, ouvertureAt);
            statement.setObject(5, expirationAt);
            return statement;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return new SessionDtos.OuvertureResponse(id, code, ouvertureAt, expirationAt);
    }

    private void verifierPromotion(Long promotionId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM promotions WHERE id = ?", Integer.class, promotionId);
        if (count == null || count == 0) {
            throw new ErreurMetierException(
                    "PROMOTION_INCONNUE", 400, "La promotion est inconnue.");
        }
    }

    private String genererCodeUnique() {
        for (int tentative = 0; tentative < 10; tentative++) {
            String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sessions WHERE code = ?", Integer.class, code);
            if (count != null && count == 0) {
                return code;
            }
        }
        throw new ErreurMetierException(
                "CODE_INDISPONIBLE", 500, "Impossible de générer un code de présence.");
    }
}