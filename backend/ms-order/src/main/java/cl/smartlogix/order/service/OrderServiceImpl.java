package cl.smartlogix.order.service;

import cl.smartlogix.order.dto.UpdateOrderState;
import cl.smartlogix.order.dto.CreateOrderRequest;
import cl.smartlogix.order.dto.OrderDto;
import cl.smartlogix.order.factory.OrderFactory;
import cl.smartlogix.order.factory.OrderFactoryProvider;
import cl.smartlogix.order.model.*;
import cl.smartlogix.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Máquina de estados del pedido (no permite saltos arbitrarios)
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
        OrderStatus.PENDIENTE,      Set.of(OrderStatus.APROBADO, OrderStatus.RECHAZADO, OrderStatus.CANCELADO),
        OrderStatus.APROBADO,       Set.of(OrderStatus.EN_PREPARACION, OrderStatus.CANCELADO),
        OrderStatus.EN_PREPARACION, Set.of(OrderStatus.ENVIADO, OrderStatus.CANCELADO),
        OrderStatus.ENVIADO,        Set.of(OrderStatus.ENTREGADO),
        OrderStatus.ENTREGADO,      Set.of(),
        OrderStatus.RECHAZADO,      Set.of(),
        OrderStatus.CANCELADO,      Set.of()
    );

    private final OrderRepository repository;
    private final OrderFactoryProvider factoryProvider;

    @Override
    public OrderDto create(CreateOrderRequest req) {
        if (req.items() == null || req.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido debe tener al menos un ítem");
        }

        // Factory Method: el creador propio del tipo arma el agregado del pedido.
        OrderFactory factory = factoryProvider.forType(req.type());
        Order order = factory.build(req, generateCode());

        return OrderDto.from(repository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto findById(Long id) {
        return OrderDto.from(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto findByCode(String code) {
        Order o = repository.findByCode(code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado: " + code));
        return OrderDto.from(o);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        return repository.findAll().stream().map(OrderDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findByStatus(OrderStatus status) {
        return repository.findByStatus(status).stream().map(OrderDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findByCustomer(String customerId) {
        return repository.findByCustomerId(customerId).stream().map(OrderDto::from).toList();
    }

    @Override
    public OrderDto changeStatus(Long id, UpdateOrderState req) {
        Order order = find(id);
        OrderStatus current = order.getStatus();
        OrderStatus next = req.status();

        if (!TRANSITIONS.getOrDefault(current, Set.of()).contains(next)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Transición no permitida: " + current + " → " + next);
        }

        order.setStatus(next);
        order.addHistory(OrderHistory.builder()
            .previousStatus(current)
            .newStatus(next)
            .reason(req.reason())
            .build());

        return OrderDto.from(repository.save(order));
    }

    private Order find(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado: " + id));
    }

    private String generateCode() {
        String date = LocalDate.now().format(DATE_FORMAT);
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "PED-" + date + "-" + suffix;
    }
}
