package cl.smartlogix.pedido.dto;

import cl.smartlogix.pedido.model.TipoPedido;

import java.math.BigDecimal;
import java.util.List;

public record CrearPedidoRequest(
    TipoPedido tipo,
    String idCliente,
    String idMarketplace,
    List<ItemRequest> items
) {
    public record ItemRequest(
        Long idProducto,
        String sku,
        Integer cantidad,
        BigDecimal precioUnitario
    ) {}
}
