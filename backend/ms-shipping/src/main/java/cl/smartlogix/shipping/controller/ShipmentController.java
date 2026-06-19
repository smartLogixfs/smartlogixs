package cl.smartlogix.shipping.controller;

import cl.smartlogix.shipping.dto.UpdateShipmentRStatusRequest;
import cl.smartlogix.shipping.dto.AssingCarierRequest;
import cl.smartlogix.shipping.dto.CreateShipmentRequest;
import cl.smartlogix.shipping.dto.ShipmentDto;
import cl.smartlogix.shipping.dto.ShipmentTrackingDto;
import cl.smartlogix.shipping.model.ShipmentState;
import cl.smartlogix.shipping.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService service;

    @PostMapping
    public ResponseEntity<ShipmentDto> crear(@Valid @RequestBody CreateShipmentRequest req) {
        ShipmentDto creado = service.crear(req);
        return ResponseEntity.created(URI.create("/shipments/" + creado.idEnvio())).body(creado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ShipmentDto> getByTracking(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(service.findByTracking(trackingNumber));
    }

    @GetMapping("/order/{idPedido}")
    public ResponseEntity<List<ShipmentDto>> getByPedido(@PathVariable Long idPedido) {
        return ResponseEntity.ok(service.findByPedido(idPedido));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentDto>> listar(@RequestParam(required = false) ShipmentState estado) {
        List<ShipmentDto> data = (estado == null) ? service.findAll() : service.findByEstado(estado);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{id}/tracking-history")
    public ResponseEntity<List<ShipmentTrackingDto>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(service.historial(id));
    }

    @PatchMapping("/{id}/carrier")
    public ResponseEntity<ShipmentDto> asignar(@PathVariable Long id,
                                               @Valid @RequestBody AssingCarierRequest req) {
        return ResponseEntity.ok(service.asignarTransportista(id, req));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ShipmentDto> cambiarEstado(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateShipmentRStatusRequest req) {
        return ResponseEntity.ok(service.cambiarEstado(id, req));
    }
}
