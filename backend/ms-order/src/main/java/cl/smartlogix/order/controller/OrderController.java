package cl.smartlogix.order.controller;

import cl.smartlogix.order.dto.UpdateOrderState;
import cl.smartlogix.order.dto.CreateOrderRequest;
import cl.smartlogix.order.dto.OrderDto;
import cl.smartlogix.order.model.OrderStatus;
import cl.smartlogix.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    public ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequest req) {
        OrderDto created = service.create(req);
        return ResponseEntity
            .created(URI.create("/orders/" + created.orderId()))
            .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<OrderDto> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(service.findByCode(code));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDto>> getByCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(service.findByCustomer(customerId));
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> list(@RequestParam(required = false) OrderStatus status) {
        List<OrderDto> data = (status == null) ? service.findAll() : service.findByStatus(status);
        return ResponseEntity.ok(data);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDto> changeStatus(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateOrderState req) {
        return ResponseEntity.ok(service.changeStatus(id, req));
    }
}
