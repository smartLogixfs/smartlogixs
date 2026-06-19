package cl.smartlogix.shipping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCarrierRequest(
    @JsonProperty("name")
    @NotBlank(message = "name es obligatorio")
    @Size(max = 120)
    String nombre,

    @Size(max = 20)
    String rut,

    @JsonProperty("contactPhone")
    @Size(max = 40)
    String telefonoContacto
) {}
