package cl.smartlogix.pedido.dto;

import cl.smartlogix.pedido.model.EstadoPedido;
import cl.smartlogix.pedido.model.Pedido;
import cl.smartlogix.pedido.model.TipoPedido;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderDto(
    Long idPedido,
    String codigo,
    TipoPedido tipo,
    EstadoPedido estado,
    String idCliente,
    String idMarketplace,
    BigDecimal subtotal,
    BigDecimal impuesto,
    BigDecimal total,
    List<PedidoItemDto> items,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static OrderDto from(Pedido p) {
        return new OrderDto(
            p.getIdPedido(),
            p.getCodigo(),
            p.getTipo(),
            p.getEstado(),
            p.getIdCliente(),
            p.getIdMarketplace(),
            p.getSubtotal(),
            p.getImpuesto(),
            p.getTotal(),
            p.getItems().stream().map(PedidoItemDto::from).toList(),
            p.getCreatedAt(),
            p.getUpdatedAt()
        );
    }
}
