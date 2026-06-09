package cl.smartlogix.shipping.dto;

import jakarta.validation.constraints.NotNull;

public record AssingCarierRequest(
    @NotNull(message = "idTransportista es obligatorio")
    Long idTransportista
) {}
