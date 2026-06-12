package cl.smartlogix.inventory.dto;

import cl.smartlogix.inventory.model.StockMovement;
import cl.smartlogix.inventory.model.MovementType;

import java.time.OffsetDateTime;

public record StockMovementDto(
    Long idMovimiento,
    Long idStock,
    MovementType tipo,
    Integer cantidad,
    String referenciaPedido,
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
