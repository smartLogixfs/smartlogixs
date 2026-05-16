package cl.smartlogix.envio.dto;

import cl.smartlogix.envio.model.EstadoEnvio;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ActualizarEstadoEnvioRequest(
    @NotNull(message = "estado es obligatorio")
    EstadoEnvio estado,

    @Size(max = 255)
    String ubicacion,

    @Size(max = 500)
    String comentario
) {}
