package cl.smartlogix.order.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void handleValidationShouldReturnBadRequest() {

        BeanPropertyBindingResult binding =
                new BeanPropertyBindingResult(new Object(), "request");

        binding.addError(
                new FieldError(
                        "request",
                        "customerId",
                        "customerId es obligatorio"
                )
        );

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, binding);

        var response = handler.handleValidation(ex);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        ProblemDetail body = response.getBody();

        assertNotNull(body);
        assertEquals("Validación fallida", body.getTitle());
        assertNotNull(body.getProperties().get("errors"));
        assertNotNull(body.getProperties().get("timestamp"));
    }

    @Test
    void handleResponseStatusShouldReturnSameStatus() {

        ResponseStatusException ex =
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido no encontrado"
                );

        var response =
                handler.handleResponseStatus(ex);

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
        assertEquals(
                "Pedido no encontrado",
                response.getBody().getDetail()
        );
    }

    @Test
    void handleIllegalArgumentShouldReturnBadRequest() {

        IllegalArgumentException ex =
                new IllegalArgumentException(
                        "Dato inválido"
                );

        var response =
                handler.handleIllegalArgument(ex);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Dato inválido",
                response.getBody().getDetail()
        );
    }
}