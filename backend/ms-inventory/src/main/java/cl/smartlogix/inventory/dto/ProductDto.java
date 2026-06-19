package cl.smartlogix.inventory.dto;

import cl.smartlogix.inventory.model.Product;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductDto(
    @JsonProperty("productId") Long idProducto,
    String sku,
    @JsonProperty("name") String nombre,
    @JsonProperty("description") String descripcion,
    @JsonProperty("price") BigDecimal precio,
    @JsonProperty("active") Boolean activo,
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
