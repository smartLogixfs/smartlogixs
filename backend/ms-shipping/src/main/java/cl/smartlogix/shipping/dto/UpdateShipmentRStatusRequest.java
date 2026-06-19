package cl.smartlogix.shipping.dto;

import cl.smartlogix.shipping.model.ShipmentState;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateShipmentRStatusRequest(
    @NotNull(message = "status es obligatorio")
    ShipmentState status,

    @Size(max = 255)
    String location,

    @Size(max = 500)
    String comment
) {}
