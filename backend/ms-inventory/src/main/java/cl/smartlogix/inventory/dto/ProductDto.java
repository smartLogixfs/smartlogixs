package cl.smartlogix.inventory.dto;

import cl.smartlogix.inventory.model.Product;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductDto(
    Long productId,
    String sku,
    String name,
    String description,
    BigDecimal price,
    Boolean active,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static ProductDto from(Product p) {
        return new ProductDto(
            p.getId(),
            p.getSku(),
            p.getName(),
            p.getDescription(),
            p.getPrice(),
            p.getActive(),
            p.getCreatedAt(),
            p.getUpdatedAt()
        );
    }
}
