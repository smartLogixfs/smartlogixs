package cl.smartlogix.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
    @Schema(description = "Indica si el login fue exitoso")
    boolean success,

    @Schema(description = "Mensaje relacionado al resultado del login")
    String mensaje
) {}
