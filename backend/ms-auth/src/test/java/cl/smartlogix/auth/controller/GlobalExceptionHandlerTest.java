package cl.smartlogix.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
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
                new BeanPropertyBindingResult(
                        new Object(),
                        "request"
                );

        binding.addError(
                new FieldError(
                        "request",
                        "email",
                        "email obligatorio"
                )
        );

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(
                        null,
                        binding
                );

        var response =
                handler.handleValidation(ex);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        ProblemDetail body =
                response.getBody();

        assertNotNull(body);
        assertEquals(
                "Validación fallida",
                body.getTitle()
        );
    }

    @Test
    void handleBadCredentialsShouldReturnUnauthorized() {

        var response =
                handler.handleBadCredentials(
                        new BadCredentialsException("bad")
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
    }

    @Test
    void handleIllegalArgumentShouldReturnBadRequest() {

        var response =
                handler.handleIllegalArgument(
                        new IllegalArgumentException(
                                "Email already in use"
                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertEquals(
                "Solicitud inválida",
                response.getBody().getTitle()
        );
    }

    @Test
    void handleResponseStatusShouldReturnStatus() {

        var response =
                handler.handleResponseStatus(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "not found"
                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );
    }

    @Test
    void handleGenericShouldReturnInternalError() {

        var response =
                handler.handleGeneric(
                        new RuntimeException("boom")
                );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertEquals(
                "Error interno",
                response.getBody().getTitle()
        );
    }
}