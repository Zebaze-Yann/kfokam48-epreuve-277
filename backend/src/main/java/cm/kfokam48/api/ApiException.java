package cm.kfokam48.api;

public class ApiException extends RuntimeException {
    public final String code;
    public final int status;

    public ApiException(String code, int status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }
}