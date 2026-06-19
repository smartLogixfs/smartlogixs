package cl.smartlogix.shipping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record AssingCarierRequest(
    @JsonProperty("carrierId")
    @NotNull(message = "carrierId es obligatorio")
    Long idTransportista
) {}
