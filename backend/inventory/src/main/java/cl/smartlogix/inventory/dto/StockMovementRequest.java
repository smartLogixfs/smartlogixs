package cl.smartlogix.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockMovementRequest(
    @NotNull(message = "idProducto es obligatorio")
    Long idProducto,

    @NotNull(message = "idBodega es obligatoria")
    Long idBodega,

    @NotNull @Min(value = 1, message = "cantidad debe ser >= 1")
    Integer cantidad,

    @Size(max = 64)
    String referenciaPedido
) {}
