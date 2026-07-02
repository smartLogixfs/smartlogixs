package cl.smartlogix.inventory.controller;

import cl.smartlogix.inventory.dto.StockMovementRequest;
import cl.smartlogix.inventory.dto.StockDto;
import cl.smartlogix.inventory.dto.StockMovementDto;
import cl.smartlogix.inventory.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la consulta y los movimientos de stock.
 * Cubre consultas por producto/bodega, stock disponible y bajo mínimo, historial
 * de movimientos y operaciones de entrada, salida, reserva y liberación.
 */
@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
@Tag(name = "Stock", description = "Consulta y movimientos de stock por producto y bodega")
public class StockController {

    private final StockService service;

    /**
     * Obtiene el stock de un producto en una bodega concreta.
     *
     * @param productId   identificador del producto
     * @param warehouseId identificador de la bodega
     * @return el registro de stock
     */
    @Operation(summary = "Obtener stock por producto y bodega")
    @ApiResponse(responseCode = "200", description = "Stock encontrado")
    @GetMapping("/{productId}/{warehouseId}")
    public ResponseEntity<StockDto> get(@Parameter(description = "ID del producto") @PathVariable Long productId,
                                        @Parameter(description = "ID de la bodega") @PathVariable Long warehouseId) {
        return ResponseEntity.ok(service.get(productId, warehouseId));
    }

    /**
     * Lista el stock de un producto en todas las bodegas.
     *
     * @param productId identificador del producto
     * @return listado de registros de stock
     */
    @Operation(summary = "Listar stock por producto", description = "Devuelve el stock de un producto en todas las bodegas.")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockDto>> byProduct(@Parameter(description = "ID del producto") @PathVariable Long productId) {
        return ResponseEntity.ok(service.findByProduct(productId));
    }

    /**
     * Calcula el stock disponible total de un producto sumando todas las bodegas.
     *
     * @param productId identificador del producto
     * @return mapa con la clave {@code available} y la cantidad disponible
     */
    @Operation(summary = "Stock disponible total", description = "Suma el stock disponible de un producto en todas las bodegas.")
    @ApiResponse(responseCode = "200", description = "Cantidad disponible calculada")
    @GetMapping("/product/{productId}/available")
    public ResponseEntity<Map<String, Integer>> totalAvailable(@Parameter(description = "ID del producto") @PathVariable Long productId) {
        return ResponseEntity.ok(Map.of("available", service.totalAvailable(productId)));
    }

    /**
     * Lista los registros de stock por debajo del umbral crítico.
     *
     * @return listado de stock bajo mínimo
     */
    @Operation(summary = "Listar stock bajo mínimo", description = "Devuelve los registros de stock por debajo del umbral crítico.")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @GetMapping("/low")
    public ResponseEntity<List<StockDto>> lowStock() {
        return ResponseEntity.ok(service.findLowStock());
    }

    /**
     * Devuelve el historial de movimientos de un registro de stock.
     *
     * @param stockId identificador del registro de stock
     * @return listado de movimientos
     */
    @Operation(summary = "Historial de movimientos", description = "Devuelve los movimientos de un registro de stock.")
    @ApiResponse(responseCode = "200", description = "Historial obtenido correctamente")
    @GetMapping("/{stockId}/history")
    public ResponseEntity<List<StockMovementDto>> history(@Parameter(description = "ID del registro de stock") @PathVariable Long stockId) {
        return ResponseEntity.ok(service.history(stockId));
    }

    /**
     * Registra una entrada (recepción) de stock.
     *
     * @param req datos del movimiento de entrada
     * @return el stock resultante tras la entrada
     */
    @Operation(summary = "Entrada de stock", description = "Registra una entrada/recepción de stock.")
    @ApiResponse(responseCode = "200", description = "Entrada registrada correctamente")
    @PostMapping("/in")
    public ResponseEntity<StockDto> stockIn(@Valid @RequestBody StockMovementRequest req) {
        return ResponseEntity.ok(service.stockIn(req));
    }

    /**
     * Registra una salida (despacho) de stock.
     *
     * @param req datos del movimiento de salida
     * @return el stock resultante tras la salida
     */
    @Operation(summary = "Salida de stock", description = "Registra una salida/despacho de stock.")
    @ApiResponse(responseCode = "200", description = "Salida registrada correctamente")
    @PostMapping("/out")
    public ResponseEntity<StockDto> stockOut(@Valid @RequestBody StockMovementRequest req) {
        return ResponseEntity.ok(service.stockOut(req));
    }

    /**
     * Reserva una cantidad de stock para un pedido.
     *
     * @param req datos del movimiento de reserva
     * @return el stock resultante tras la reserva
     */
    @Operation(summary = "Reservar stock", description = "Reserva una cantidad de stock para un pedido.")
    @ApiResponse(responseCode = "200", description = "Stock reservado correctamente")
    @PostMapping("/reserve")
    public ResponseEntity<StockDto> reserve(@Valid @RequestBody StockMovementRequest req) {
        return ResponseEntity.ok(service.reserve(req));
    }

    /**
     * Libera una reserva de stock previamente realizada.
     *
     * @param req datos del movimiento de liberación
     * @return el stock resultante tras la liberación
     */
    @Operation(summary = "Liberar stock", description = "Libera una reserva de stock previamente realizada.")
    @ApiResponse(responseCode = "200", description = "Reserva liberada correctamente")
    @PostMapping("/release")
    public ResponseEntity<StockDto> release(@Valid @RequestBody StockMovementRequest req) {
        return ResponseEntity.ok(service.release(req));
    }
}
