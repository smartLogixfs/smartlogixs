package cl.smartlogix.inventory.service;

import cl.smartlogix.inventory.dto.StockMovementRequest;
import cl.smartlogix.inventory.dto.StockDto;
import cl.smartlogix.inventory.dto.StockMovementDto;

import java.util.List;

public interface StockService {
    StockDto get(Long idProducto, Long idBodega);
    List<StockDto> findByProducto(Long idProducto);
    List<StockDto> findConStockBajo();
    int disponibleTotal(Long idProducto);
    List<StockMovementDto> historial(Long idStock);

    StockDto entrada(StockMovementRequest req);
    StockDto salida(StockMovementRequest req);
    StockDto reservar(StockMovementRequest req);
    StockDto liberar(StockMovementRequest req);
}
