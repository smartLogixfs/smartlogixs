package cl.smartlogix.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearBodegaRequest(
    @NotBlank(message = "nombre es obligatorio")
    @Size(max = 120)
    String nombre,

    @Size(max = 255)
    String ubicacion
) {}
