package cl.smartlogix.shipping.controller;

import cl.smartlogix.shipping.dto.CarrierDto;
import cl.smartlogix.shipping.dto.CreateCarrierRequest;
import cl.smartlogix.shipping.service.CarrierService;
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
 * Controlador REST para la gestión de transportistas (carriers).
 * Permite crear, consultar por ID y listar transportistas (con filtro opcional por activos).
 */
@RestController
@RequestMapping("/carriers")
@RequiredArgsConstructor
@Tag(name = "Transportistas", description = "Gestión de transportistas (carriers)")
public class CarrierController {

    private final CarrierService service;

    /**
     * Crea un nuevo transportista.
     *
     * @param req datos del transportista a crear
     * @return el transportista creado (HTTP 201)
     */
    @Operation(summary = "Crear transportista")
    @ApiResponse(responseCode = "201", description = "Transportista creado correctamente")
    @PostMapping
    public ResponseEntity<CarrierDto> create(@Valid @RequestBody CreateCarrierRequest req) {
        CarrierDto created = service.create(req);
        return ResponseEntity.created(URI.create("/carriers/" + created.carrierId())).body(created);
    }

    /**
     * Obtiene un transportista por su identificador.
     *
     * @param id identificador del transportista
     * @return el transportista encontrado
     */
    @Operation(summary = "Obtener transportista por ID")
    @ApiResponse(responseCode = "200", description = "Transportista encontrado")
    @ApiResponse(responseCode = "404", description = "Transportista no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<CarrierDto> getById(@Parameter(description = "ID del transportista") @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Lista los transportistas, opcionalmente solo los activos.
     *
     * @param active si es {@code true}, devuelve solo transportistas activos
     * @return listado de transportistas
     */
    @Operation(summary = "Listar transportistas", description = "Devuelve los transportistas, con filtro opcional por activos.")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @GetMapping
    public ResponseEntity<List<CarrierDto>> list(@Parameter(description = "Si es true, solo transportistas activos") @RequestParam(required = false) Boolean active) {
        List<CarrierDto> data = Boolean.TRUE.equals(active) ? service.findActive() : service.findAll();
        return ResponseEntity.ok(data);
    }
}
