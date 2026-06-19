package cl.smartlogix.inventory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWarehouseRequest(
    @JsonProperty("name")
    @NotBlank(message = "name es obligatorio")
    @Size(max = 120)
    String nombre,

    @JsonProperty("location")
    @Size(max = 255)
    String ubicacion
) {}
