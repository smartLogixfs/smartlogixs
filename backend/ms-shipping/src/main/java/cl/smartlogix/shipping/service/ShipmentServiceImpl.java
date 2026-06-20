package cl.smartlogix.shipping.service;

import cl.smartlogix.shipping.dto.UpdateShipmentRStatusRequest;
import cl.smartlogix.shipping.dto.AssingCarierRequest;
import cl.smartlogix.shipping.dto.CreateShipmentRequest;
import cl.smartlogix.shipping.dto.ShipmentDto;
import cl.smartlogix.shipping.dto.ShipmentTrackingDto;
import cl.smartlogix.shipping.model.Shipment;
import cl.smartlogix.shipping.model.ShipmentTracking;
import cl.smartlogix.shipping.model.ShipmentState;
import cl.smartlogix.shipping.model.Carrier;
import cl.smartlogix.shipping.repository.ShipmentRepository;
import cl.smartlogix.shipping.repository.ShipmentTrackingRepository;
import cl.smartlogix.shipping.repository.CarrierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Máquina de estados del envío
    private static final Map<ShipmentState, Set<ShipmentState>> TRANSITIONS = Map.of(
        ShipmentState.CREADO,     Set.of(ShipmentState.ASIGNADO, ShipmentState.INCIDENCIA),
        ShipmentState.ASIGNADO,   Set.of(ShipmentState.EN_RUTA, ShipmentState.INCIDENCIA),
        ShipmentState.EN_RUTA,    Set.of(ShipmentState.ENTREGADO, ShipmentState.INCIDENCIA),
        ShipmentState.INCIDENCIA, Set.of(ShipmentState.EN_RUTA, ShipmentState.ENTREGADO),
        ShipmentState.ENTREGADO,  Set.of()
    );

    private final ShipmentRepository shipmentRepository;
    private final CarrierRepository carrierRepository;
    private final ShipmentTrackingRepository trackingRepository;

    @Override
    public ShipmentDto create(CreateShipmentRequest req) {
        Shipment shipment = Shipment.builder()
            .orderId(req.orderId())
            .status(ShipmentState.CREADO)
            .destinationAddress(req.destinationAddress())
            .district(req.district())
            .region(req.region())
            .estimatedDate(req.estimatedDate())
            .trackingNumber(generateTracking())
            .build();
        shipment.addTracking(ShipmentTracking.builder()
            .status(ShipmentState.CREADO)
            .comment("Envío creado")
            .build());
        return ShipmentDto.from(shipmentRepository.save(shipment));
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentDto findById(Long id) {
        return ShipmentDto.from(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentDto findByTracking(String trackingNumber) {
        Shipment s = shipmentRepository.findByTrackingNumber(trackingNumber)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tracking no encontrado: " + trackingNumber));
        return ShipmentDto.from(s);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentDto> findByOrder(Long orderId) {
        return shipmentRepository.findByOrderId(orderId).stream().map(ShipmentDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentDto> findAll() {
        return shipmentRepository.findAll().stream().map(ShipmentDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentDto> findByStatus(ShipmentState status) {
        return shipmentRepository.findByStatus(status).stream().map(ShipmentDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentTrackingDto> history(Long shipmentId) {
        return trackingRepository.findByShipment_IdOrderByCreatedAtAsc(shipmentId).stream()
            .map(ShipmentTrackingDto::from).toList();
    }

    @Override
    public ShipmentDto assignCarrier(Long shipmentId, AssingCarierRequest req) {
        Shipment shipment = find(shipmentId);
        if (shipment.getStatus() != ShipmentState.CREADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Solo se puede asignar transportista en estado CREADO (actual: " + shipment.getStatus() + ")");
        }
        Carrier c = carrierRepository.findById(req.carrierId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Transportista no encontrado: " + req.carrierId()));
        if (!Boolean.TRUE.equals(c.getActive())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transportista inactivo: " + c.getName());
        }

        shipment.setCarrier(c);
        shipment.setStatus(ShipmentState.ASIGNADO);
        shipment.addTracking(ShipmentTracking.builder()
            .status(ShipmentState.ASIGNADO)
            .comment("Transportista asignado: " + c.getName())
            .build());
        return ShipmentDto.from(shipmentRepository.save(shipment));
    }

    @Override
    public ShipmentDto changeStatus(Long shipmentId, UpdateShipmentRStatusRequest req) {
        Shipment shipment = find(shipmentId);
        ShipmentState current = shipment.getStatus();
        ShipmentState next = req.status();

        if (!TRANSITIONS.getOrDefault(current, Set.of()).contains(next)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Transición no permitida: " + current + " → " + next);
        }

        shipment.setStatus(next);
        if (next == ShipmentState.ENTREGADO) {
            shipment.setDeliveryDate(OffsetDateTime.now());
        }
        shipment.addTracking(ShipmentTracking.builder()
            .status(next)
            .location(req.location())
            .comment(req.comment())
            .build());
        return ShipmentDto.from(shipmentRepository.save(shipment));
    }

    private Shipment find(Long id) {
        return shipmentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Envío no encontrado: " + id));
    }

    private String generateTracking() {
        String date = LocalDate.now().format(DATE_FORMAT);
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ENV-" + date + "-" + suffix;
    }
}
