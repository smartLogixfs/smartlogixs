package cl.smartlogix.inventario.service;

import cl.smartlogix.inventario.dto.MovimientoRequest;
import cl.smartlogix.inventario.dto.StockDto;
import cl.smartlogix.inventario.model.Bodega;
import cl.smartlogix.inventario.model.MovimientoStock;
import cl.smartlogix.inventario.model.Producto;
import cl.smartlogix.inventario.model.Stock;
import cl.smartlogix.inventario.model.TipoMovimiento;
import cl.smartlogix.inventario.repository.BodegaRepository;
import cl.smartlogix.inventario.repository.MovimientoStockRepository;
import cl.smartlogix.inventario.repository.ProductoRepository;
import cl.smartlogix.inventario.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock StockRepository stockRepository;
    @Mock ProductoRepository productoRepository;
    @Mock BodegaRepository bodegaRepository;
    @Mock MovimientoStockRepository movimientoRepository;
    @InjectMocks StockServiceImpl service;

    @Test
    void entradaPrimerIngresoCreaStockAntesDelMovimiento() {
        // No existe stock previo → debe buscar producto y bodega para crear el Stock
        when(stockRepository.findByProducto_IdProductoAndBodega_IdBodega(1L, 1L))
            .thenReturn(Optional.empty());
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoConId(1L)));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodegaConId(1L)));
        // El save debe ejecutarse ANTES de movimientoRepository.save (caso original que rompía
        // con TransientPropertyValueException)
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> {
            Stock s = i.getArgument(0);
            s.setIdStock(10L);
            return s;
        });

        StockDto result = service.entrada(new MovimientoRequest(1L, 1L, 100, "PED-1"));

        assertThat(result.cantidad()).isEqualTo(100);
        assertThat(result.disponible()).isEqualTo(100);

        ArgumentCaptor<MovimientoStock> movCaptor = ArgumentCaptor.forClass(MovimientoStock.class);
        verify(movimientoRepository).save(movCaptor.capture());
        assertThat(movCaptor.getValue().getStock().getIdStock()).isEqualTo(10L); // Stock ya persistido
        assertThat(movCaptor.getValue().getTipo()).isEqualTo(TipoMovimiento.ENTRADA);
    }

    @Test
    void entradaSobreStockExistenteSumaCantidad() {
        Stock existente = stockConCantidades(50, 0);
        when(stockRepository.findByProducto_IdProductoAndBodega_IdBodega(1L, 1L))
            .thenReturn(Optional.of(existente));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        StockDto result = service.entrada(new MovimientoRequest(1L, 1L, 30, null));

        assertThat(result.cantidad()).isEqualTo(80);
        verify(movimientoRepository, times(1)).save(any(MovimientoStock.class));
    }

    @Test
    void salidaConDisponibleInsuficienteLanzaConflict() {
        Stock existente = stockConCantidades(5, 0);
        when(stockRepository.findByProducto_IdProductoAndBodega_IdBodega(1L, 1L))
            .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.salida(new MovimientoRequest(1L, 1L, 10, null)))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Stock disponible insuficiente");
    }

    @Test
    void reservarConDisponibleSuficienteIncrementaReservada() {
        Stock existente = stockConCantidades(10, 2); // disponible 8
        when(stockRepository.findByProducto_IdProductoAndBodega_IdBodega(1L, 1L))
            .thenReturn(Optional.of(existente));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        StockDto result = service.reservar(new MovimientoRequest(1L, 1L, 5, "PED-XYZ"));

        assertThat(result.cantReservada()).isEqualTo(7);
        assertThat(result.disponible()).isEqualTo(3);
    }

    @Test
    void reservarConDisponibleInsuficienteLanzaConflict() {
        Stock existente = stockConCantidades(10, 8); // disponible 2
        when(stockRepository.findByProducto_IdProductoAndBodega_IdBodega(1L, 1L))
            .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.reservar(new MovimientoRequest(1L, 1L, 5, null)))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("insuficiente para reservar");
    }

    @Test
    void liberarConReservaSuficienteDisminuye() {
        Stock existente = stockConCantidades(10, 5);
        when(stockRepository.findByProducto_IdProductoAndBodega_IdBodega(1L, 1L))
            .thenReturn(Optional.of(existente));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        StockDto result = service.liberar(new MovimientoRequest(1L, 1L, 3, null));

        assertThat(result.cantReservada()).isEqualTo(2);
        assertThat(result.disponible()).isEqualTo(8);
    }

    @Test
    void liberarMasDeLoReservadoLanzaConflict() {
        Stock existente = stockConCantidades(10, 2);
        when(stockRepository.findByProducto_IdProductoAndBodega_IdBodega(1L, 1L))
            .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.liberar(new MovimientoRequest(1L, 1L, 5, null)))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("No hay reserva suficiente");
    }

    @Test
    void getStockInexistenteLanzaNotFound() {
        when(stockRepository.findByProducto_IdProductoAndBodega_IdBodega(99L, 99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L, 99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("No existe stock");
    }

    private Producto productoConId(long id) {
        return Producto.builder()
            .idProducto(id).sku("SKU-001").nombre("Test").precio(BigDecimal.ZERO).activo(true)
            .build();
    }

    private Bodega bodegaConId(long id) {
        return Bodega.builder().idBodega(id).nombre("Bodega").activo(true).build();
    }

    private Stock stockConCantidades(int cantidad, int reservada) {
        return Stock.builder()
            .idStock(1L)
            .producto(productoConId(1L))
            .bodega(bodegaConId(1L))
            .cantidad(cantidad)
            .cantReservada(reservada)
            .stockMinimo(0)
            .build();
    }
}
