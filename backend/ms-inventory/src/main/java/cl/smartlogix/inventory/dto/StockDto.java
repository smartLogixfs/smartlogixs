package cl.smartlogix.inventory.dto;

import cl.smartlogix.inventory.model.Stock;

import java.time.OffsetDateTime;

public record StockDto(
    Long stockId,
    Long productId,
    String sku,
    Long warehouseId,
    String warehouseName,
    Integer quantity,
    Integer reservedQuantity,
    Integer available,
    Integer minStock,
    OffsetDateTime updatedAt
) {
    public static StockDto from(Stock s) {
        return new StockDto(
            s.getId(),
            s.getProduct().getId(),
            s.getProduct().getSku(),
            s.getWarehouse().getId(),
            s.getWarehouse().getName(),
            s.getQuantity(),
            s.getReservedQuantity(),
            s.getAvailable(),
            s.getMinStock(),
            s.getUpdatedAt()
        );
    }
}
