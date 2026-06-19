package cl.smartlogix.order.dto;

import cl.smartlogix.order.model.OrderType;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("type") OrderType tipo,

    @JsonProperty("customerId")
    @NotBlank(message = "customerId es obligatorio")
    @Size(max = 64)
    String idCliente,

    @JsonProperty("marketplaceId")
    @Size(max = 64)
    String idMarketplace,

    @NotEmpty(message = "El pedido debe tener al menos un ítem")
    @Valid
    List<ItemRequest> items
) {
    public record ItemRequest(
        @JsonProperty("productId")
        @NotNull(message = "productId es obligatorio")
        Long idProducto,

        @NotBlank(message = "sku es obligatorio")
        @Size(max = 64)
        String sku,

        @JsonProperty("quantity")
        @NotNull @Min(value = 1, message = "quantity debe ser >= 1")
        Integer cantidad,

        @JsonProperty("unitPrice")
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false, message = "unitPrice debe ser > 0")
        BigDecimal precioUnitario
    ) {}
}
