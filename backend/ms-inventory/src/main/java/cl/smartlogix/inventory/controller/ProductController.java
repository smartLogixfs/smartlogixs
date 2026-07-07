package cl.smartlogix.inventory.controller;

import cl.smartlogix.inventory.dto.UpdateProductRequest;
import cl.smartlogix.inventory.dto.CreateProductRequest;
import cl.smartlogix.inventory.dto.ProductDto;
import cl.smartlogix.inventory.service.ProductService;
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
 * Controlador REST para la gestión del catálogo de productos.
 * Expone operaciones de alta, consulta (por ID o SKU), listado y actualización parcial.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Gestión del catálogo de productos del inventario")
public class ProductController {

    private final ProductService service;

    /**
     * Crea un nuevo producto en el catálogo.
     *
     * @param req datos del producto a crear
     * @return el producto creado con su ubicación (HTTP 201)
     */
    @Operation(summary = "Crear producto", description = "Registra un nuevo producto en el catálogo.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Producto creado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    public ResponseEntity<ProductDto> create(@Valid @RequestBody CreateProductRequest req) {
        ProductDto created = service.create(req);
        return ResponseEntity.created(URI.create("/products/" + created.productId())).body(created);
    }

    /**
     * Obtiene un producto por su identificador.
     *
     * @param id identificador del producto
     * @return el producto encontrado
     */
    @Operation(summary = "Obtener producto por ID", description = "Devuelve un producto según su identificador.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto encontrado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(@Parameter(description = "ID del producto") @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Obtiene un producto por su código SKU.
     *
     * @param sku código SKU del producto
     * @return el producto encontrado
     */
    @Operation(summary = "Obtener producto por SKU", description = "Devuelve un producto según su código SKU.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto encontrado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductDto> getBySku(@Parameter(description = "Código SKU del producto") @PathVariable String sku) {
        return ResponseEntity.ok(service.findBySku(sku));
    }

    /**
     * Lista todos los productos del catálogo.
     *
     * @return el listado completo de productos
     */
    @Operation(summary = "Listar productos", description = "Devuelve todos los productos del catálogo.")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @GetMapping
    public ResponseEntity<List<ProductDto>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Actualiza parcialmente un producto (por ejemplo, activarlo o desactivarlo).
     *
     * @param id  identificador del producto
     * @param req campos a modificar
     * @return el producto actualizado
     */
    @Operation(summary = "Actualizar producto", description = "Modifica parcialmente los datos de un producto (ej. activar/desactivar).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ProductDto> update(@Parameter(description = "ID del producto") @PathVariable Long id,
                                             @Valid @RequestBody UpdateProductRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }
}
