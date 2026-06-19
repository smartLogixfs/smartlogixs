package cl.smartlogix.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockMovementRequest(
    @NotNull(message = "productId es obligatorio")
    Long productId,

    @NotNull(message = "warehouseId es obligatorio")
    Long warehouseId,

    @NotNull @Min(value = 1, message = "quantity debe ser >= 1")
    Integer quantity,

    @Size(max = 64)
    String orderReference
) {}
