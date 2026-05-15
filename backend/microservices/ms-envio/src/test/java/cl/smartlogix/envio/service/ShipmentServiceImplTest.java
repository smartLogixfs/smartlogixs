package cl.smartlogix.envio.service;

import cl.smartlogix.envio.dto.ActualizarEstadoEnvioRequest;
import cl.smartlogix.envio.dto.AsignarTransportistaRequest;
import cl.smartlogix.envio.dto.CrearEnvioRequest;
import cl.smartlogix.envio.dto.ShipmentDto;
import cl.smartlogix.envio.model.Envio;
import cl.smartlogix.envio.model.EstadoEnvio;
import cl.smartlogix.envio.model.Transportista;
import cl.smartlogix.envio.repository.EnvioRepository;
import cl.smartlogix.envio.repository.EnvioSeguimientoRepository;
import cl.smartlogix.envio.repository.TransportistaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceImplTest {

    @Mock EnvioRepository envioRepository;
    @Mock TransportistaRepository transportistaRepository;
    @Mock EnvioSeguimientoRepository seguimientoRepository;
    @InjectMocks ShipmentServiceImpl service;

    @Test
    void crearGeneraTrackingYDejaEnCreadoConSeguimientoInicial() {
        var req = new CrearEnvioRequest(7L, "Av. Providencia 1234", "Providencia", "RM", null);
        when(envioRepository.save(any(Envio.class))).thenAnswer(i -> i.getArgument(0));

        ShipmentDto result = service.crear(req);

        assertThat(result.estado()).isEqualTo(EstadoEnvio.CREADO);
        assertThat(result.idPedido()).isEqualTo(7L);
        assertThat(result.trackingNumber()).startsWith("ENV-").hasSize(21); // ENV- + yyyyMMdd + - + 8 = 21
        assertThat(result.seguimiento()).hasSize(1);
        assertThat(result.seguimiento().get(0).estado()).isEqualTo(EstadoEnvio.CREADO);
        assertThat(result.seguimiento().get(0).comentario()).isEqualTo("Envío creado");
    }

    @Test
    void asignarTransportistaDesdeCreadoConActivoTransicionaAAsignado() {
        Envio envio = envioEnEstado(EstadoEnvio.CREADO);
        Transportista t = Transportista.builder()
            .idTransportista(1L).nombre("Chilexpress").activo(true).build();
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));
        when(transportistaRepository.findById(1L)).thenReturn(Optional.of(t));
        when(envioRepository.save(any(Envio.class))).thenAnswer(i -> i.getArgument(0));

        ShipmentDto result = service.asignarTransportista(1L, new AsignarTransportistaRequest(1L));

        assertThat(result.estado()).isEqualTo(EstadoEnvio.ASIGNADO);
        assertThat(result.transportistaNombre()).isEqualTo("Chilexpress");
    }

    @Test
    void asignarTransportistaConTransportistaInactivoLanzaConflict() {
        Envio envio = envioEnEstado(EstadoEnvio.CREADO);
        Transportista t = Transportista.builder()
            .idTransportista(1L).nombre("Inactivo").activo(false).build();
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));
        when(transportistaRepository.findById(1L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> service.asignarTransportista(1L, new AsignarTransportistaRequest(1L)))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Transportista inactivo");
    }

    @Test
    void asignarTransportistaDesdeEstadoDistintoDeCreadoLanzaConflict() {
        Envio envio = envioEnEstado(EstadoEnvio.EN_RUTA);
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));

        assertThatThrownBy(() -> service.asignarTransportista(1L, new AsignarTransportistaRequest(1L)))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Solo se puede asignar transportista en estado CREADO");
    }

    @Test
    void cambiarEstadoTransicionPermitidaActualizaYAgregaSeguimiento() {
        Envio envio = envioEnEstado(EstadoEnvio.ASIGNADO);
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenAnswer(i -> i.getArgument(0));

        ShipmentDto result = service.cambiarEstado(1L,
            new ActualizarEstadoEnvioRequest(EstadoEnvio.EN_RUTA, "Centro de distribución", "Salió"));

        assertThat(result.estado()).isEqualTo(EstadoEnvio.EN_RUTA);
        assertThat(result.fechaEntrega()).isNull();
    }

    @Test
    void cambiarEstadoAEntregadoSeteaFechaEntrega() {
        Envio envio = envioEnEstado(EstadoEnvio.EN_RUTA);
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenAnswer(i -> i.getArgument(0));

        ShipmentDto result = service.cambiarEstado(1L,
            new ActualizarEstadoEnvioRequest(EstadoEnvio.ENTREGADO, null, "Entregado al cliente"));

        assertThat(result.estado()).isEqualTo(EstadoEnvio.ENTREGADO);
        assertThat(result.fechaEntrega()).isNotNull();
    }

    @Test
    void cambiarEstadoTransicionInvalidaLanzaConflict() {
        Envio envio = envioEnEstado(EstadoEnvio.CREADO);
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));

        // CREADO no puede ir directo a ENTREGADO (debe pasar por ASIGNADO → EN_RUTA)
        assertThatThrownBy(() -> service.cambiarEstado(1L,
            new ActualizarEstadoEnvioRequest(EstadoEnvio.ENTREGADO, null, null)))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Transición no permitida");
    }

    @Test
    void incidenciaPuedeReintentarsAEnRuta() {
        Envio envio = envioEnEstado(EstadoEnvio.INCIDENCIA);
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenAnswer(i -> i.getArgument(0));

        ShipmentDto result = service.cambiarEstado(1L,
            new ActualizarEstadoEnvioRequest(EstadoEnvio.EN_RUTA, null, "Reintento"));

        assertThat(result.estado()).isEqualTo(EstadoEnvio.EN_RUTA);
    }

    @Test
    void findByTrackingInexistenteLanzaNotFound() {
        when(envioRepository.findByTrackingNumber("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByTracking("MISSING"))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Tracking no encontrado");
    }

    private Envio envioEnEstado(EstadoEnvio estado) {
        return Envio.builder()
            .idEnvio(1L)
            .idPedido(7L)
            .trackingNumber("ENV-20260513-ABCDEF12")
            .estado(estado)
            .direccionDestino("Av. Test 123")
            .build();
    }
}
