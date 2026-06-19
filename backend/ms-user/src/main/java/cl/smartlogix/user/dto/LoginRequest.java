package cl.smartlogix.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequest(
    @Schema(description = "Correo del usuario")
    String email,

    @Schema(description = "Contraseña del usuario")
    String password
) {}
