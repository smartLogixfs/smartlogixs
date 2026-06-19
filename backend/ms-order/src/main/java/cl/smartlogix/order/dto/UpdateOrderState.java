package cl.smartlogix.order.dto;

import cl.smartlogix.order.model.OrderStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateOrderState(
    @JsonProperty("status")
    @NotNull(message = "status es obligatorio")
    OrderStatus estado,

    @JsonProperty("reason")
    @Size(max = 255)
    String motivo
) {}
