package cl.smartlogix.order.dto;

import cl.smartlogix.order.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateOrderState(
    @NotNull(message = "estado es obligatorio")
    OrderStatus estado,

    @Size(max = 255)
    String motivo
) {}
