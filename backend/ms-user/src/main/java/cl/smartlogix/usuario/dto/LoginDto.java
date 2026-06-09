package cl.smartlogix.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginDto(
    @Schema(description = "Correo del usuario")
    String correo,

    @Schema(description = "Contraseña del usuario")
    String password
) {}
