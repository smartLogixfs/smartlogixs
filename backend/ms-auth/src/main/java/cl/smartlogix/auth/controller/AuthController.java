package cl.smartlogix.auth.controller;

import cl.smartlogix.auth.dto.AuthResponse;
import cl.smartlogix.auth.dto.LoginRequest;
import cl.smartlogix.auth.dto.RegisterRequest;
import cl.smartlogix.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST de autenticación.
 * Expone el registro de usuarios y el login con emisión de access token JWT (RS256).
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Registro, login y emisión de JWT (RS256)")
public class AuthController {

    private final AuthService authService;

    /**
     * @param authService servicio de autenticación inyectado por Spring
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registra una nueva cuenta de usuario.
     *
     * @param request datos de registro (credenciales y perfil)
     * @return HTTP 201 si el registro fue exitoso
     */
    @Operation(summary = "Registrar usuario", description = "Crea una nueva cuenta de usuario.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "El usuario ya existe")
    })
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Valida las credenciales del usuario y emite un access token JWT.
     *
     * @param request credenciales de acceso
     * @return la respuesta de autenticación con el token
     */
    @Operation(summary = "Iniciar sesión", description = "Valida credenciales y devuelve un access token JWT.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login exitoso, token emitido"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
