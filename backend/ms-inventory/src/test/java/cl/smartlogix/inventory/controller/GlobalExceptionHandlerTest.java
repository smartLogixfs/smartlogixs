package cl.smartlogix.inventory.controller;

import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void debeManejarValidationException() {

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "object");

        bindingResult.addError(
                new FieldError(
                        "object",
                        "name",
                        "name es obligatorio"
                )
        );

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(
                        null,
                        bindingResult
                );

        ResponseEntity<ProblemDetail> response =
                handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode());

        assertEquals("Validación fallida",
                response.getBody().getTitle());

        assertNotNull(
                response.getBody().getProperties().get("errors")
        );
    }

    @Test
    void debeManejarResponseStatusException() {

        ResponseStatusException ex =
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                );

        ResponseEntity<ProblemDetail> response =
                handler.handleResponseStatus(ex);

        assertEquals(HttpStatus.NOT_FOUND,
                response.getStatusCode());

        assertEquals("Producto no encontrado",
                response.getBody().getDetail());
    }

    @Test
    void debeManejarOptimisticLockException() {

        OptimisticLockingFailureException ex =
                new OptimisticLockingFailureException(
                        "conflicto"
                );

        ResponseEntity<ProblemDetail> response =
                handler.handleOptimisticLock(ex);

        assertEquals(HttpStatus.CONFLICT,
                response.getStatusCode());

        assertEquals(
                "Conflicto de concurrencia",
                response.getBody().getTitle()
        );
    }

    @Test
    void debeManejarIllegalArgumentException() {

        IllegalArgumentException ex =
                new IllegalArgumentException(
                        "dato inválido"
                );

        ResponseEntity<ProblemDetail> response =
                handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode());

        assertEquals(
                "dato inválido",
                response.getBody().getDetail()
        );
    }
}