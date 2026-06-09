package cl.smartlogix.inventory.dto;

import cl.smartlogix.inventory.model.MovimientoStock;
import cl.smartlogix.inventory.model.TipoMovimiento;

import java.time.OffsetDateTime;

public record StockMovementDto(
    Long idMovimiento,
    Long idStock,
    TipoMovimiento tipo,
    Integer cantidad,
    String referenciaPedido,
    OffsetDateTime createdAt
) {
    public static StockMovementDto from(MovimientoStock m) {
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
