package cl.smartlogix.inventario.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CrearProductoRequest(
    @NotBlank(message = "sku es obligatorio")
    @Size(max = 64)
    String sku,

    @NotBlank(message = "nombre es obligatorio")
    @Size(max = 200)
    String nombre,

    String descripcion,

    @NotNull
    @DecimalMin(value = "0.0", message = "precio debe ser >= 0")
    BigDecimal precio
) {}
