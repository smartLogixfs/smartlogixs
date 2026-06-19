package cl.smartlogix.order.dto;

import cl.smartlogix.order.model.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
    OrderType type,

    @NotBlank(message = "customerId es obligatorio")
    @Size(max = 64)
    String customerId,

    @Size(max = 64)
    String marketplaceId,

    @NotEmpty(message = "El pedido debe tener al menos un ítem")
    @Valid
    List<ItemRequest> items
) {
    public record ItemRequest(
        @NotNull(message = "productId es obligatorio")
        Long productId,

        @NotBlank(message = "sku es obligatorio")
        @Size(max = 64)
        String sku,

        @NotNull @Min(value = 1, message = "quantity debe ser >= 1")
        Integer quantity,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false, message = "unitPrice debe ser > 0")
        BigDecimal unitPrice
    ) {}
}
