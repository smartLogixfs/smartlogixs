package cl.smartlogix.pedido.dto;

import cl.smartlogix.pedido.model.EstadoPedido;

public record ActualizarEstadoRequest(
    EstadoPedido estado,
    String motivo
) {}
