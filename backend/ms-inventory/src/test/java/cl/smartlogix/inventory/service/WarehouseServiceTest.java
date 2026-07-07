package cl.smartlogix.inventory.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import cl.smartlogix.inventory.dto.CreateWarehouseRequest;
import cl.smartlogix.inventory.dto.WarehouseDto;
import cl.smartlogix.inventory.model.Warehouse;
import cl.smartlogix.inventory.repository.WarehouseRepository;

class WarehouseServiceTest {

    @Mock
    private WarehouseRepository repository;

    @InjectMocks
    private WarehouseServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Warehouse buildWarehouse() {
        return Warehouse.builder()
                .id(1L)
                .name("Bodega Central")
                .location("Santiago")
                .active(true)
                .build();
    }

    @Test
    void debeCrearBodega() {

        CreateWarehouseRequest request =
                new CreateWarehouseRequest(
                        "Bodega Central",
                        "Santiago"
                );

        Warehouse warehouse = buildWarehouse();

        when(repository.save(any(Warehouse.class)))
                .thenReturn(warehouse);

        WarehouseDto resultado = service.create(request);

        assertNotNull(resultado);
        assertEquals(1L, resultado.warehouseId());
        assertEquals("Bodega Central", resultado.name());
        assertEquals("Santiago", resultado.location());
        assertTrue(resultado.active());

        verify(repository).save(any(Warehouse.class));
    }

    @Test
    void debeBuscarBodegaPorId() {

        Warehouse warehouse = buildWarehouse();

        when(repository.findById(1L))
                .thenReturn(Optional.of(warehouse));

        WarehouseDto resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.warehouseId());
        assertEquals("Bodega Central", resultado.name());

        verify(repository).findById(1L);
    }

    @Test
    void debeLanzarErrorSiBodegaNoExiste() {

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.findById(1L)
                );

        assertEquals(
                "404 NOT_FOUND \"Bodega no encontrada: 1\"",
                exception.getMessage()
        );

        verify(repository).findById(1L);
    }

    @Test
    void debeListarBodegas() {

        Warehouse warehouse1 = buildWarehouse();

        Warehouse warehouse2 = Warehouse.builder()
                .id(2L)
                .name("Bodega Norte")
                .location("Antofagasta")
                .active(true)
                .build();

        when(repository.findAll())
                .thenReturn(List.of(warehouse1, warehouse2));

        List<WarehouseDto> resultado = service.findAll();

        assertEquals(2, resultado.size());

        assertEquals(
                "Bodega Central",
                resultado.get(0).name()
        );

        assertEquals(
                "Bodega Norte",
                resultado.get(1).name()
        );

        verify(repository).findAll();
    }

    @Test
    void debeRetornarListaVaciaCuandoNoHayBodegas() {

        when(repository.findAll())
                .thenReturn(List.of());

        List<WarehouseDto> resultado = service.findAll();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(repository).findAll();
    }
}