package cl.smartlogix.shipping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateShipmentRequest(
    @NotNull(message = "idPedido es obligatorio")
    Long idPedido,

    @NotBlank(message = "direccionDestino es obligatoria")
    @Size(max = 255)
    String direccionDestino,

    @Size(max = 120)
    String comuna,

    @Size(max = 120)
    String region,

    LocalDate fechaEstimada
) {}
