package cl.smartlogix.inventory.controller;

import cl.smartlogix.inventory.dto.UpdateProductRequest;
import cl.smartlogix.inventory.dto.CreateProductRequest;
import cl.smartlogix.inventory.dto.ProductDto;
import cl.smartlogix.inventory.service.ProductService;
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
    public ResponseEntity<ProductDto> crear(@Valid @RequestBody CreateProductRequest req) {
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
                                                 @Valid @RequestBody UpdateProductRequest req) {
        return ResponseEntity.ok(service.actualizar(id, req));
    }
}
