package cl.smartlogix.order.service;

import cl.smartlogix.order.dto.ActualizarEstadoRequest;
import cl.smartlogix.order.dto.CrearPedidoRequest;
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
    private static final Map<EstadoPedido, Set<EstadoPedido>> TRANSICIONES = Map.of(
        EstadoPedido.PENDIENTE,      Set.of(EstadoPedido.APROBADO, EstadoPedido.RECHAZADO, EstadoPedido.CANCELADO),
        EstadoPedido.APROBADO,       Set.of(EstadoPedido.EN_PREPARACION, EstadoPedido.CANCELADO),
        EstadoPedido.EN_PREPARACION, Set.of(EstadoPedido.ENVIADO, EstadoPedido.CANCELADO),
        EstadoPedido.ENVIADO,        Set.of(EstadoPedido.ENTREGADO),
        EstadoPedido.ENTREGADO,      Set.of(),
        EstadoPedido.RECHAZADO,      Set.of(),
        EstadoPedido.CANCELADO,      Set.of()
    );

    private final OrderRepository repository;

    @Override
    public OrderDto crear(CrearPedidoRequest req) {
        if (req.items() == null || req.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido debe tener al menos un ítem");
        }

        Pedido pedido = Pedido.builder()
            .codigo(generarCodigo())
            .tipo(req.tipo() != null ? req.tipo() : TipoPedido.ESTANDAR)
            .estado(EstadoPedido.PENDIENTE)
            .idCliente(req.idCliente())
            .idMarketplace(req.idMarketplace())
            .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CrearPedidoRequest.ItemRequest it : req.items()) {
            BigDecimal sub = it.precioUnitario().multiply(BigDecimal.valueOf(it.cantidad()));
            PedidoItem item = PedidoItem.builder()
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

        pedido.addHistorial(PedidoHistorial.builder()
            .estadoAnterior(null)
            .estadoNuevo(EstadoPedido.PENDIENTE)
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
        Pedido p = repository.findByCodigo(codigo)
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
    public List<OrderDto> findByEstado(EstadoPedido estado) {
        return repository.findByEstado(estado).stream().map(OrderDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findByCliente(String idCliente) {
        return repository.findByIdCliente(idCliente).stream().map(OrderDto::from).toList();
    }

    @Override
    public OrderDto cambiarEstado(Long id, ActualizarEstadoRequest req) {
        Pedido pedido = buscar(id);
        EstadoPedido actual = pedido.getEstado();
        EstadoPedido nuevo = req.estado();

        if (!TRANSICIONES.getOrDefault(actual, Set.of()).contains(nuevo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Transición no permitida: " + actual + " → " + nuevo);
        }

        pedido.setEstado(nuevo);
        pedido.addHistorial(PedidoHistorial.builder()
            .estadoAnterior(actual)
            .estadoNuevo(nuevo)
            .motivo(req.motivo())
            .build());

        return OrderDto.from(repository.save(pedido));
    }

    private Pedido buscar(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado: " + id));
    }

    private String generarCodigo() {
        String fecha = LocalDate.now().format(FECHA);
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "PED-" + fecha + "-" + suffix;
    }
}
