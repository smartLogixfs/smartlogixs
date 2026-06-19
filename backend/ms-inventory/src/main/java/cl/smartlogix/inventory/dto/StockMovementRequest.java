package cl.smartlogix.inventory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockMovementRequest(
    @JsonProperty("productId")
    @NotNull(message = "productId es obligatorio")
    Long idProducto,

    @JsonProperty("warehouseId")
    @NotNull(message = "warehouseId es obligatoria")
    Long idBodega,

    @JsonProperty("quantity")
    @NotNull @Min(value = 1, message = "quantity debe ser >= 1")
    Integer cantidad,

    @JsonProperty("orderReference")
    @Size(max = 64)
    String referenciaPedido
) {}
