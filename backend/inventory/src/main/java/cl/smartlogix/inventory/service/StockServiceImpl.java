package cl.smartlogix.inventory.service;

import cl.smartlogix.inventory.dto.MovimientoRequest;
import cl.smartlogix.inventory.dto.StockDto;
import cl.smartlogix.inventory.dto.StockMovementDto;
import cl.smartlogix.inventory.model.Bodega;
import cl.smartlogix.inventory.model.MovimientoStock;
import cl.smartlogix.inventory.model.Producto;
import cl.smartlogix.inventory.model.Stock;
import cl.smartlogix.inventory.model.TipoMovimiento;
import cl.smartlogix.inventory.repository.BodegaRepository;
import cl.smartlogix.inventory.repository.MovimientoStockRepository;
import cl.smartlogix.inventory.repository.ProductoRepository;
import cl.smartlogix.inventory.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;
    private final MovimientoStockRepository movimientoRepository;

    @Override
    @Transactional(readOnly = true)
    public StockDto get(Long idProducto, Long idBodega) {
        return StockDto.from(buscarStock(idProducto, idBodega));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDto> findByProducto(Long idProducto) {
        return stockRepository.findByProducto_IdProducto(idProducto).stream().map(StockDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDto> findConStockBajo() {
        return stockRepository.findConStockBajo().stream().map(StockDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public int disponibleTotal(Long idProducto) {
        return stockRepository.sumDisponibleByProducto(idProducto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementDto> historial(Long idStock) {
        return movimientoRepository.findByStock_IdStockOrderByCreatedAtDesc(idStock).stream()
            .map(StockMovementDto::from).toList();
    }

    @Override
    public StockDto entrada(MovimientoRequest req) {
        Stock stock = obtenerOCrearStock(req.idProducto(), req.idBodega());
        stock.setCantidad(stock.getCantidad() + req.cantidad());
        // Persiste el Stock antes del Movimiento: en el primer ENTRADA el Stock es transient
        // y MovimientoStock.stock es non-nullable, lo cual falla la validación de Hibernate.
        stock = stockRepository.save(stock);
        registrarMovimiento(stock, TipoMovimiento.ENTRADA, req);
        return StockDto.from(stock);
    }

    @Override
    public StockDto salida(MovimientoRequest req) {
        Stock stock = buscarStock(req.idProducto(), req.idBodega());
        if (stock.getDisponible() < req.cantidad()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Stock disponible insuficiente: " + stock.getDisponible() + " < " + req.cantidad());
        }
        stock.setCantidad(stock.getCantidad() - req.cantidad());
        registrarMovimiento(stock, TipoMovimiento.SALIDA, req);
        return StockDto.from(stockRepository.save(stock));
    }

    @Override
    public StockDto reservar(MovimientoRequest req) {
        Stock stock = buscarStock(req.idProducto(), req.idBodega());
        if (stock.getDisponible() < req.cantidad()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Stock disponible insuficiente para reservar: " + stock.getDisponible() + " < " + req.cantidad());
        }
        stock.setCantReservada(stock.getCantReservada() + req.cantidad());
        registrarMovimiento(stock, TipoMovimiento.RESERVA, req);
        return StockDto.from(stockRepository.save(stock));
    }

    @Override
    public StockDto liberar(MovimientoRequest req) {
        Stock stock = buscarStock(req.idProducto(), req.idBodega());
        if (stock.getCantReservada() < req.cantidad()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "No hay reserva suficiente para liberar: " + stock.getCantReservada() + " < " + req.cantidad());
        }
        stock.setCantReservada(stock.getCantReservada() - req.cantidad());
        registrarMovimiento(stock, TipoMovimiento.LIBERACION, req);
        return StockDto.from(stockRepository.save(stock));
    }

    private Stock buscarStock(Long idProducto, Long idBodega) {
        return stockRepository.findByProducto_IdProductoAndBodega_IdBodega(idProducto, idBodega)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No existe stock para producto " + idProducto + " en bodega " + idBodega));
    }

    private Stock obtenerOCrearStock(Long idProducto, Long idBodega) {
        return stockRepository.findByProducto_IdProductoAndBodega_IdBodega(idProducto, idBodega)
            .orElseGet(() -> {
                Producto producto = productoRepository.findById(idProducto)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + idProducto));
                Bodega bodega = bodegaRepository.findById(idBodega)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bodega no encontrada: " + idBodega));
                return Stock.builder()
                    .producto(producto)
                    .bodega(bodega)
                    .cantidad(0)
                    .cantReservada(0)
                    .stockMinimo(0)
                    .build();
            });
    }

    private void registrarMovimiento(Stock stock, TipoMovimiento tipo, MovimientoRequest req) {
        movimientoRepository.save(MovimientoStock.builder()
            .stock(stock)
            .tipo(tipo)
            .cantidad(req.cantidad())
            .referenciaPedido(req.referenciaPedido())
            .build());
    }
}
