package cl.smartlogix.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import cl.smartlogix.auth.dto.AuthResponse;
import cl.smartlogix.auth.dto.LoginRequest;
import cl.smartlogix.auth.dto.RegisterRequest;
import cl.smartlogix.auth.service.AuthService;

class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);

    private final AuthController controller =
            new AuthController(authService);

    @Test
    void registerShouldReturnCreated() {

        RegisterRequest request =
                new RegisterRequest(
                        "Juan Perez",
                        "juan@test.cl",
                        "Password123"
                );

        ResponseEntity<Void> response =
                controller.register(request);

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        verify(authService).register(request);
    }

    @Test
    void loginShouldReturnToken() {

        LoginRequest request =
                new LoginRequest(
                        "juan@test.cl",
                        "Password123"
                );

        AuthResponse authResponse =
                new AuthResponse(
                        "jwt-token",
                        "Bearer",
                        1800
                );

        when(authService.login(request))
                .thenReturn(authResponse);

        ResponseEntity<AuthResponse> response =
                controller.login(request);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "jwt-token",
                response.getBody().accessToken()
        );

        verify(authService).login(request);
    }
}