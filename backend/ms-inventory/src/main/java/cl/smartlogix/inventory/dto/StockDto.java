package cl.smartlogix.inventory.dto;

import cl.smartlogix.inventory.model.Stock;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record StockDto(
    @JsonProperty("stockId") Long idStock,
    @JsonProperty("productId") Long idProducto,
    String sku,
    @JsonProperty("warehouseId") Long idBodega,
    @JsonProperty("warehouseName") String bodega,
    @JsonProperty("quantity") Integer cantidad,
    @JsonProperty("reservedQuantity") Integer cantReservada,
    @JsonProperty("available") Integer disponible,
    @JsonProperty("minStock") Integer stockMinimo,
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
