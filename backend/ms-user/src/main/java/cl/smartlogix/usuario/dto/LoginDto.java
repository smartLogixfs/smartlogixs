package cl.smartlogix.usuario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoginDto(
    @JsonProperty("email")
    @Schema(description = "Correo del usuario")
    String correo,

    @Schema(description = "Contraseña del usuario")
    String password
) {}
