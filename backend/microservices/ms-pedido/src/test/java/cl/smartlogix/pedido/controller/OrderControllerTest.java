package cl.smartlogix.pedido.controller;

import cl.smartlogix.pedido.dto.ActualizarEstadoRequest;
import cl.smartlogix.pedido.dto.CrearPedidoRequest;
import cl.smartlogix.pedido.dto.OrderDto;
import cl.smartlogix.pedido.dto.PedidoItemDto;
import cl.smartlogix.pedido.model.EstadoPedido;
import cl.smartlogix.pedido.model.TipoPedido;
import cl.smartlogix.pedido.service.OrderService;
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

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean OrderService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void postPedidosDevuelve201ConLocation() throws Exception {
        var req = new CrearPedidoRequest(
            TipoPedido.ESTANDAR, "CL-001", null,
            List.of(new CrearPedidoRequest.ItemRequest(1L, "SKU-001", 2, new BigDecimal("5000")))
        );
        when(service.crear(any())).thenReturn(dtoEjemplo());

        mockMvc.perform(post("/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/pedidos/42"))
            .andExpect(jsonPath("$.idPedido").value(42))
            .andExpect(jsonPath("$.codigo").value("PED-20260513-AB12CD"))
            .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void postPedidosSinClienteDevuelve400ProblemDetail() throws Exception {
        // idCliente vacio dispara @NotBlank → MethodArgumentNotValidException → handleValidation
        String body = """
            { "tipo": "ESTANDAR", "idCliente": "", "items": [
              { "idProducto": 1, "sku": "SKU-1", "cantidad": 1, "precioUnitario": 100 } ] }
            """;
        mockMvc.perform(post("/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validación fallida"))
            .andExpect(jsonPath("$.errors.idCliente").exists());
    }

    @Test
    void postPedidosSinItemsDevuelve400() throws Exception {
        String body = """
            { "tipo": "ESTANDAR", "idCliente": "CL-001", "items": [] }
            """;
        mockMvc.perform(post("/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.items").exists());
    }

    @Test
    void getByIdDevuelve200() throws Exception {
        when(service.findById(42L)).thenReturn(dtoEjemplo());

        mockMvc.perform(get("/pedidos/42"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.idPedido").value(42));
    }

    @Test
    void getByIdInexistenteDevuelve404() throws Exception {
        when(service.findById(99L)).thenThrow(
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado: 99"));

        mockMvc.perform(get("/pedidos/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Pedido no encontrado: 99"));
    }

    @Test
    void getByCodigoDevuelve200() throws Exception {
        when(service.findByCodigo("PED-20260513-AB12CD")).thenReturn(dtoEjemplo());

        mockMvc.perform(get("/pedidos/codigo/PED-20260513-AB12CD"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("PED-20260513-AB12CD"));
    }

    @Test
    void getByClienteDevuelveLista() throws Exception {
        when(service.findByCliente("CL-001")).thenReturn(List.of(dtoEjemplo()));

        mockMvc.perform(get("/pedidos/cliente/CL-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idCliente").value("CL-001"));
    }

    @Test
    void listarSinFiltroLlamaFindAll() throws Exception {
        when(service.findAll()).thenReturn(List.of(dtoEjemplo()));

        mockMvc.perform(get("/pedidos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idPedido").value(42));
    }

    @Test
    void listarConEstadoFiltra() throws Exception {
        when(service.findByEstado(EstadoPedido.APROBADO)).thenReturn(List.of());

        mockMvc.perform(get("/pedidos").param("estado", "APROBADO"))
            .andExpect(status().isOk());
    }

    @Test
    void patchEstadoDevuelve200() throws Exception {
        var req = new ActualizarEstadoRequest(EstadoPedido.APROBADO, "Pago OK");
        OrderDto actualizado = dtoEjemploEstado(EstadoPedido.APROBADO);
        when(service.cambiarEstado(eq(42L), any())).thenReturn(actualizado);

        mockMvc.perform(patch("/pedidos/42/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("APROBADO"));
    }

    @Test
    void patchEstadoIlegalDevuelve409() throws Exception {
        var req = new ActualizarEstadoRequest(EstadoPedido.PENDIENTE, "intento ilegal");
        when(service.cambiarEstado(eq(42L), any())).thenThrow(
            new ResponseStatusException(HttpStatus.CONFLICT, "Transición no permitida: ENTREGADO → PENDIENTE"));

        mockMvc.perform(patch("/pedidos/42/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Transición no permitida: ENTREGADO → PENDIENTE"));
    }

    @Test
    void illegalArgumentExceptionDevuelve400() throws Exception {
        when(service.findById(any())).thenThrow(new IllegalArgumentException("argumento inválido"));

        mockMvc.perform(get("/pedidos/1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("argumento inválido"));
    }

    private OrderDto dtoEjemplo() {
        return dtoEjemploEstado(EstadoPedido.PENDIENTE);
    }

    private OrderDto dtoEjemploEstado(EstadoPedido estado) {
        OffsetDateTime now = OffsetDateTime.parse("2026-05-13T10:00:00Z");
        return new OrderDto(
            42L, "PED-20260513-AB12CD",
            TipoPedido.ESTANDAR, estado,
            "CL-001", null,
            new BigDecimal("10000.00"), new BigDecimal("1900.00"), new BigDecimal("11900.00"),
            List.of(new PedidoItemDto(1L, 1L, "SKU-001", 2, new BigDecimal("5000.00"), new BigDecimal("10000.00"))),
            now, now
        );
    }
}
