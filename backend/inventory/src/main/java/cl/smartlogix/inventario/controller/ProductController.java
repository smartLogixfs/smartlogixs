package cl.smartlogix.inventario.controller;

import cl.smartlogix.inventario.dto.ActualizarProductoRequest;
import cl.smartlogix.inventario.dto.CrearProductoRequest;
import cl.smartlogix.inventario.dto.ProductDto;
import cl.smartlogix.inventario.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

// Path: /productos — dominio en español, clase en inglés (mismo patrón que OrderController).
@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PostMapping
    public ResponseEntity<ProductDto> crear(@Valid @RequestBody CrearProductoRequest req) {
        ProductDto creado = service.crear(req);
        return ResponseEntity.created(URI.create("/productos/" + creado.idProducto())).body(creado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductDto> getBySku(@PathVariable String sku) {
        return ResponseEntity.ok(service.findBySku(sku));
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> listar() {
        return ResponseEntity.ok(service.findAll());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductDto> actualizar(@PathVariable Long id,
                                                 @Valid @RequestBody ActualizarProductoRequest req) {
        return ResponseEntity.ok(service.actualizar(id, req));
    }
}
