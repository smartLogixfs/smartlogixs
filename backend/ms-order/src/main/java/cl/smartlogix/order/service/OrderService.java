package cl.smartlogix.order.service;

import cl.smartlogix.order.dto.ActualizarEstadoRequest;
import cl.smartlogix.order.dto.CreateOrderRequest;
import cl.smartlogix.order.dto.OrderDto;
import cl.smartlogix.order.model.EstadoPedido;

import java.util.List;

public interface OrderService {
    OrderDto crear(CreateOrderRequest req);
    OrderDto findById(Long id);
    OrderDto findByCodigo(String codigo);
    List<OrderDto> findAll();
    List<OrderDto> findByEstado(EstadoPedido estado);
    List<OrderDto> findByCliente(String idCliente);
    OrderDto cambiarEstado(Long id, ActualizarEstadoRequest req);
}
