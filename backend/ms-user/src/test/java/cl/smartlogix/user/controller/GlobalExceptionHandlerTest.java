package cl.smartlogix.user.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void debeRetornar404CuandoUsuarioNoEncontrado() {

        RuntimeException ex =
                new RuntimeException("Usuario no encontrado");

        ResponseEntity<ProblemDetail> response =
                handler.handleRuntime(ex);

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Recurso no encontrado",
                response.getBody().getTitle()
        );

        assertEquals(
                "Usuario no encontrado",
                response.getBody().getDetail()
        );
    }

    @Test
    void debeRetornar404CuandoUsuarioNoExiste() {

        RuntimeException ex =
                new RuntimeException("Usuario no existe");

        ResponseEntity<ProblemDetail> response =
                handler.handleRuntime(ex);

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertEquals(
                "Recurso no encontrado",
                response.getBody().getTitle()
        );
    }

    @Test
    void debeRetornar409CuandoCorreoYaRegistrado() {

        RuntimeException ex =
                new RuntimeException("El correo ya está registrado");

        ResponseEntity<ProblemDetail> response =
                handler.handleRuntime(ex);

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        assertEquals(
                "Conflicto",
                response.getBody().getTitle()
        );
    }

    @Test
    void debeRetornar409CuandoExisteDuplicado() {

        RuntimeException ex =
                new RuntimeException("Registro duplicado");

        ResponseEntity<ProblemDetail> response =
                handler.handleRuntime(ex);

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        assertEquals(
                "Conflicto",
                response.getBody().getTitle()
        );
    }

    @Test
    void debeRetornar400ParaRuntimeExceptionGenerica() {

        RuntimeException ex =
                new RuntimeException("Error cualquiera");

        ResponseEntity<ProblemDetail> response =
                handler.handleRuntime(ex);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertEquals(
                "Error en la solicitud",
                response.getBody().getTitle()
        );
    }

    @Test
    void debeManejarIllegalArgumentException() {

        IllegalArgumentException ex =
                new IllegalArgumentException("Dato inválido");

        ResponseEntity<ProblemDetail> response =
                handler.handleIllegalArgument(ex);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertEquals(
                "Solicitud inválida",
                response.getBody().getTitle()
        );

        assertEquals(
                "Dato inválido",
                response.getBody().getDetail()
        );
    }

    @Test
    void debeManejarResponseStatusException404() {

        ResponseStatusException ex =
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                );

        ResponseEntity<ProblemDetail> response =
                handler.handleResponseStatus(ex);

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertEquals(
                "Usuario no encontrado",
                response.getBody().getDetail()
        );
    }

    @Test
    void debeManejarResponseStatusException400() {

        ResponseStatusException ex =
                new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Solicitud inválida"
                );

        ResponseEntity<ProblemDetail> response =
                handler.handleResponseStatus(ex);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertEquals(
                "Solicitud inválida",
                response.getBody().getDetail()
        );
    }

    @Test
    void debeManejarExceptionGenerica() {

        Exception ex =
                new Exception("Error interno");

        ResponseEntity<ProblemDetail> response =
                handler.handleGeneric(ex);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertEquals(
                "Error interno",
                response.getBody().getTitle()
        );

        assertEquals(
                "Ocurrió un error al procesar la solicitud",
                response.getBody().getDetail()
        );
    }
}