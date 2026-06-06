package cl.smartlogix.envio.controller;

import cl.smartlogix.envio.dto.CarrierDto;
import cl.smartlogix.envio.dto.CrearTransportistaRequest;
import cl.smartlogix.envio.service.CarrierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/transportistas")
@RequiredArgsConstructor
public class CarrierController {

    private final CarrierService service;

    @PostMapping
    public ResponseEntity<CarrierDto> crear(@Valid @RequestBody CrearTransportistaRequest req) {
        CarrierDto creado = service.crear(req);
        return ResponseEntity.created(URI.create("/transportistas/" + creado.idTransportista())).body(creado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarrierDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<CarrierDto>> listar(@RequestParam(required = false) Boolean activo) {
        List<CarrierDto> data = Boolean.TRUE.equals(activo) ? service.findActivos() : service.findAll();
        return ResponseEntity.ok(data);
    }
}
