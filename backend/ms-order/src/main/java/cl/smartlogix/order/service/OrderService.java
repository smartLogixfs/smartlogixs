package cl.smartlogix.order.service;

import cl.smartlogix.order.dto.UpdateOrderState;
import cl.smartlogix.order.dto.CreateOrderRequest;
import cl.smartlogix.order.dto.OrderDto;
import cl.smartlogix.order.model.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderDto crear(CreateOrderRequest req);
    OrderDto findById(Long id);
    OrderDto findByCodigo(String codigo);
    List<OrderDto> findAll();
    List<OrderDto> findByEstado(OrderStatus estado);
    List<OrderDto> findByCliente(String idCliente);
    OrderDto cambiarEstado(Long id, UpdateOrderState req);
}
