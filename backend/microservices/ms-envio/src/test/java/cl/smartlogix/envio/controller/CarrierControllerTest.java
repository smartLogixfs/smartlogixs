package cl.smartlogix.envio.controller;

import cl.smartlogix.envio.dto.CarrierDto;
import cl.smartlogix.envio.dto.CrearTransportistaRequest;
import cl.smartlogix.envio.service.CarrierService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarrierController.class)
class CarrierControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean CarrierService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void postTransportistasDevuelve201() throws Exception {
        var req = new CrearTransportistaRequest("Chilexpress", "96.756.430-3", "+56912345678");
        when(service.crear(any())).thenReturn(new CarrierDto(1L, "Chilexpress", "96.756.430-3", "+56912345678", true));

        mockMvc.perform(post("/transportistas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/transportistas/1"))
            .andExpect(jsonPath("$.nombre").value("Chilexpress"));
    }

    @Test
    void postTransportistasSinNombreDevuelve400() throws Exception {
        String body = """
            { "nombre": "" }
            """;
        mockMvc.perform(post("/transportistas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.nombre").exists());
    }

    @Test
    void getByIdDevuelve200() throws Exception {
        when(service.findById(1L)).thenReturn(new CarrierDto(1L, "Chilexpress", null, null, true));

        mockMvc.perform(get("/transportistas/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.idTransportista").value(1));
    }

    @Test
    void getByIdInexistenteDevuelve404() throws Exception {
        when(service.findById(99L)).thenThrow(
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Transportista no encontrado"));

        mockMvc.perform(get("/transportistas/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void listarSinFiltroLlamaFindAll() throws Exception {
        when(service.findAll()).thenReturn(List.of(new CarrierDto(1L, "Chilexpress", null, null, true)));

        mockMvc.perform(get("/transportistas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idTransportista").value(1));
    }

    @Test
    void listarConActivoTrueFiltra() throws Exception {
        when(service.findActivos()).thenReturn(List.of(new CarrierDto(1L, "Chilexpress", null, null, true)));

        mockMvc.perform(get("/transportistas").param("activo", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].activo").value(true));
    }
}
