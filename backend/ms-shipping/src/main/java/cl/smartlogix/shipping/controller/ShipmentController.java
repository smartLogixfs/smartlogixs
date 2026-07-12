package cl.smartlogix.shipping.controller;

import cl.smartlogix.shipping.dto.UpdateShipmentRStatusRequest;
import cl.smartlogix.shipping.dto.AssingCarierRequest;
import cl.smartlogix.shipping.dto.CreateShipmentRequest;
import cl.smartlogix.shipping.dto.ShipmentDto;
import cl.smartlogix.shipping.dto.ShipmentTrackingDto;
import cl.smartlogix.shipping.model.ShipmentState;
import cl.smartlogix.shipping.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para la gestión de envíos y su seguimiento.
 * Cubre creación, consulta (por ID, número de seguimiento o pedido), listado con
 * filtro por estado, historial de tracking, asignación de transportista y cambios de estado.
 */
@RestController
@RequestMapping("/shipments")
@RequiredArgsConstructor
@Tag(name = "Envíos", description = "Gestión de envíos y seguimiento (tracking)")
public class ShipmentController {

    private final ShipmentService service;

    /**
     * Crea un nuevo envío asociado a un pedido.
     *
     * @param req datos del envío a crear
     * @return el envío creado (HTTP 201)
     */
    @Operation(summary = "Crear envío", description = "Genera un nuevo envío asociado a un pedido.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Envío creado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    public ResponseEntity<ShipmentDto> create(@Valid @RequestBody CreateShipmentRequest req) {
        ShipmentDto created = service.create(req);
        return ResponseEntity.created(URI.create("/shipments/" + created.shipmentId())).body(created);
    }

    /**
     * Obtiene un envío por su identificador.
     *
     * @param id identificador del envío
     * @return el envío encontrado
     */
    @Operation(summary = "Obtener envío por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Envío encontrado"),
        @ApiResponse(responseCode = "404", description = "Envío no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDto> getById(@Parameter(description = "ID del envío") @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Obtiene un envío por su número de seguimiento.
     *
     * @param trackingNumber número de seguimiento del envío
     * @return el envío encontrado
     */
    @Operation(summary = "Obtener envío por número de seguimiento")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Envío encontrado"),
        @ApiResponse(responseCode = "404", description = "Envío no encontrado")
    })
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ShipmentDto> getByTracking(@Parameter(description = "Número de seguimiento") @PathVariable String trackingNumber) {
        return ResponseEntity.ok(service.findByTracking(trackingNumber));
    }

    /**
     * Lista los envíos asociados a un pedido.
     *
     * @param orderId identificador del pedido
     * @return listado de envíos del pedido
     */
    @Operation(summary = "Listar envíos por pedido")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ShipmentDto>> getByOrder(@Parameter(description = "ID del pedido") @PathVariable Long orderId) {
        return ResponseEntity.ok(service.findByOrder(orderId));
    }

    /**
     * Lista todos los envíos, opcionalmente filtrados por estado.
     *
     * @param status estado por el que filtrar (opcional)
     * @return listado de envíos
     */
    @Operation(summary = "Listar envíos", description = "Devuelve todos los envíos, con filtro opcional por estado.")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @GetMapping
    public ResponseEntity<List<ShipmentDto>> list(@Parameter(description = "Filtro opcional por estado") @RequestParam(required = false) ShipmentState status) {
        List<ShipmentDto> data = (status == null) ? service.findAll() : service.findByStatus(status);
        return ResponseEntity.ok(data);
    }

    /**
     * Devuelve el historial de eventos de tracking de un envío.
     *
     * @param id identificador del envío
     * @return listado de eventos de seguimiento
     */
    @Operation(summary = "Historial de seguimiento", description = "Devuelve los eventos de tracking de un envío.")
    @ApiResponse(responseCode = "200", description = "Historial obtenido correctamente")
    @GetMapping("/{id}/tracking-history")
    public ResponseEntity<List<ShipmentTrackingDto>> history(@Parameter(description = "ID del envío") @PathVariable Long id) {
        return ResponseEntity.ok(service.history(id));
    }

    /**
     * Asigna un transportista a un envío.
     *
     * @param id  identificador del envío
     * @param req datos del transportista a asignar
     * @return el envío actualizado
     */
    @Operation(summary = "Asignar transportista", description = "Asocia un transportista a un envío.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transportista asignado correctamente"),
        @ApiResponse(responseCode = "404", description = "Envío no encontrado")
    })
    @PatchMapping("/{id}/carrier")
    public ResponseEntity<ShipmentDto> assignCarrier(@Parameter(description = "ID del envío") @PathVariable Long id,
                                                    @Valid @RequestBody AssingCarierRequest req) {
        return ResponseEntity.ok(service.assignCarrier(id, req));
    }

    /**
     * Cambia el estado de un envío y registra el evento de tracking correspondiente.
     *
     * @param id  identificador del envío
     * @param req nuevo estado y datos del evento
     * @return el envío actualizado
     */
    @Operation(summary = "Cambiar estado del envío", description = "Actualiza el estado del envío y registra un evento de tracking.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Envío no encontrado")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ShipmentDto> changeStatus(@Parameter(description = "ID del envío") @PathVariable Long id,
                                                    @Valid @RequestBody UpdateShipmentRStatusRequest req) {
        return ResponseEntity.ok(service.changeStatus(id, req));
    }
}
