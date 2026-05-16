package cl.smartlogix.inventario.controller;

import cl.smartlogix.inventario.dto.MovimientoRequest;
import cl.smartlogix.inventario.dto.StockDto;
import cl.smartlogix.inventario.service.StockService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockController.class)
class StockControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean StockService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getDevuelveStock() throws Exception {
        when(service.get(1L, 1L)).thenReturn(stockEjemplo(100, 0));

        mockMvc.perform(get("/stock/1/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.disponible").value(100));
    }

    @Test
    void getInexistenteDevuelve404() throws Exception {
        when(service.get(99L, 99L)).thenThrow(
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock no encontrado"));

        mockMvc.perform(get("/stock/99/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void byProductoDevuelveLista() throws Exception {
        when(service.findByProducto(1L)).thenReturn(List.of(stockEjemplo(100, 5)));

        mockMvc.perform(get("/stock/producto/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].cantReservada").value(5));
    }

    @Test
    void disponibleTotalDevuelveMapa() throws Exception {
        when(service.disponibleTotal(1L)).thenReturn(95);

        mockMvc.perform(get("/stock/producto/1/disponible"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.disponible").value(95));
    }

    @Test
    void stockBajoDevuelveLista() throws Exception {
        when(service.findConStockBajo()).thenReturn(List.of(stockEjemplo(2, 0)));

        mockMvc.perform(get("/stock/bajo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].disponible").value(2));
    }

    @Test
    void historialDevuelveLista() throws Exception {
        when(service.historial(1L)).thenReturn(List.of());

        mockMvc.perform(get("/stock/1/historial"))
            .andExpect(status().isOk());
    }

    @Test
    void entradaDevuelve200() throws Exception {
        var req = new MovimientoRequest(1L, 1L, 10, null);
        when(service.entrada(any())).thenReturn(stockEjemplo(110, 0));

        mockMvc.perform(post("/stock/entrada")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.disponible").value(110));
    }

    @Test
    void salidaDevuelve200() throws Exception {
        var req = new MovimientoRequest(1L, 1L, 5, "PED-001");
        when(service.salida(any())).thenReturn(stockEjemplo(95, 0));

        mockMvc.perform(post("/stock/salida")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk());
    }

    @Test
    void salidaInsuficienteDevuelve409() throws Exception {
        var req = new MovimientoRequest(1L, 1L, 1000, "PED-001");
        when(service.salida(any())).thenThrow(
            new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente"));

        mockMvc.perform(post("/stock/salida")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Stock insuficiente"));
    }

    @Test
    void reservarDevuelve200() throws Exception {
        var req = new MovimientoRequest(1L, 1L, 3, "PED-001");
        when(service.reservar(any())).thenReturn(stockEjemplo(100, 3));

        mockMvc.perform(post("/stock/reservar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cantReservada").value(3));
    }

    @Test
    void liberarDevuelve200() throws Exception {
        var req = new MovimientoRequest(1L, 1L, 2, "PED-001");
        when(service.liberar(any())).thenReturn(stockEjemplo(100, 1));

        mockMvc.perform(post("/stock/liberar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk());
    }

    @Test
    void postSinCantidadDevuelve400() throws Exception {
        String body = """
            { "idProducto": 1, "idBodega": 1 }
            """;
        mockMvc.perform(post("/stock/entrada")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.cantidad").exists());
    }

    private StockDto stockEjemplo(int cantidad, int reservada) {
        return new StockDto(
            1L, 1L, "SKU-001",
            1L, "Bodega Central",
            cantidad, reservada, cantidad - reservada, 10,
            OffsetDateTime.parse("2026-05-13T10:00:00Z")
        );
    }
}
