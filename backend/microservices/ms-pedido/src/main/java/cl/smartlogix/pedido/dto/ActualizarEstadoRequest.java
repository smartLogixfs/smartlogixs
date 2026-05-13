package cl.smartlogix.pedido.dto;

import cl.smartlogix.pedido.model.EstadoPedido;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ActualizarEstadoRequest(
    @NotNull(message = "estado es obligatorio")
    EstadoPedido estado,

    @Size(max = 255)
    String motivo
) {}
