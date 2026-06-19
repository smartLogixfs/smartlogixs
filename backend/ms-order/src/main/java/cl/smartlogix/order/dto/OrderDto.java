package cl.smartlogix.order.dto;

import cl.smartlogix.order.model.OrderStatus;
import cl.smartlogix.order.model.Order;
import cl.smartlogix.order.model.OrderType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderDto(
    Long orderId,
    String code,
    OrderType type,
    OrderStatus status,
    String customerId,
    String marketplaceId,
    BigDecimal subtotal,
    BigDecimal tax,
    BigDecimal total,
    List<OrderItemDto> items,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static OrderDto from(Order o) {
        return new OrderDto(
            o.getId(),
            o.getCode(),
            o.getType(),
            o.getStatus(),
            o.getCustomerId(),
            o.getMarketplaceId(),
            o.getSubtotal(),
            o.getTax(),
            o.getTotal(),
            o.getItems().stream().map(OrderItemDto::from).toList(),
            o.getCreatedAt(),
            o.getUpdatedAt()
        );
    }
}
