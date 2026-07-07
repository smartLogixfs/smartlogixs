package cl.smartlogix.order.controller;

import cl.smartlogix.order.dto.UpdateOrderState;
import cl.smartlogix.order.dto.CreateOrderRequest;
import cl.smartlogix.order.dto.OrderDto;
import cl.smartlogix.order.model.OrderStatus;
import cl.smartlogix.order.service.OrderService;
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
 * Controlador REST para la gestión de pedidos.
 * Cubre creación, consulta (por ID, código o cliente), listado con filtro por estado
 * y transiciones de la máquina de estados del pedido.
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Gestión de pedidos, items y máquina de estados")
public class OrderController {

    private final OrderService service;

    /**
     * Crea un nuevo pedido con sus items.
     *
     * @param req datos del pedido a crear
     * @return el pedido creado (HTTP 201)
     */
    @Operation(summary = "Crear pedido", description = "Registra un nuevo pedido con sus items.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido creado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    public ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequest req) {
        OrderDto created = service.create(req);
        return ResponseEntity
            .created(URI.create("/orders/" + created.orderId()))
            .body(created);
    }

    /**
     * Obtiene un pedido por su identificador.
     *
     * @param id identificador del pedido
     * @return el pedido encontrado
     */
    @Operation(summary = "Obtener pedido por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getById(@Parameter(description = "ID del pedido") @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Obtiene un pedido por su código de negocio.
     *
     * @param code código del pedido
     * @return el pedido encontrado
     */
    @Operation(summary = "Obtener pedido por código")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @GetMapping("/code/{code}")
    public ResponseEntity<OrderDto> getByCode(@Parameter(description = "Código del pedido") @PathVariable String code) {
        return ResponseEntity.ok(service.findByCode(code));
    }

    /**
     * Lista los pedidos de un cliente.
     *
     * @param customerId identificador del cliente
     * @return listado de pedidos del cliente
     */
    @Operation(summary = "Listar pedidos por cliente")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDto>> getByCustomer(@Parameter(description = "ID del cliente") @PathVariable String customerId) {
        return ResponseEntity.ok(service.findByCustomer(customerId));
    }

    /**
     * Lista todos los pedidos, opcionalmente filtrados por estado.
     *
     * @param status estado por el que filtrar (opcional)
     * @return listado de pedidos
     */
    @Operation(summary = "Listar pedidos", description = "Devuelve todos los pedidos, con filtro opcional por estado.")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @GetMapping
    public ResponseEntity<List<OrderDto>> list(@Parameter(description = "Filtro opcional por estado") @RequestParam(required = false) OrderStatus status) {
        List<OrderDto> data = (status == null) ? service.findAll() : service.findByStatus(status);
        return ResponseEntity.ok(data);
    }

    /**
     * Cambia el estado de un pedido, avanzándolo en su máquina de estados.
     *
     * @param id  identificador del pedido
     * @param req nuevo estado solicitado
     * @return el pedido actualizado
     */
    @Operation(summary = "Cambiar estado del pedido", description = "Avanza el pedido en su máquina de estados.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
        @ApiResponse(responseCode = "409", description = "Transición de estado inválida")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDto> changeStatus(@Parameter(description = "ID del pedido") @PathVariable Long id,
                                                 @Valid @RequestBody UpdateOrderState req) {
        return ResponseEntity.ok(service.changeStatus(id, req));
    }
}
