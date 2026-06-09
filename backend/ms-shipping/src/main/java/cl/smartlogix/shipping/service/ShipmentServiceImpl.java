package cl.smartlogix.shipping.service;

import cl.smartlogix.shipping.dto.ActualizarEstadoEnvioRequest;
import cl.smartlogix.shipping.dto.AsignarTransportistaRequest;
import cl.smartlogix.shipping.dto.CrearEnvioRequest;
import cl.smartlogix.shipping.dto.ShipmentDto;
import cl.smartlogix.shipping.dto.ShipmentTrackingDto;
import cl.smartlogix.shipping.model.Envio;
import cl.smartlogix.shipping.model.EnvioSeguimiento;
import cl.smartlogix.shipping.model.EstadoEnvio;
import cl.smartlogix.shipping.model.Transportista;
import cl.smartlogix.shipping.repository.EnvioRepository;
import cl.smartlogix.shipping.repository.EnvioSeguimientoRepository;
import cl.smartlogix.shipping.repository.TransportistaRepository;
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
    private static final Map<EstadoEnvio, Set<EstadoEnvio>> TRANSICIONES = Map.of(
        EstadoEnvio.CREADO,     Set.of(EstadoEnvio.ASIGNADO, EstadoEnvio.INCIDENCIA),
        EstadoEnvio.ASIGNADO,   Set.of(EstadoEnvio.EN_RUTA, EstadoEnvio.INCIDENCIA),
        EstadoEnvio.EN_RUTA,    Set.of(EstadoEnvio.ENTREGADO, EstadoEnvio.INCIDENCIA),
        EstadoEnvio.INCIDENCIA, Set.of(EstadoEnvio.EN_RUTA, EstadoEnvio.ENTREGADO),
        EstadoEnvio.ENTREGADO,  Set.of()
    );

    private final EnvioRepository envioRepository;
    private final TransportistaRepository transportistaRepository;
    private final EnvioSeguimientoRepository seguimientoRepository;

    @Override
    public ShipmentDto crear(CrearEnvioRequest req) {
        Envio envio = Envio.builder()
            .idPedido(req.idPedido())
            .estado(EstadoEnvio.CREADO)
            .direccionDestino(req.direccionDestino())
            .comuna(req.comuna())
            .region(req.region())
            .fechaEstimada(req.fechaEstimada())
            .trackingNumber(generarTracking())
            .build();
        envio.addSeguimiento(EnvioSeguimiento.builder()
            .estado(EstadoEnvio.CREADO)
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
        Envio e = envioRepository.findByTrackingNumber(trackingNumber)
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
    public List<ShipmentDto> findByEstado(EstadoEnvio estado) {
        return envioRepository.findByEstado(estado).stream().map(ShipmentDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentTrackingDto> historial(Long idEnvio) {
        return seguimientoRepository.findByEnvio_IdEnvioOrderByCreatedAtAsc(idEnvio).stream()
            .map(ShipmentTrackingDto::from).toList();
    }

    @Override
    public ShipmentDto asignarTransportista(Long idEnvio, AsignarTransportistaRequest req) {
        Envio envio = buscar(idEnvio);
        if (envio.getEstado() != EstadoEnvio.CREADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Solo se puede asignar transportista en estado CREADO (actual: " + envio.getEstado() + ")");
        }
        Transportista t = transportistaRepository.findById(req.idTransportista())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Transportista no encontrado: " + req.idTransportista()));
        if (!Boolean.TRUE.equals(t.getActivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transportista inactivo: " + t.getNombre());
        }

        envio.setTransportista(t);
        envio.setEstado(EstadoEnvio.ASIGNADO);
        envio.addSeguimiento(EnvioSeguimiento.builder()
            .estado(EstadoEnvio.ASIGNADO)
            .comentario("Transportista asignado: " + t.getNombre())
            .build());
        return ShipmentDto.from(envioRepository.save(envio));
    }

    @Override
    public ShipmentDto cambiarEstado(Long idEnvio, ActualizarEstadoEnvioRequest req) {
        Envio envio = buscar(idEnvio);
        EstadoEnvio actual = envio.getEstado();
        EstadoEnvio nuevo = req.estado();

        if (!TRANSICIONES.getOrDefault(actual, Set.of()).contains(nuevo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Transición no permitida: " + actual + " → " + nuevo);
        }

        envio.setEstado(nuevo);
        if (nuevo == EstadoEnvio.ENTREGADO) {
            envio.setFechaEntrega(OffsetDateTime.now());
        }
        envio.addSeguimiento(EnvioSeguimiento.builder()
            .estado(nuevo)
            .ubicacion(req.ubicacion())
            .comentario(req.comentario())
            .build());
        return ShipmentDto.from(envioRepository.save(envio));
    }

    private Envio buscar(Long id) {
        return envioRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Envío no encontrado: " + id));
    }

    private String generarTracking() {
        String fecha = LocalDate.now().format(FECHA);
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ENV-" + fecha + "-" + suffix;
    }
}
