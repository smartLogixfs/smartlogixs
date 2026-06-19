package cl.smartlogix.order.dto;

import cl.smartlogix.order.model.OrderStatus;
import cl.smartlogix.order.model.Order;
import cl.smartlogix.order.model.OrderType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderDto(
    @JsonProperty("orderId") Long idPedido,
    @JsonProperty("code") String codigo,
    @JsonProperty("type") OrderType tipo,
    @JsonProperty("status") OrderStatus estado,
    @JsonProperty("customerId") String idCliente,
    @JsonProperty("marketplaceId") String idMarketplace,
    BigDecimal subtotal,
    @JsonProperty("tax") BigDecimal impuesto,
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
