package cl.smartlogix.shipping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateShipmentRequest(
    @NotNull(message = "orderId es obligatorio")
    Long orderId,

    @NotBlank(message = "destinationAddress es obligatoria")
    @Size(max = 255)
    String destinationAddress,

    @Size(max = 120)
    String district,

    @Size(max = 120)
    String region,

    LocalDate estimatedDate
) {}
