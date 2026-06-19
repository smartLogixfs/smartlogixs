package cl.smartlogix.inventory.dto;

import cl.smartlogix.inventory.model.StockMovement;
import cl.smartlogix.inventory.model.MovementType;

import java.time.OffsetDateTime;

public record StockMovementDto(
    Long movementId,
    Long stockId,
    MovementType type,
    Integer quantity,
    String orderReference,
    OffsetDateTime createdAt
) {
    public static StockMovementDto from(StockMovement m) {
        return new StockMovementDto(
            m.getId(),
            m.getStock().getId(),
            m.getType(),
            m.getQuantity(),
            m.getOrderReference(),
            m.getCreatedAt()
        );
    }
}
