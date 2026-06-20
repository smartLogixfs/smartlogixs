package cl.smartlogix.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(
    @Size(max = 200)
    String name,

    String description,

    @DecimalMin(value = "0.0", message = "price debe ser >= 0")
    BigDecimal price,

    Boolean active
) {}
