package cl.smartlogix.inventory.controller;

import cl.smartlogix.inventory.dto.CreateWarehouseRequest;
import cl.smartlogix.inventory.dto.WarehouseDto;
import cl.smartlogix.inventory.service.WarehouseService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

class WarehouseControllerTest {

    @Mock
    private WarehouseService service;

    @InjectMocks
    private WarehouseController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private WarehouseDto buildWarehouse() {
        return new WarehouseDto(
                1L,
                "Bodega Central",
                "Santiago",
                true
        );
    }

    @Test
    void debeCrearBodega() {

        CreateWarehouseRequest request =
                new CreateWarehouseRequest(
                        "Bodega Central",
                        "Santiago"
                );

        WarehouseDto dto = buildWarehouse();

        when(service.create(request))
                .thenReturn(dto);

        ResponseEntity<WarehouseDto> response =
                controller.create(request);

        assertEquals(201,
                response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals("Bodega Central",
                response.getBody().name());

        verify(service).create(request);
    }

    @Test
    void debeBuscarBodegaPorId() {

        WarehouseDto dto = buildWarehouse();

        when(service.findById(1L))
                .thenReturn(dto);

        ResponseEntity<WarehouseDto> response =
                controller.getById(1L);

        assertEquals(200,
                response.getStatusCode().value());

        assertEquals(1L,
                response.getBody().warehouseId());

        verify(service).findById(1L);
    }

    @Test
    void debeListarBodegas() {

        WarehouseDto dto = buildWarehouse();

        when(service.findAll())
                .thenReturn(List.of(dto));

        ResponseEntity<List<WarehouseDto>> response =
                controller.list();

        assertEquals(200,
                response.getStatusCode().value());

        assertEquals(1,
                response.getBody().size());

        verify(service).findAll();
    }

    @Test
    void debeListarBodegasVacio() {

        when(service.findAll())
                .thenReturn(List.of());

        ResponseEntity<List<WarehouseDto>> response =
                controller.list();

        assertNotNull(response.getBody());

        assertTrue(response.getBody().isEmpty());

        verify(service).findAll();
    }
}