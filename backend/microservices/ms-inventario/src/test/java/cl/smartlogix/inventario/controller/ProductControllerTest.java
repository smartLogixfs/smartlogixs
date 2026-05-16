package cl.smartlogix.inventario.controller;

import cl.smartlogix.inventario.dto.ActualizarProductoRequest;
import cl.smartlogix.inventario.dto.CrearProductoRequest;
import cl.smartlogix.inventario.dto.ProductDto;
import cl.smartlogix.inventario.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
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

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ProductService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void postProductosDevuelve201() throws Exception {
        var req = new CrearProductoRequest("SKU-001", "Caja 30x20", null, new BigDecimal("2500"));
        when(service.crear(any())).thenReturn(dtoEjemplo());

        mockMvc.perform(post("/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/productos/1"))
            .andExpect(jsonPath("$.sku").value("SKU-001"));
    }

    @Test
    void postProductosSinSkuDevuelve400() throws Exception {
        String body = """
            { "sku": "", "nombre": "X", "precio": 1.0 }
            """;
        mockMvc.perform(post("/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.sku").exists());
    }

    @Test
    void getByIdDevuelve200() throws Exception {
        when(service.findById(1L)).thenReturn(dtoEjemplo());

        mockMvc.perform(get("/productos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.idProducto").value(1));
    }

    @Test
    void getByIdInexistenteDevuelve404() throws Exception {
        when(service.findById(99L)).thenThrow(
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        mockMvc.perform(get("/productos/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Producto no encontrado"));
    }

    @Test
    void getBySkuDevuelve200() throws Exception {
        when(service.findBySku("SKU-001")).thenReturn(dtoEjemplo());

        mockMvc.perform(get("/productos/sku/SKU-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sku").value("SKU-001"));
    }

    @Test
    void listarDevuelveArray() throws Exception {
        when(service.findAll()).thenReturn(List.of(dtoEjemplo()));

        mockMvc.perform(get("/productos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].sku").value("SKU-001"));
    }

    @Test
    void patchActualizarDevuelve200() throws Exception {
        when(service.actualizar(eq(1L), any())).thenReturn(dtoEjemplo());
        String body = """
            { "nombre": "Caja XL", "activo": true }
            """;
        mockMvc.perform(patch("/productos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());
    }

    private ProductDto dtoEjemplo() {
        OffsetDateTime now = OffsetDateTime.parse("2026-05-13T10:00:00Z");
        return new ProductDto(1L, "SKU-001", "Caja 30x20", null, new BigDecimal("2500.00"), true, now, now);
    }
}
