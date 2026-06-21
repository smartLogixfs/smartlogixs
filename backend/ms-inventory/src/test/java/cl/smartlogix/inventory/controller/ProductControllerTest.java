package cl.smartlogix.inventory.controller;

import cl.smartlogix.inventory.dto.CreateProductRequest;
import cl.smartlogix.inventory.dto.ProductDto;
import cl.smartlogix.inventory.dto.UpdateProductRequest;
import cl.smartlogix.inventory.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService service;

    @InjectMocks
    private ProductController controller;

    @Test
    void debeCrearProducto() {

        CreateProductRequest request = new CreateProductRequest(
                "SKU001",
                "Producto Test",
                "Descripcion",
                new BigDecimal("1000")
        );

        ProductDto dto = new ProductDto(
                1L,
                "SKU001",
                "Producto Test",
                "Descripcion",
                new BigDecimal("1000"),
                true,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(service.create(request)).thenReturn(dto);

        ResponseEntity<ProductDto> response =
                controller.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(dto, response.getBody());
        assertEquals("/products/1",
                response.getHeaders().getLocation().toString());
    }

    @Test
    void debeBuscarProductoPorId() {

        ProductDto dto = new ProductDto(
                1L,
                "SKU001",
                "Producto Test",
                "Descripcion",
                new BigDecimal("1000"),
                true,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(service.findById(1L)).thenReturn(dto);

        ResponseEntity<ProductDto> response =
                controller.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void debeBuscarProductoPorSku() {

        ProductDto dto = new ProductDto(
                1L,
                "SKU001",
                "Producto Test",
                "Descripcion",
                new BigDecimal("1000"),
                true,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(service.findBySku("SKU001")).thenReturn(dto);

        ResponseEntity<ProductDto> response =
                controller.getBySku("SKU001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void debeListarProductos() {

        ProductDto dto1 = new ProductDto(
                1L,
                "SKU001",
                "Producto 1",
                "Descripcion",
                new BigDecimal("1000"),
                true,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        ProductDto dto2 = new ProductDto(
                2L,
                "SKU002",
                "Producto 2",
                "Descripcion",
                new BigDecimal("2000"),
                true,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(service.findAll())
                .thenReturn(List.of(dto1, dto2));

        ResponseEntity<List<ProductDto>> response =
                controller.list();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void debeActualizarProducto() {

        UpdateProductRequest request = new UpdateProductRequest(
                "Producto Actualizado",
                "Nueva descripcion",
                new BigDecimal("1500"),
                true
        );

        ProductDto dto = new ProductDto(
                1L,
                "SKU001",
                "Producto Actualizado",
                "Nueva descripcion",
                new BigDecimal("1500"),
                true,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(service.update(1L, request))
                .thenReturn(dto);

        ResponseEntity<ProductDto> response =
                controller.update(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }
}