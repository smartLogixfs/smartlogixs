package cl.smartlogix.order.service;

import cl.smartlogix.order.dto.UpdateOrderState;
import cl.smartlogix.order.dto.CreateOrderRequest;
import cl.smartlogix.order.dto.OrderDto;
import cl.smartlogix.order.model.*;
import cl.smartlogix.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal IVA = new BigDecimal("0.19");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Máquina de estados del pedido (no permite saltos arbitrarios)
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSICIONES = Map.of(
        OrderStatus.PENDIENTE,      Set.of(OrderStatus.APROBADO, OrderStatus.RECHAZADO, OrderStatus.CANCELADO),
        OrderStatus.APROBADO,       Set.of(OrderStatus.EN_PREPARACION, OrderStatus.CANCELADO),
        OrderStatus.EN_PREPARACION, Set.of(OrderStatus.ENVIADO, OrderStatus.CANCELADO),
        OrderStatus.ENVIADO,        Set.of(OrderStatus.ENTREGADO),
        OrderStatus.ENTREGADO,      Set.of(),
        OrderStatus.RECHAZADO,      Set.of(),
        OrderStatus.CANCELADO,      Set.of()
    );

    private final OrderRepository repository;

    @Override
    public OrderDto crear(CreateOrderRequest req) {
        if (req.items() == null || req.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido debe tener al menos un ítem");
        }

        Order pedido = Order.builder()
            .codigo(generarCodigo())
            .tipo(req.tipo() != null ? req.tipo() : OrderType.ESTANDAR)
            .estado(OrderStatus.PENDIENTE)
            .idCliente(req.idCliente())
            .idMarketplace(req.idMarketplace())
            .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CreateOrderRequest.ItemRequest it : req.items()) {
            BigDecimal sub = it.precioUnitario().multiply(BigDecimal.valueOf(it.cantidad()));
            OrderItem item = OrderItem.builder()
                .idProducto(it.idProducto())
                .sku(it.sku())
                .cantidad(it.cantidad())
                .precioUnitario(it.precioUnitario())
                .subtotal(sub)
                .build();
            pedido.addItem(item);
            subtotal = subtotal.add(sub);
        }
        BigDecimal impuesto = subtotal.multiply(IVA).setScale(2, RoundingMode.HALF_UP);
        pedido.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        pedido.setImpuesto(impuesto);
        pedido.setTotal(subtotal.add(impuesto).setScale(2, RoundingMode.HALF_UP));

        pedido.addHistorial(OrderHistory.builder()
            .estadoAnterior(null)
            .estadoNuevo(OrderStatus.PENDIENTE)
            .motivo("Pedido creado")
            .build());

        return OrderDto.from(repository.save(pedido));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto findById(Long id) {
        return OrderDto.from(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto findByCodigo(String codigo) {
        Order p = repository.findByCodigo(codigo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado: " + codigo));
        return OrderDto.from(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        return repository.findAll().stream().map(OrderDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findByEstado(OrderStatus estado) {
        return repository.findByEstado(estado).stream().map(OrderDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findByCliente(String idCliente) {
        return repository.findByIdCliente(idCliente).stream().map(OrderDto::from).toList();
    }

    @Override
    public OrderDto cambiarEstado(Long id, UpdateOrderState req) {
        Order pedido = buscar(id);
        OrderStatus actual = pedido.getEstado();
        OrderStatus nuevo = req.estado();

        if (!TRANSICIONES.getOrDefault(actual, Set.of()).contains(nuevo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Transición no permitida: " + actual + " → " + nuevo);
        }

        pedido.setEstado(nuevo);
        pedido.addHistorial(OrderHistory.builder()
            .estadoAnterior(actual)
            .estadoNuevo(nuevo)
            .motivo(req.motivo())
            .build());

        return OrderDto.from(repository.save(pedido));
    }

    private Order buscar(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado: " + id));
    }

    private String generarCodigo() {
        String fecha = LocalDate.now().format(FECHA);
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "PED-" + fecha + "-" + suffix;
    }
}
