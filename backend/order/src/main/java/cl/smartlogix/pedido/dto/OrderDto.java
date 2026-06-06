package cl.smartlogix.pedido.dto;

import cl.smartlogix.pedido.model.OrderStatus;
import cl.smartlogix.pedido.model.Order;
import cl.smartlogix.pedido.model.OrderType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderDto(
    Long idPedido,
    String codigo,
    OrderType tipo,
    OrderStatus estado,
    String idCliente,
    String idMarketplace,
    BigDecimal subtotal,
    BigDecimal impuesto,
    BigDecimal total,
    List<OrderItemDto> items,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static OrderDto from(Order p) {
        return new OrderDto(
            p.getIdPedido(),
            p.getCodigo(),
            p.getTipo(),
            p.getEstado(),
            p.getIdCliente(),
            p.getIdMarketplace(),
            p.getSubtotal(),
            p.getImpuesto(),
            p.getTotal(),
            p.getItems().stream().map(OrderItemDto::from).toList(),
            p.getCreatedAt(),
            p.getUpdatedAt()
        );
    }
}
