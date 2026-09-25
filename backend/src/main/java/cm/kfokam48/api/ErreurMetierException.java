package cm.kfokam48.api;

public class ErreurMetierException extends RuntimeException {
    private final String code;
    private final int statut;

    public ErreurMetierException(String code, int statut, String message) {
        super(message);
        this.code = code;
        this.statut = statut;
    }

    public String getCode() {
        return code;
    }

    public int getStatut() {
        return statut;
    }
}