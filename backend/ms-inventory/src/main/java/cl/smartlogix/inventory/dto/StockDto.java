package cl.smartlogix.inventory.dto;

import java.time.OffsetDateTime;

public record StockDto(
    Long idStock,
    Long idProducto,
    String sku,
    Long idBodega,
    String bodega,
    Integer cantidad,
    Integer cantReservada,
    Integer disponible,
    Integer stockMinimo,
    OffsetDateTime updatedAt
) {
    public static StockDto from(Stock s) {
        return new StockDto(
            s.getIdStock(),
            s.getProducto().getIdProducto(),
            s.getProducto().getSku(),
            s.getBodega().getIdBodega(),
            s.getBodega().getNombre(),
            s.getCantidad(),
            s.getCantReservada(),
            s.getDisponible(),
            s.getStockMinimo(),
            s.getUpdatedAt()
        );
    }
}
