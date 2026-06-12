package cl.smartlogix.inventory.dto;

import cl.smartlogix.inventory.model.Product;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductDto(
    Long idProducto,
    String sku,
    String nombre,
    String descripcion,
    BigDecimal precio,
    Boolean activo,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static ProductDto from(Product p) {
        return new ProductDto(
            p.getIdProducto(),
            p.getSku(),
            p.getNombre(),
            p.getDescripcion(),
            p.getPrecio(),
            p.getActivo(),
            p.getCreatedAt(),
            p.getUpdatedAt()
        );
    }
}
