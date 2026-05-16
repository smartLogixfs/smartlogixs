package cl.smartlogix.envio.controller;

import cl.smartlogix.envio.dto.ActualizarEstadoEnvioRequest;
import cl.smartlogix.envio.dto.AsignarTransportistaRequest;
import cl.smartlogix.envio.dto.CrearEnvioRequest;
import cl.smartlogix.envio.dto.ShipmentDto;
import cl.smartlogix.envio.model.EstadoEnvio;
import cl.smartlogix.envio.service.ShipmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShipmentController.class)
class ShipmentControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ShipmentService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void postEnviosDevuelve201() throws Exception {
        var req = new CrearEnvioRequest(1L, "Av. Providencia 1234", "Providencia", "RM", null);
        when(service.crear(any())).thenReturn(dtoEjemplo(EstadoEnvio.CREADO));

        mockMvc.perform(post("/envios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/envios/1"))
            .andExpect(jsonPath("$.trackingNumber").value("ENV-20260513-AB12CD34"));
    }

    @Test
    void postEnviosSinDireccionDevuelve400() throws Exception {
        String body = """
            { "idPedido": 1, "direccionDestino": "" }
            """;
        mockMvc.perform(post("/envios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.direccionDestino").exists());
    }

    @Test
    void getByIdDevuelve200() throws Exception {
        when(service.findById(1L)).thenReturn(dtoEjemplo(EstadoEnvio.CREADO));

        mockMvc.perform(get("/envios/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.idEnvio").value(1));
    }

    @Test
    void getByIdInexistenteDevuelve404() throws Exception {
        when(service.findById(99L)).thenThrow(
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Envío no encontrado"));

        mockMvc.perform(get("/envios/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getByTrackingDevuelve200() throws Exception {
        when(service.findByTracking("ENV-20260513-AB12CD34")).thenReturn(dtoEjemplo(EstadoEnvio.CREADO));

        mockMvc.perform(get("/envios/tracking/ENV-20260513-AB12CD34"))
            .andExpect(status().isOk());
    }

    @Test
    void getByPedidoDevuelveLista() throws Exception {
        when(service.findByPedido(1L)).thenReturn(List.of(dtoEjemplo(EstadoEnvio.CREADO)));

        mockMvc.perform(get("/envios/pedido/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idPedido").value(1));
    }

    @Test
    void listarSinFiltroLlamaFindAll() throws Exception {
        when(service.findAll()).thenReturn(List.of(dtoEjemplo(EstadoEnvio.CREADO)));

        mockMvc.perform(get("/envios"))
            .andExpect(status().isOk());
    }

    @Test
    void listarConEstadoFiltra() throws Exception {
        when(service.findByEstado(EstadoEnvio.EN_RUTA)).thenReturn(List.of());

        mockMvc.perform(get("/envios").param("estado", "EN_RUTA"))
            .andExpect(status().isOk());
    }

    @Test
    void historialDevuelveLista() throws Exception {
        when(service.historial(1L)).thenReturn(List.of());

        mockMvc.perform(get("/envios/1/seguimiento"))
            .andExpect(status().isOk());
    }

    @Test
    void patchTransportistaDevuelve200() throws Exception {
        var req = new AsignarTransportistaRequest(2L);
        when(service.asignarTransportista(eq(1L), any())).thenReturn(dtoEjemplo(EstadoEnvio.ASIGNADO));

        mockMvc.perform(patch("/envios/1/transportista")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk());
    }

    @Test
    void patchEstadoIlegalDevuelve409() throws Exception {
        var req = new ActualizarEstadoEnvioRequest(EstadoEnvio.CREADO, null, null);
        when(service.cambiarEstado(eq(1L), any())).thenThrow(
            new ResponseStatusException(HttpStatus.CONFLICT, "Transición no permitida"));

        mockMvc.perform(patch("/envios/1/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Transición no permitida"));
    }

    private ShipmentDto dtoEjemplo(EstadoEnvio estado) {
        OffsetDateTime now = OffsetDateTime.parse("2026-05-13T10:00:00Z");
        return new ShipmentDto(
            1L, 1L, null, null,
            "ENV-20260513-AB12CD34", estado,
            "Av. Providencia 1234", "Providencia", "RM",
            null, null,
            List.of(),
            now, now
        );
    }
}
