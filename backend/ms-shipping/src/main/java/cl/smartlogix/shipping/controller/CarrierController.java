package cl.smartlogix.shipping.controller;

import cl.smartlogix.shipping.dto.CarrierDto;
import cl.smartlogix.shipping.dto.CreateCarrierRequest;
import cl.smartlogix.shipping.service.CarrierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/carriers")
@RequiredArgsConstructor
public class CarrierController {

    private final CarrierService service;

    @PostMapping
    public ResponseEntity<CarrierDto> create(@Valid @RequestBody CreateCarrierRequest req) {
        CarrierDto created = service.create(req);
        return ResponseEntity.created(URI.create("/carriers/" + created.carrierId())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarrierDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<CarrierDto>> list(@RequestParam(required = false) Boolean active) {
        List<CarrierDto> data = Boolean.TRUE.equals(active) ? service.findActive() : service.findAll();
        return ResponseEntity.ok(data);
    }
}
