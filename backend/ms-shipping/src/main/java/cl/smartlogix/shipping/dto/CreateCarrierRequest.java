package cl.smartlogix.shipping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCarrierRequest(
    @NotBlank(message = "name es obligatorio")
    @Size(max = 120)
    String name,

    @Size(max = 20)
    String rut,

    @Size(max = 40)
    String contactPhone
) {}
