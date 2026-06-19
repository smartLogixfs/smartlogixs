package cl.smartlogix.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWarehouseRequest(
    @NotBlank(message = "name es obligatorio")
    @Size(max = 120)
    String name,

    @Size(max = 255)
    String location
) {}
