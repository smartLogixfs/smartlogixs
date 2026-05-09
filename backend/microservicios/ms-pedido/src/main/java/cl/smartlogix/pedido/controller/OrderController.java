package cl.smartlogix.pedido.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.smartlogix.pedido.dto.OrderDto;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @GetMapping("/{id}")
    ResponseEntity<OrderDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new OrderDto(100L));
    }
}
