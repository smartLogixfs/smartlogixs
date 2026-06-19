package cl.smartlogix.order.dto;

import cl.smartlogix.order.model.OrderItem;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record OrderItemDto(
    @JsonProperty("itemId") Long idItem,
    @JsonProperty("productId") Long idProducto,
    String sku,
    @JsonProperty("quantity") Integer cantidad,
    @JsonProperty("unitPrice") BigDecimal precioUnitario,
    BigDecimal subtotal
) {
    public static OrderItemDto from(OrderItem item) {
        return new OrderItemDto(
            item.getIdItem(),
            item.getIdProducto(),
            item.getSku(),
            item.getCantidad(),
            item.getPrecioUnitario(),
            item.getSubtotal()
        );
    }
}
