package cl.smartlogix.pedido.dto;

import cl.smartlogix.pedido.model.Order;

import java.math.BigDecimal;

public record OrderItemDto(
    Long idItem,
    Long idProducto,
    String sku,
    Integer cantidad,
    BigDecimal precioUnitario,
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
