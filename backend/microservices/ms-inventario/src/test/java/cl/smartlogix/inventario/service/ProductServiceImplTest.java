package cl.smartlogix.inventario.service;

import cl.smartlogix.inventario.dto.ActualizarProductoRequest;
import cl.smartlogix.inventario.dto.CrearProductoRequest;
import cl.smartlogix.inventario.dto.ProductDto;
import cl.smartlogix.inventario.model.Producto;
import cl.smartlogix.inventario.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock ProductoRepository repository;
    @InjectMocks ProductServiceImpl service;

    @Test
    void crearConSkuNuevoPersisteYDevuelve() {
        var req = new CrearProductoRequest("SKU-001", "Caja 30x20", "desc", new BigDecimal("2500"));
        when(repository.existsBySku("SKU-001")).thenReturn(false);
        when(repository.save(any(Producto.class))).thenAnswer(i -> {
            Producto p = i.getArgument(0);
            p.setIdProducto(42L);
            return p;
        });

        ProductDto result = service.crear(req);

        assertThat(result.idProducto()).isEqualTo(42L);
        assertThat(result.sku()).isEqualTo("SKU-001");
        assertThat(result.activo()).isTrue();
        assertThat(result.precio()).isEqualByComparingTo("2500");
    }

    @Test
    void crearConSkuExistenteLanzaConflict() {
        var req = new CrearProductoRequest("SKU-DUPE", "Otro", null, new BigDecimal("100"));
        when(repository.existsBySku("SKU-DUPE")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(req))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("SKU ya existe");
    }

    @Test
    void actualizarSoloModificaCamposNoNulos() {
        Producto existente = Producto.builder()
            .idProducto(1L).sku("SKU-001").nombre("original")
            .descripcion("desc original").precio(new BigDecimal("1000")).activo(true)
            .build();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        // Solo cambia precio; nombre y descripcion deben quedar igual
        var req = new ActualizarProductoRequest(null, null, new BigDecimal("9999"), null);
        ProductDto result = service.actualizar(1L, req);

        assertThat(result.nombre()).isEqualTo("original");
        assertThat(result.descripcion()).isEqualTo("desc original");
        assertThat(result.precio()).isEqualByComparingTo("9999");
        assertThat(result.activo()).isTrue();
    }

    @Test
    void findByIdInexistenteLanzaNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Producto no encontrado");
    }

    @Test
    void findBySkuInexistenteLanzaNotFound() {
        when(repository.findBySku("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findBySku("MISSING"))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Producto no encontrado");
    }
}
