package cl.smartlogix.shipping.controller;

import cl.smartlogix.shipping.dto.ActualizarEstadoEnvioRequest;
import cl.smartlogix.shipping.dto.AsignarTransportistaRequest;
import cl.smartlogix.shipping.dto.CrearEnvioRequest;
import cl.smartlogix.shipping.dto.ShipmentDto;
import cl.smartlogix.shipping.dto.ShipmentTrackingDto;
import cl.smartlogix.shipping.model.EstadoEnvio;
import cl.smartlogix.shipping.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/envios")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService service;

    @PostMapping
    public ResponseEntity<ShipmentDto> crear(@Valid @RequestBody CrearEnvioRequest req) {
        ShipmentDto creado = service.crear(req);
        return ResponseEntity.created(URI.create("/envios/" + creado.idEnvio())).body(creado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ShipmentDto> getByTracking(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(service.findByTracking(trackingNumber));
    }

    @GetMapping("/pedido/{idPedido}")
    public ResponseEntity<List<ShipmentDto>> getByPedido(@PathVariable Long idPedido) {
        return ResponseEntity.ok(service.findByPedido(idPedido));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentDto>> listar(@RequestParam(required = false) EstadoEnvio estado) {
        List<ShipmentDto> data = (estado == null) ? service.findAll() : service.findByEstado(estado);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{id}/seguimiento")
    public ResponseEntity<List<ShipmentTrackingDto>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(service.historial(id));
    }

    @PatchMapping("/{id}/transportista")
    public ResponseEntity<ShipmentDto> asignar(@PathVariable Long id,
                                               @Valid @RequestBody AsignarTransportistaRequest req) {
        return ResponseEntity.ok(service.asignarTransportista(id, req));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ShipmentDto> cambiarEstado(@PathVariable Long id,
                                                     @Valid @RequestBody ActualizarEstadoEnvioRequest req) {
        return ResponseEntity.ok(service.cambiarEstado(id, req));
    }
}
