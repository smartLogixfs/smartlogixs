package cl.smartlogix.order.dto;

import cl.smartlogix.order.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateOrderState(
    @NotNull(message = "status es obligatorio")
    OrderStatus status,

    @Size(max = 255)
    String reason
) {}
