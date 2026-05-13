package cl.smartlogix.pedido.service;

import cl.smartlogix.pedido.dto.ActualizarEstadoRequest;
import cl.smartlogix.pedido.dto.CrearPedidoRequest;
import cl.smartlogix.pedido.dto.OrderDto;
import cl.smartlogix.pedido.model.EstadoPedido;

import java.util.List;

public interface OrderService {
    OrderDto crear(CrearPedidoRequest req);
    OrderDto findById(Long id);
    OrderDto findByCodigo(String codigo);
    List<OrderDto> findAll();
    List<OrderDto> findByEstado(EstadoPedido estado);
    List<OrderDto> findByCliente(String idCliente);
    OrderDto cambiarEstado(Long id, ActualizarEstadoRequest req);
}
