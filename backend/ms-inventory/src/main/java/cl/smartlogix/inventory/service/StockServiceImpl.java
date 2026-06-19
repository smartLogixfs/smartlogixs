package cl.smartlogix.inventory.service;

import cl.smartlogix.inventory.dto.StockMovementRequest;
import cl.smartlogix.inventory.dto.StockDto;
import cl.smartlogix.inventory.dto.StockMovementDto;
import cl.smartlogix.inventory.model.Warehouse;
import cl.smartlogix.inventory.model.StockMovement;
import cl.smartlogix.inventory.model.Product;
import cl.smartlogix.inventory.model.Stock;
import cl.smartlogix.inventory.model.MovementType;
import cl.smartlogix.inventory.repository.WarehouseRepository;
import cl.smartlogix.inventory.repository.StockMovementRepository;
import cl.smartlogix.inventory.repository.ProductRepository;
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
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockMovementRepository movementRepository;

    @Override
    @Transactional(readOnly = true)
    public StockDto get(Long productId, Long warehouseId) {
        return StockDto.from(findStock(productId, warehouseId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDto> findByProduct(Long productId) {
        return stockRepository.findByProduct_Id(productId).stream().map(StockDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDto> findLowStock() {
        return stockRepository.findLowStock().stream().map(StockDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public int totalAvailable(Long productId) {
        return stockRepository.sumAvailableByProduct(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementDto> history(Long stockId) {
        return movementRepository.findByStock_IdOrderByCreatedAtDesc(stockId).stream()
            .map(StockMovementDto::from).toList();
    }

    @Override
    public StockDto stockIn(StockMovementRequest req) {
        Stock stock = getOrCreateStock(req.productId(), req.warehouseId());
        stock.setQuantity(stock.getQuantity() + req.quantity());
        // Persiste el Stock antes del Movement: en el primer ENTRADA el Stock es transient
        // y StockMovement.stock es non-nullable, lo cual falla la validación de Hibernate.
        stock = stockRepository.save(stock);
        registerMovement(stock, MovementType.ENTRADA, req);
        return StockDto.from(stock);
    }

    @Override
    public StockDto stockOut(StockMovementRequest req) {
        Stock stock = findStock(req.productId(), req.warehouseId());
        if (stock.getAvailable() < req.quantity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Stock disponible insuficiente: " + stock.getAvailable() + " < " + req.quantity());
        }
        stock.setQuantity(stock.getQuantity() - req.quantity());
        registerMovement(stock, MovementType.SALIDA, req);
        return StockDto.from(stockRepository.save(stock));
    }

    @Override
    public StockDto reserve(StockMovementRequest req) {
        Stock stock = findStock(req.productId(), req.warehouseId());
        if (stock.getAvailable() < req.quantity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Stock disponible insuficiente para reservar: " + stock.getAvailable() + " < " + req.quantity());
        }
        stock.setReservedQuantity(stock.getReservedQuantity() + req.quantity());
        registerMovement(stock, MovementType.RESERVA, req);
        return StockDto.from(stockRepository.save(stock));
    }

    @Override
    public StockDto release(StockMovementRequest req) {
        Stock stock = findStock(req.productId(), req.warehouseId());
        if (stock.getReservedQuantity() < req.quantity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "No hay reserva suficiente para liberar: " + stock.getReservedQuantity() + " < " + req.quantity());
        }
        stock.setReservedQuantity(stock.getReservedQuantity() - req.quantity());
        registerMovement(stock, MovementType.LIBERACION, req);
        return StockDto.from(stockRepository.save(stock));
    }

    private Stock findStock(Long productId, Long warehouseId) {
        return stockRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No existe stock para producto " + productId + " en bodega " + warehouseId));
    }

    private Stock getOrCreateStock(Long productId, Long warehouseId) {
        return stockRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId)
            .orElseGet(() -> {
                Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + productId));
                Warehouse warehouse = warehouseRepository.findById(warehouseId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bodega no encontrada: " + warehouseId));
                return Stock.builder()
                    .product(product)
                    .warehouse(warehouse)
                    .quantity(0)
                    .reservedQuantity(0)
                    .minStock(0)
                    .build();
            });
    }

    private void registerMovement(Stock stock, MovementType type, StockMovementRequest req) {
        movementRepository.save(StockMovement.builder()
            .stock(stock)
            .type(type)
            .quantity(req.quantity())
            .orderReference(req.orderReference())
            .build());
    }
}
