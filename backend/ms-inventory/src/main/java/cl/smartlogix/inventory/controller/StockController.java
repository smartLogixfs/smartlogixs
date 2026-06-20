package cl.smartlogix.inventory.controller;

import cl.smartlogix.inventory.dto.StockMovementRequest;
import cl.smartlogix.inventory.dto.StockDto;
import cl.smartlogix.inventory.dto.StockMovementDto;
import cl.smartlogix.inventory.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService service;

    @GetMapping("/{productId}/{warehouseId}")
    public ResponseEntity<StockDto> get(@PathVariable Long productId, @PathVariable Long warehouseId) {
        return ResponseEntity.ok(service.get(productId, warehouseId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockDto>> byProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(service.findByProduct(productId));
    }

    @GetMapping("/product/{productId}/available")
    public ResponseEntity<Map<String, Integer>> totalAvailable(@PathVariable Long productId) {
        return ResponseEntity.ok(Map.of("available", service.totalAvailable(productId)));
    }

    @GetMapping("/low")
    public ResponseEntity<List<StockDto>> lowStock() {
        return ResponseEntity.ok(service.findLowStock());
    }

    @GetMapping("/{stockId}/history")
    public ResponseEntity<List<StockMovementDto>> history(@PathVariable Long stockId) {
        return ResponseEntity.ok(service.history(stockId));
    }

    @PostMapping("/in")
    public ResponseEntity<StockDto> stockIn(@Valid @RequestBody StockMovementRequest req) {
        return ResponseEntity.ok(service.stockIn(req));
    }

    @PostMapping("/out")
    public ResponseEntity<StockDto> stockOut(@Valid @RequestBody StockMovementRequest req) {
        return ResponseEntity.ok(service.stockOut(req));
    }

    @PostMapping("/reserve")
    public ResponseEntity<StockDto> reserve(@Valid @RequestBody StockMovementRequest req) {
        return ResponseEntity.ok(service.reserve(req));
    }

    @PostMapping("/release")
    public ResponseEntity<StockDto> release(@Valid @RequestBody StockMovementRequest req) {
        return ResponseEntity.ok(service.release(req));
    }
}
