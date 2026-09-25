package cm.kfokam48.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Objects;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiDtos.ErrorResponse> handle(ApiException e) {
        return ResponseEntity.status(e.status).body(new ApiDtos.ErrorResponse(e.code, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiDtos.ErrorResponse> validation(MethodArgumentNotValidException e) {
        String message = Objects
                .requireNonNullElse(e.getBindingResult().getFieldError(), e.getBindingResult().getGlobalError())
                .getDefaultMessage();
        return ResponseEntity.badRequest().body(new ApiDtos.ErrorResponse("DONNEES_INVALIDES", message));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiDtos.ErrorResponse> generic(Exception e) {
        return ResponseEntity.internalServerError()
                .body(new ApiDtos.ErrorResponse("ERREUR_INTERNE", "Une erreur interne est survenue."));
    }
}