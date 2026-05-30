package cl.smartlogix.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UsuarioDto(
    @Schema(description = "Nombre del usuario")
    String nombre,

    @Schema(description = "Correo del usuario (debe ser único)")
    String correo,

    @Schema(description = "Contraseña del usuario")
    String password,

    @Schema(description = "Telefono")
    String telefono,

    @Schema(description = "Dirección")
    String direccion,

    @Schema(description = "Región")
    String region,

    @Schema(description = "Comuna")
    String comuna
) {}
