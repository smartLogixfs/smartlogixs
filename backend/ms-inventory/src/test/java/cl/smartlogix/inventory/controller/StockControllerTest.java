package cl.smartlogix.inventory.controller;

import cl.smartlogix.inventory.dto.StockDto;
import cl.smartlogix.inventory.dto.StockMovementDto;
import cl.smartlogix.inventory.dto.StockMovementRequest;
import cl.smartlogix.inventory.model.MovementType;
import cl.smartlogix.inventory.service.StockService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

class StockControllerTest {

    @Mock
    private StockService service;

    @InjectMocks
    private StockController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private StockDto buildStock() {
        return new StockDto(
                1L,
                1L,
                "SKU001",
                1L,
                "Bodega Central",
                100,
                10,
                90,
                5,
                OffsetDateTime.now()
        );
    }

    @Test
    void debeObtenerStock() {

        StockDto dto = buildStock();

        when(service.get(1L, 1L))
                .thenReturn(dto);

        ResponseEntity<StockDto> response =
                controller.get(1L, 1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(90, response.getBody().available());

        verify(service).get(1L, 1L);
    }

    @Test
    void debeBuscarStockPorProducto() {

        when(service.findByProduct(1L))
                .thenReturn(List.of(buildStock()));

        ResponseEntity<List<StockDto>> response =
                controller.byProduct(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());

        verify(service).findByProduct(1L);
    }

    @Test
    void debeBuscarStockPorProductoVacio() {

        when(service.findByProduct(1L))
                .thenReturn(List.of());

        ResponseEntity<List<StockDto>> response =
                controller.byProduct(1L);

        assertTrue(response.getBody().isEmpty());

        verify(service).findByProduct(1L);
    }

    @Test
    void debeRetornarTotalDisponible() {

        when(service.totalAvailable(1L))
                .thenReturn(90);

        ResponseEntity<Map<String, Integer>> response =
                controller.totalAvailable(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(90, response.getBody().get("available"));

        verify(service).totalAvailable(1L);
    }

    @Test
    void debeListarStockBajo() {

        when(service.findLowStock())
                .thenReturn(List.of(buildStock()));

        ResponseEntity<List<StockDto>> response =
                controller.lowStock();

        assertEquals(1, response.getBody().size());

        verify(service).findLowStock();
    }

    @Test
    void debeListarStockBajoVacio() {

        when(service.findLowStock())
                .thenReturn(List.of());

        ResponseEntity<List<StockDto>> response =
                controller.lowStock();

        assertTrue(response.getBody().isEmpty());

        verify(service).findLowStock();
    }

    @Test
    void debeRetornarHistorialMovimientos() {

        StockMovementDto movement =
                new StockMovementDto(
                        1L,
                        1L,
                        MovementType.ENTRADA,
                        10,
                        "ORD-1",
                        OffsetDateTime.now()
                );

        when(service.history(1L))
                .thenReturn(List.of(movement));

        ResponseEntity<List<StockMovementDto>> response =
                controller.history(1L);

        assertEquals(1, response.getBody().size());

        verify(service).history(1L);
    }

    @Test
    void debeIngresarStock() {

        StockMovementRequest request =
                new StockMovementRequest(
                        1L,
                        1L,
                        10,
                        "ORD-1"
                );

        when(service.stockIn(request))
                .thenReturn(buildStock());

        ResponseEntity<StockDto> response =
                controller.stockIn(request);

        assertNotNull(response.getBody());

        verify(service).stockIn(request);
    }

    @Test
    void debeRetirarStock() {

        StockMovementRequest request =
                new StockMovementRequest(
                        1L,
                        1L,
                        10,
                        "ORD-1"
                );

        when(service.stockOut(request))
                .thenReturn(buildStock());

        ResponseEntity<StockDto> response =
                controller.stockOut(request);

        assertNotNull(response.getBody());

        verify(service).stockOut(request);
    }

    @Test
    void debeReservarStock() {

        StockMovementRequest request =
                new StockMovementRequest(
                        1L,
                        1L,
                        10,
                        "ORD-1"
                );

        when(service.reserve(request))
                .thenReturn(buildStock());

        ResponseEntity<StockDto> response =
                controller.reserve(request);

        assertNotNull(response.getBody());

        verify(service).reserve(request);
    }

    @Test
    void debeLiberarStockReservado() {

        StockMovementRequest request =
                new StockMovementRequest(
                        1L,
                        1L,
                        10,
                        "ORD-1"
                );

        when(service.release(request))
                .thenReturn(buildStock());

        ResponseEntity<StockDto> response =
                controller.release(request);

        assertNotNull(response.getBody());

        verify(service).release(request);
    }
}