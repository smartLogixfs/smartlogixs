package cl.smartlogix.order.dto;

import cl.smartlogix.order.model.OrderItem;

import java.math.BigDecimal;

public record OrderItemDto(
    Long itemId,
    Long productId,
    String sku,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal
) {
    public static OrderItemDto from(OrderItem item) {
        return new OrderItemDto(
            item.getId(),
            item.getProductId(),
            item.getSku(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getSubtotal()
        );
    }
}
