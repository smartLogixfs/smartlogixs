package cl.smartlogix.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(
    @Size(max = 200)
    String nombre,

    String descripcion,

    @DecimalMin(value = "0.0", message = "precio debe ser >= 0")
    BigDecimal precio,

    Boolean activo
) {}
