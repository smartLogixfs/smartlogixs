package cl.smartlogix.user.controller;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validación fallida");
        pd.setDetail("Uno o más campos no cumplen las restricciones");
        pd.setProperty("errors", errors);
        pd.setProperty("timestamp", OffsetDateTime.now());
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleResponseStatus(ResponseStatusException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(ex.getStatusCode());
        pd.setDetail(ex.getReason());
        pd.setProperty("timestamp", OffsetDateTime.now());
        return ResponseEntity.status(ex.getStatusCode()).body(pd);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Solicitud inválida");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", OffsetDateTime.now());
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ProblemDetail> handleRuntime(RuntimeException ex) {
        // UserService usa RuntimeException para "no encontrado", "ya registrado", etc.
        // Heurística sobre el mensaje para diferenciar 404 vs 409 vs 400.
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        HttpStatus status;
        String title;
        if (msg.contains("no encontrado") || msg.contains("no existe") || msg.contains("not found")) {
            status = HttpStatus.NOT_FOUND;
            title = "Recurso no encontrado";
        } else if (msg.contains("ya está registrado") || msg.contains("ya registrado") || msg.contains("duplicad")) {
            status = HttpStatus.CONFLICT;
            title = "Conflicto";
        } else {
            status = HttpStatus.BAD_REQUEST;
            title = "Error en la solicitud";
        }
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle(title);
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", OffsetDateTime.now());
        return ResponseEntity.status(status).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Error interno");
        pd.setDetail("Ocurrió un error al procesar la solicitud");
        pd.setProperty("timestamp", OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }
}
