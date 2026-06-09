package cl.smartlogix.inventory.controller;

import cl.smartlogix.inventory.dto.CreateWarehouseRequest;
import cl.smartlogix.inventory.dto.WarehouseDto;
import cl.smartlogix.inventory.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/bodegas")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService service;

    @PostMapping
    public ResponseEntity<WarehouseDto> crear(@Valid @RequestBody CreateWarehouseRequest req) {
        WarehouseDto creada = service.crear(req);
        return ResponseEntity.created(URI.create("/bodegas/" + creada.idBodega())).body(creada);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<WarehouseDto>> listar() {
        return ResponseEntity.ok(service.findAll());
    }
}
