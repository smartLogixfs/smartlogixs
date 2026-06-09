package cl.smartlogix.inventory.service;

import cl.smartlogix.inventory.dto.MovimientoRequest;
import cl.smartlogix.inventory.dto.StockDto;
import cl.smartlogix.inventory.dto.StockMovementDto;

import java.util.List;

public interface StockService {
    StockDto get(Long idProducto, Long idBodega);
    List<StockDto> findByProducto(Long idProducto);
    List<StockDto> findConStockBajo();
    int disponibleTotal(Long idProducto);
    List<StockMovementDto> historial(Long idStock);

    StockDto entrada(MovimientoRequest req);
    StockDto salida(MovimientoRequest req);
    StockDto reservar(MovimientoRequest req);
    StockDto liberar(MovimientoRequest req);
}
