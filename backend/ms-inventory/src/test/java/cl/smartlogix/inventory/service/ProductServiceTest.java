package cl.smartlogix.inventory.service;

import cl.smartlogix.inventory.dto.CreateProductRequest;
import cl.smartlogix.inventory.dto.UpdateProductRequest;
import cl.smartlogix.inventory.model.Product;
import cl.smartlogix.inventory.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Product buildProduct() {
        return Product.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Producto Test")
                .description("Descripcion")
                .price(BigDecimal.valueOf(1000))
                .active(true)
                .build();
    }

    // ---------------- CREATE ----------------

    @Test
    void debeCrearProductoCorrectamente() {

        CreateProductRequest req = new CreateProductRequest(
                "SKU-001",
                "Producto Test",
                "Descripcion",
                BigDecimal.valueOf(1000)
        );

        Product saved = buildProduct();

        when(repository.existsBySku(req.sku())).thenReturn(false);
        when(repository.save(any(Product.class))).thenReturn(saved);

        var result = service.create(req);

        assertNotNull(result);
        assertEquals("SKU-001", result.sku());
        assertEquals("Producto Test", result.name());

        verify(repository).save(any(Product.class));
    }

    @Test
    void noDebeCrearProductoConSkuDuplicado() {

        CreateProductRequest req = new CreateProductRequest(
                "SKU-001",
                "Producto Test",
                "Descripcion",
                BigDecimal.valueOf(1000)
        );

        when(repository.existsBySku(req.sku())).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.create(req)
        );

        assertTrue(ex.getReason().contains("SKU ya existe"));
        verify(repository, never()).save(any());
    }

    // ---------------- FIND BY ID ----------------

    @Test
    void debeBuscarPorId() {

        Product product = buildProduct();

        when(repository.findById(1L)).thenReturn(Optional.of(product));

        var result = service.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.productId());
        assertEquals("Producto Test", result.name());
    }

    @Test
    void debeFallarSiProductoNoExistePorId() {

        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.findById(1L)
        );

        assertTrue(ex.getReason().contains("Producto no encontrado"));
    }

    // ---------------- FIND BY SKU ----------------

    @Test
    void debeBuscarPorSku() {

        Product product = buildProduct();

        when(repository.findBySku("SKU-001")).thenReturn(Optional.of(product));

        var result = service.findBySku("SKU-001");

        assertNotNull(result);
        assertEquals("SKU-001", result.sku());
        assertEquals("Producto Test", result.name());
    }

    @Test
    void debeFallarSiSkuNoExiste() {

        when(repository.findBySku("SKU-001")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.findBySku("SKU-001")
        );

        assertTrue(ex.getReason().contains("Producto no encontrado"));
    }

    // ---------------- FIND ALL ----------------

    @Test
    void debeListarProductos() {

        when(repository.findAll()).thenReturn(List.of(buildProduct()));

        var result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("SKU-001", result.get(0).sku());

        verify(repository).findAll();
    }

    // ---------------- UPDATE ----------------

    @Test
    void debeActualizarProducto() {

        Product existing = buildProduct();

        UpdateProductRequest req = new UpdateProductRequest(
                "Producto Actualizado",
                "Nueva descripcion",
                BigDecimal.valueOf(2000),
                false
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Product.class))).thenReturn(existing);

        var result = service.update(1L, req);

        assertEquals("Producto Actualizado", result.name());
        assertEquals(BigDecimal.valueOf(2000), result.price());
        assertFalse(result.active());

        verify(repository).save(any(Product.class));
    }

    @Test
    void debeFallarUpdateSiNoExisteProducto() {

        UpdateProductRequest req = new UpdateProductRequest(
                "X",
                "X",
                BigDecimal.TEN,
                true
        );

        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.update(1L, req)
        );

        assertTrue(ex.getReason().contains("Producto no encontrado"));
    }
}