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

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Máquina de estados del envío
    private static final Map<ShipmentState, Set<ShipmentState>> TRANSICIONES = Map.of(
        ShipmentState.CREADO,     Set.of(ShipmentState.ASIGNADO, ShipmentState.INCIDENCIA),
        ShipmentState.ASIGNADO,   Set.of(ShipmentState.EN_RUTA, ShipmentState.INCIDENCIA),
        ShipmentState.EN_RUTA,    Set.of(ShipmentState.ENTREGADO, ShipmentState.INCIDENCIA),
        ShipmentState.INCIDENCIA, Set.of(ShipmentState.EN_RUTA, ShipmentState.ENTREGADO),
        ShipmentState.ENTREGADO,  Set.of()
    );

    private final ShipmentRepository envioRepository;
    private final CarrierRepository transportistaRepository;
    private final ShipmentTrackingRepository seguimientoRepository;

    @Override
    public ShipmentDto crear(CreateShipmentRequest req) {
        Shipment envio = Shipment.builder()
            .idPedido(req.idPedido())
            .estado(ShipmentState.CREADO)
            .direccionDestino(req.direccionDestino())
            .comuna(req.comuna())
            .region(req.region())
            .fechaEstimada(req.fechaEstimada())
            .trackingNumber(generarTracking())
            .build();
        envio.addSeguimiento(ShipmentTracking.builder()
            .estado(ShipmentState.CREADO)
            .comentario("Envío creado")
            .build());
        return ShipmentDto.from(envioRepository.save(envio));
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentDto findById(Long id) {
        return ShipmentDto.from(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentDto findByTracking(String trackingNumber) {
        Shipment e = envioRepository.findByTrackingNumber(trackingNumber)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tracking no encontrado: " + trackingNumber));
        return ShipmentDto.from(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentDto> findByPedido(Long idPedido) {
        return envioRepository.findByIdPedido(idPedido).stream().map(ShipmentDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentDto> findAll() {
        return envioRepository.findAll().stream().map(ShipmentDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentDto> findByEstado(ShipmentState estado) {
        return envioRepository.findByEstado(estado).stream().map(ShipmentDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentTrackingDto> historial(Long idEnvio) {
        return seguimientoRepository.findByEnvio_IdEnvioOrderByCreatedAtAsc(idEnvio).stream()
            .map(ShipmentTrackingDto::from).toList();
    }

    @Override
    public ShipmentDto asignarTransportista(Long idEnvio, AssingCarierRequest req) {
        Shipment envio = buscar(idEnvio);
        if (envio.getEstado() != ShipmentState.CREADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Solo se puede asignar transportista en estado CREADO (actual: " + envio.getEstado() + ")");
        }
        Carrier t = transportistaRepository.findById(req.idTransportista())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Transportista no encontrado: " + req.idTransportista()));
        if (!Boolean.TRUE.equals(t.getActivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transportista inactivo: " + t.getNombre());
        }

        envio.setTransportista(t);
        envio.setEstado(ShipmentState.ASIGNADO);
        envio.addSeguimiento(ShipmentTracking.builder()
            .estado(ShipmentState.ASIGNADO)
            .comentario("Transportista asignado: " + t.getNombre())
            .build());
        return ShipmentDto.from(envioRepository.save(envio));
    }

    @Override
    public ShipmentDto cambiarEstado(Long idEnvio, UpdateShipmentRStatusRequest req) {
        Shipment envio = buscar(idEnvio);
        ShipmentState actual = envio.getEstado();
        ShipmentState nuevo = req.estado();

        if (!TRANSICIONES.getOrDefault(actual, Set.of()).contains(nuevo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Transición no permitida: " + actual + " → " + nuevo);
        }

        envio.setEstado(nuevo);
        if (nuevo == ShipmentState.ENTREGADO) {
            envio.setFechaEntrega(OffsetDateTime.now());
        }
        envio.addSeguimiento(ShipmentTracking.builder()
            .estado(nuevo)
            .ubicacion(req.ubicacion())
            .comentario(req.comentario())
            .build());
        return ShipmentDto.from(envioRepository.save(envio));
    }

    private Shipment buscar(Long id) {
        return envioRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Envío no encontrado: " + id));
    }

    private String generarTracking() {
        String fecha = LocalDate.now().format(FECHA);
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ENV-" + fecha + "-" + suffix;
    }
}
