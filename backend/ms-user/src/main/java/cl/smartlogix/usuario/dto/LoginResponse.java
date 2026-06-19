package cl.smartlogix.usuario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
    @Schema(description = "Indica si el login fue exitoso")
    boolean success,

    @JsonProperty("message")
    @Schema(description = "Mensaje relacionado al resultado del login")
    String mensaje
) {}
