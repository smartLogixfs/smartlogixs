package cl.smartlogix.usuario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record UsuarioDto(
    @JsonProperty("name")
    @Schema(description = "Nombre del usuario")
    String nombre,

    @JsonProperty("email")
    @Schema(description = "Correo del usuario (debe ser único)")
    String correo,

    @Schema(description = "Contraseña del usuario")
    String password,

    @JsonProperty("phone")
    @Schema(description = "Telefono")
    String telefono,

    @JsonProperty("address")
    @Schema(description = "Dirección")
    String direccion,

    @Schema(description = "Región")
    String region,

    @JsonProperty("district")
    @Schema(description = "Comuna")
    String comuna
) {}
