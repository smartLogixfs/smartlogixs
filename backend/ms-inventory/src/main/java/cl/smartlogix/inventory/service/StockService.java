package cl.smartlogix.inventory.service;

import cl.smartlogix.inventory.dto.StockMovementRequest;
import cl.smartlogix.inventory.dto.StockDto;
import cl.smartlogix.inventory.dto.StockMovementDto;

import java.util.List;

public interface StockService {
    StockDto get(Long productId, Long warehouseId);
    List<StockDto> findByProduct(Long productId);
    List<StockDto> findLowStock();
    int totalAvailable(Long productId);
    List<StockMovementDto> history(Long stockId);

    StockDto stockIn(StockMovementRequest req);
    StockDto stockOut(StockMovementRequest req);
    StockDto reserve(StockMovementRequest req);
    StockDto release(StockMovementRequest req);
}
