package cl.smartlogix.inventory.controller;

import cl.smartlogix.inventory.dto.CreateWarehouseRequest;
import cl.smartlogix.inventory.dto.WarehouseDto;
import cl.smartlogix.inventory.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para la gestión de bodegas (almacenes).
 * Permite crear, consultar por ID y listar bodegas.
 */
@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
@Tag(name = "Bodegas", description = "Gestión de bodegas / almacenes")
public class WarehouseController {

    private final WarehouseService service;

    /**
     * Crea una nueva bodega.
     *
     * @param req datos de la bodega a crear
     * @return la bodega creada (HTTP 201)
     */
    @Operation(summary = "Crear bodega", description = "Registra una nueva bodega.")
    @ApiResponse(responseCode = "201", description = "Bodega creada correctamente")
    @PostMapping
    public ResponseEntity<WarehouseDto> create(@Valid @RequestBody CreateWarehouseRequest req) {
        WarehouseDto created = service.create(req);
        return ResponseEntity.created(URI.create("/warehouses/" + created.warehouseId())).body(created);
    }

    /**
     * Obtiene una bodega por su identificador.
     *
     * @param id identificador de la bodega
     * @return la bodega encontrada
     */
    @Operation(summary = "Obtener bodega por ID")
    @ApiResponse(responseCode = "200", description = "Bodega encontrada")
    @ApiResponse(responseCode = "404", description = "Bodega no encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<WarehouseDto> getById(@Parameter(description = "ID de la bodega") @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Lista todas las bodegas.
     *
     * @return el listado de bodegas
     */
    @Operation(summary = "Listar bodegas")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @GetMapping
    public ResponseEntity<List<WarehouseDto>> list() {
        return ResponseEntity.ok(service.findAll());
    }
}
