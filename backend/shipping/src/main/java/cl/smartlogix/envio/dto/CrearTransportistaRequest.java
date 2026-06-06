package cl.smartlogix.envio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearTransportistaRequest(
    @NotBlank(message = "nombre es obligatorio")
    @Size(max = 120)
    String nombre,

    @Size(max = 20)
    String rut,

    @Size(max = 40)
    String telefonoContacto
) {}
