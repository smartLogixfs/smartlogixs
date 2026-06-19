package cl.smartlogix.inventory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(
    @NotBlank(message = "sku es obligatorio")
    @Size(max = 64)
    String sku,

    @JsonProperty("name")
    @NotBlank(message = "name es obligatorio")
    @Size(max = 200)
    String nombre,

    @JsonProperty("description")
    String descripcion,

    @JsonProperty("price")
    @NotNull
    @DecimalMin(value = "0.0", message = "price debe ser >= 0")
    BigDecimal precio
) {}
