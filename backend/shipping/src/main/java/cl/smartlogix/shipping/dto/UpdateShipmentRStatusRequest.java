package cl.smartlogix.shipping.dto;

import cl.smartlogix.shipping.model.EstadoEnvio;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateShipmentRStatusRequest(
    @NotNull(message = "estado es obligatorio")
    EstadoEnvio estado,

    @Size(max = 255)
    String ubicacion,

    @Size(max = 500)
    String comentario
) {}
