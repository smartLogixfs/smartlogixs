package cl.smartlogix.shipping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateShipmentRequest(
    @JsonProperty("orderId")
    @NotNull(message = "orderId es obligatorio")
    Long idPedido,

    @JsonProperty("destinationAddress")
    @NotBlank(message = "destinationAddress es obligatoria")
    @Size(max = 255)
    String direccionDestino,

    @JsonProperty("district")
    @Size(max = 120)
    String comuna,

    @Size(max = 120)
    String region,

    @JsonProperty("estimatedDate")
    LocalDate fechaEstimada
) {}
