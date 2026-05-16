package cl.smartlogix.inventario.controller;

import cl.smartlogix.inventario.dto.CrearBodegaRequest;
import cl.smartlogix.inventario.dto.WarehouseDto;
import cl.smartlogix.inventario.service.WarehouseService;
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

@WebMvcTest(WarehouseController.class)
class WarehouseControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean WarehouseService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void postBodegasDevuelve201() throws Exception {
        var req = new CrearBodegaRequest("Bodega Central", "Santiago");
        when(service.crear(any())).thenReturn(new WarehouseDto(1L, "Bodega Central", "Santiago", true));

        mockMvc.perform(post("/bodegas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/bodegas/1"))
            .andExpect(jsonPath("$.nombre").value("Bodega Central"));
    }

    @Test
    void postBodegasSinNombreDevuelve400() throws Exception {
        String body = """
            { "nombre": "", "ubicacion": "Santiago" }
            """;
        mockMvc.perform(post("/bodegas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.nombre").exists());
    }

    @Test
    void getByIdDevuelve200() throws Exception {
        when(service.findById(1L)).thenReturn(new WarehouseDto(1L, "Bodega Central", "Santiago", true));

        mockMvc.perform(get("/bodegas/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.idBodega").value(1));
    }

    @Test
    void getByIdInexistenteDevuelve404() throws Exception {
        when(service.findById(99L)).thenThrow(
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Bodega no encontrada"));

        mockMvc.perform(get("/bodegas/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void listarDevuelveArray() throws Exception {
        when(service.findAll()).thenReturn(List.of(new WarehouseDto(1L, "Bodega Central", "Santiago", true)));

        mockMvc.perform(get("/bodegas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idBodega").value(1));
    }
}
