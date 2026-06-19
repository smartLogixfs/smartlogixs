package cl.smartlogix.inventory.dto;

import cl.smartlogix.inventory.model.StockMovement;
import cl.smartlogix.inventory.model.MovementType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record StockMovementDto(
    @JsonProperty("movementId") Long idMovimiento,
    @JsonProperty("stockId") Long idStock,
    @JsonProperty("type") MovementType tipo,
    @JsonProperty("quantity") Integer cantidad,
    @JsonProperty("orderReference") String referenciaPedido,
    OffsetDateTime createdAt
) {
    public static StockMovementDto from(StockMovement m) {
        return new StockMovementDto(
            m.getIdMovimiento(),
            m.getStock().getIdStock(),
            m.getTipo(),
            m.getCantidad(),
            m.getReferenciaPedido(),
            m.getCreatedAt()
        );
    }
}
