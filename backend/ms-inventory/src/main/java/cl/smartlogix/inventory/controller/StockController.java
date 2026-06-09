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

    @GetMapping("/{idProducto}/{idBodega}")
    public ResponseEntity<StockDto> get(@PathVariable Long idProducto, @PathVariable Long idBodega) {
        return ResponseEntity.ok(service.get(idProducto, idBodega));
    }

    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<List<StockDto>> byProducto(@PathVariable Long idProducto) {
        return ResponseEntity.ok(service.findByProducto(idProducto));
    }

    @GetMapping("/producto/{idProducto}/disponible")
    public ResponseEntity<Map<String, Integer>> disponibleTotal(@PathVariable Long idProducto) {
        return ResponseEntity.ok(Map.of("disponible", service.disponibleTotal(idProducto)));
    }

    @GetMapping("/bajo")
    public ResponseEntity<List<StockDto>> stockBajo() {
        return ResponseEntity.ok(service.findConStockBajo());
    }

    @GetMapping("/{idStock}/historial")
    public ResponseEntity<List<StockMovementDto>> historial(@PathVariable Long idStock) {
        return ResponseEntity.ok(service.historial(idStock));
    }

    @PostMapping("/entrada")
    public ResponseEntity<StockDto> entrada(@Valid @RequestBody StockMovementRequest req) {
        return ResponseEntity.ok(service.entrada(req));
    }

    @PostMapping("/salida")
    public ResponseEntity<StockDto> salida(@Valid @RequestBody StockMovementRequest req) {
        return ResponseEntity.ok(service.salida(req));
    }

    @PostMapping("/reservar")
    public ResponseEntity<StockDto> reservar(@Valid @RequestBody StockMovementRequest req) {
        return ResponseEntity.ok(service.reservar(req));
    }

    @PostMapping("/liberar")
    public ResponseEntity<StockDto> liberar(@Valid @RequestBody StockMovementRequest req) {
        return ResponseEntity.ok(service.liberar(req));
    }
}
