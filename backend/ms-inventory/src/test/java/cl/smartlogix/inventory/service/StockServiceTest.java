package cl.smartlogix.inventory.service;

import cl.smartlogix.inventory.dto.StockMovementRequest;
import cl.smartlogix.inventory.model.Product;
import cl.smartlogix.inventory.model.Stock;
import cl.smartlogix.inventory.model.StockMovement;
import cl.smartlogix.inventory.model.Warehouse;
import cl.smartlogix.inventory.repository.ProductRepository;
import cl.smartlogix.inventory.repository.StockMovementRepository;
import cl.smartlogix.inventory.repository.StockRepository;
import cl.smartlogix.inventory.repository.WarehouseRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private StockMovementRepository movementRepository;

    @InjectMocks
    private StockServiceImpl service;

    private Product product;
    private Warehouse warehouse;
    private Stock stock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        product = Product.builder()
                .id(1L)
                .sku("SKU001")
                .name("Producto")
                .build();

        warehouse = Warehouse.builder()
                .id(1L)
                .name("Bodega Central")
                .build();

        stock = Stock.builder()
                .id(1L)
                .product(product)
                .warehouse(warehouse)
                .quantity(100)
                .reservedQuantity(10)
                .minStock(5)
                .build();
    }

    @Test
    void debeObtenerStock() {
        when(stockRepository.findByProduct_IdAndWarehouse_Id(1L, 1L))
                .thenReturn(Optional.of(stock));

        var resultado = service.get(1L, 1L);

        assertNotNull(resultado);
        assertEquals(100, resultado.quantity());
    }

    @Test
    void debeLanzarErrorSiStockNoExiste() {
        when(stockRepository.findByProduct_IdAndWarehouse_Id(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.get(1L, 1L));
    }

    @Test
    void debeListarStockPorProducto() {
        when(stockRepository.findByProduct_Id(1L))
                .thenReturn(List.of(stock));

        assertEquals(1, service.findByProduct(1L).size());
    }

    @Test
    void debeListarStockBajo() {
        when(stockRepository.findLowStock())
                .thenReturn(List.of(stock));

        assertEquals(1, service.findLowStock().size());
    }

    @Test
    void debeRetornarTotalDisponible() {
        when(stockRepository.sumAvailableByProduct(1L))
                .thenReturn(90);

        assertEquals(90, service.totalAvailable(1L));
    }

    @Test
    void debeRetornarHistorialMovimientos() {

        StockMovement movement = StockMovement.builder()
                .id(1L)
                .stock(stock)
                .quantity(5)
                .build();

        when(movementRepository.findByStock_IdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(movement));

        assertEquals(1, service.history(1L).size());
    }

    @Test
    void debeIngresarStock() {

        StockMovementRequest req =
                new StockMovementRequest(1L, 1L, 20, "ORD-1");

        when(stockRepository.findByProduct_IdAndWarehouse_Id(1L, 1L))
                .thenReturn(Optional.of(stock));

        when(stockRepository.save(any(Stock.class)))
                .thenAnswer(i -> i.getArgument(0));

        var resultado = service.stockIn(req);

        assertEquals(120, resultado.quantity());

        verify(movementRepository).save(any());
    }

    @Test
    void debeCrearStockSiNoExiste() {

        StockMovementRequest req =
                new StockMovementRequest(1L, 1L, 10, "ORD-1");

        when(stockRepository.findByProduct_IdAndWarehouse_Id(1L, 1L))
                .thenReturn(Optional.empty());

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(warehouseRepository.findById(1L))
                .thenReturn(Optional.of(warehouse));

        when(stockRepository.save(any(Stock.class)))
                .thenAnswer(i -> i.getArgument(0));

        var resultado = service.stockIn(req);

        assertEquals(10, resultado.quantity());
    }

    @Test
    void debeRetirarStock() {

        StockMovementRequest req =
                new StockMovementRequest(1L, 1L, 20, "ORD-1");

        when(stockRepository.findByProduct_IdAndWarehouse_Id(1L, 1L))
                .thenReturn(Optional.of(stock));

        when(stockRepository.save(any(Stock.class)))
                .thenAnswer(i -> i.getArgument(0));

        var resultado = service.stockOut(req);

        assertEquals(80, resultado.quantity());
    }

    @Test
    void debeFallarSiNoHayStockDisponible() {

        StockMovementRequest req =
                new StockMovementRequest(1L, 1L, 500, "ORD-1");

        when(stockRepository.findByProduct_IdAndWarehouse_Id(1L, 1L))
                .thenReturn(Optional.of(stock));

        assertThrows(ResponseStatusException.class,
                () -> service.stockOut(req));
    }

    @Test
    void debeReservarStock() {

        StockMovementRequest req =
                new StockMovementRequest(1L, 1L, 20, "ORD-1");

        when(stockRepository.findByProduct_IdAndWarehouse_Id(1L, 1L))
                .thenReturn(Optional.of(stock));

        when(stockRepository.save(any(Stock.class)))
                .thenAnswer(i -> i.getArgument(0));

        var resultado = service.reserve(req);

        assertEquals(30, resultado.reservedQuantity());
    }

    @Test
    void debeLiberarReserva() {

        StockMovementRequest req =
                new StockMovementRequest(1L, 1L, 5, "ORD-1");

        when(stockRepository.findByProduct_IdAndWarehouse_Id(1L, 1L))
                .thenReturn(Optional.of(stock));

        when(stockRepository.save(any(Stock.class)))
                .thenAnswer(i -> i.getArgument(0));

        var resultado = service.release(req);

        assertEquals(5, resultado.reservedQuantity());
    }

    @Test
    void debeFallarSiNoHayReservaSuficiente() {

        StockMovementRequest req =
                new StockMovementRequest(1L, 1L, 50, "ORD-1");

        when(stockRepository.findByProduct_IdAndWarehouse_Id(1L, 1L))
                .thenReturn(Optional.of(stock));

        assertThrows(ResponseStatusException.class,
                () -> service.release(req));
    }

    @Test
    void debeFallarSiProductoNoExisteAlCrearStock() {

        StockMovementRequest req =
                new StockMovementRequest(1L, 1L, 10, "ORD-1");

        when(stockRepository.findByProduct_IdAndWarehouse_Id(1L, 1L))
                .thenReturn(Optional.empty());

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.stockIn(req));
    }

    @Test
    void debeFallarSiBodegaNoExisteAlCrearStock() {

        StockMovementRequest req =
                new StockMovementRequest(1L, 1L, 10, "ORD-1");

        when(stockRepository.findByProduct_IdAndWarehouse_Id(1L, 1L))
                .thenReturn(Optional.empty());

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(warehouseRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.stockIn(req));
    }
}
