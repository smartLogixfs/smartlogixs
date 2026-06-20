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
    public ResponseEntity<ShipmentDto> create(@Valid @RequestBody CreateShipmentRequest req) {
        ShipmentDto created = service.create(req);
        return ResponseEntity.created(URI.create("/shipments/" + created.shipmentId())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ShipmentDto> getByTracking(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(service.findByTracking(trackingNumber));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ShipmentDto>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(service.findByOrder(orderId));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentDto>> list(@RequestParam(required = false) ShipmentState status) {
        List<ShipmentDto> data = (status == null) ? service.findAll() : service.findByStatus(status);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{id}/tracking-history")
    public ResponseEntity<List<ShipmentTrackingDto>> history(@PathVariable Long id) {
        return ResponseEntity.ok(service.history(id));
    }

    @PatchMapping("/{id}/carrier")
    public ResponseEntity<ShipmentDto> assignCarrier(@PathVariable Long id,
                                                    @Valid @RequestBody AssingCarierRequest req) {
        return ResponseEntity.ok(service.assignCarrier(id, req));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ShipmentDto> changeStatus(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateShipmentRStatusRequest req) {
        return ResponseEntity.ok(service.changeStatus(id, req));
    }
}
