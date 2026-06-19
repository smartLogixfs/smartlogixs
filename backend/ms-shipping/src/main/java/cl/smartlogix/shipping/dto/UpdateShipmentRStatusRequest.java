package cl.smartlogix.shipping.dto;

import cl.smartlogix.shipping.model.ShipmentState;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateShipmentRStatusRequest(
    @JsonProperty("status")
    @NotNull(message = "status es obligatorio")
    ShipmentState estado,

    @JsonProperty("location")
    @Size(max = 255)
    String ubicacion,

    @JsonProperty("comment")
    @Size(max = 500)
    String comentario
) {}
