package cm.kfokam48.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GestionnaireErreurs {
    @ExceptionHandler(ErreurMetierException.class)
    public ResponseEntity<ErreurResponse> gererErreurMetier(ErreurMetierException exception) {
        return ResponseEntity.status(exception.getStatut())
                .body(new ErreurResponse(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurResponse> gererValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .orElse("Les données envoyées sont invalides.");
        return ResponseEntity.badRequest()
                .body(new ErreurResponse("DONNEES_INVALIDES", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurResponse> gererErreurInattendue(Exception exception) {
        return ResponseEntity.internalServerError()
                .body(new ErreurResponse("ERREUR_INTERNE", "Une erreur interne est survenue."));
    }
}