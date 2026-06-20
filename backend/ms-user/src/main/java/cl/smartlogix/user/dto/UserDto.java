package cl.smartlogix.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import cl.smartlogix.user.model.User;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDto(
    @Schema(description = "ID interno (nulo en requests)")
    Long id,

    @Schema(description = "Nombre del usuario")
    String name,

    @Schema(description = "Correo del usuario (debe ser único)")
    String email,

    @Schema(description = "Contraseña (solo en requests)")
    String password,

    @Schema(description = "Teléfono")
    String phone,

    @Schema(description = "Dirección")
    String address,

    @Schema(description = "Región")
    String region,

    @Schema(description = "Comuna")
    String district
) {
    // Factory para responses sin exponer el hash de password
    public static UserDto from(User u) {
        return new UserDto(
            u.getId(),
            u.getName(),
            u.getEmail(),
            null,
            u.getPhone(),
            u.getAddress(),
            u.getRegion(),
            u.getDistrict()
        );
    }
}
