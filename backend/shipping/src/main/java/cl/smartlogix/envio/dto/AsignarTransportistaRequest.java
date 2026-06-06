package cl.smartlogix.envio.dto;

import jakarta.validation.constraints.NotNull;

public record AsignarTransportistaRequest(
    @NotNull(message = "idTransportista es obligatorio")
    Long idTransportista
) {}
