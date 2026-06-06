package cl.smartlogix.pedido.controller;

import cl.smartlogix.pedido.dto.ActualizarEstadoRequest;
import cl.smartlogix.pedido.dto.CrearPedidoRequest;
import cl.smartlogix.pedido.dto.OrderDto;
import cl.smartlogix.pedido.model.EstadoPedido;
import cl.smartlogix.pedido.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

// Path: /pedidos — el dominio del proyecto es Pedidos (es lo que enruta el BFF/KrakenD).
// El nombre de la clase se mantiene como OrderController por convención del scaffolding.
@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    public ResponseEntity<OrderDto> crear(@Valid @RequestBody CrearPedidoRequest req) {
        OrderDto creado = service.crear(req);
        return ResponseEntity
            .created(URI.create("/pedidos/" + creado.idPedido()))
            .body(creado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<OrderDto> getByCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(service.findByCodigo(codigo));
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<OrderDto>> getByCliente(@PathVariable String idCliente) {
        return ResponseEntity.ok(service.findByCliente(idCliente));
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> listar(@RequestParam(required = false) EstadoPedido estado) {
        List<OrderDto> data = (estado == null) ? service.findAll() : service.findByEstado(estado);
        return ResponseEntity.ok(data);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<OrderDto> cambiarEstado(@PathVariable Long id,
                                                  @Valid @RequestBody ActualizarEstadoRequest req) {
        return ResponseEntity.ok(service.cambiarEstado(id, req));
    }
}
