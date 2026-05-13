package cl.smartlogix.pedido.dto;

import cl.smartlogix.pedido.model.PedidoItem;

import java.math.BigDecimal;

public record PedidoItemDto(
    Long idItem,
    Long idProducto,
    String sku,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal
) {
    public static PedidoItemDto from(PedidoItem item) {
        return new PedidoItemDto(
            item.getIdItem(),
            item.getIdProducto(),
            item.getSku(),
            item.getCantidad(),
            item.getPrecioUnitario(),
            item.getSubtotal()
        );
    }
}
