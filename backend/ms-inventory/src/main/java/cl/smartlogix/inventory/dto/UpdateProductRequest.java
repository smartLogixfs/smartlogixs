package cl.smartlogix.inventory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(
    @JsonProperty("name")
    @Size(max = 200)
    String nombre,

    @JsonProperty("description")
    String descripcion,

    @JsonProperty("price")
    @DecimalMin(value = "0.0", message = "price debe ser >= 0")
    BigDecimal precio,

    @JsonProperty("active")
    Boolean activo
) {}
