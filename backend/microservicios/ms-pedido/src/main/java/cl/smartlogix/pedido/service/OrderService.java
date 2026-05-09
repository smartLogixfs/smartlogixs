package cl.smartlogix.pedido.service;

import java.util.List;

import cl.smartlogix.pedido.dto.OrderDto;

public interface OrderService {
    OrderDto create(OrderDto dto);
    OrderDto getById(Long id);
    List<OrderDto> getAll();
    OrderDto update(Long id, OrderDto dto);
    void delete(Long id);
}
