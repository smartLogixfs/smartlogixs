package cl.smartlogix.inventario.service;

import cl.smartlogix.inventario.dto.ActualizarProductoRequest;
import cl.smartlogix.inventario.dto.CrearProductoRequest;
import cl.smartlogix.inventario.dto.ProductDto;
import cl.smartlogix.inventario.model.Producto;
import cl.smartlogix.inventario.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductoRepository repository;

    @Override
    public ProductDto crear(CrearProductoRequest req) {
        if (repository.existsBySku(req.sku())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU ya existe: " + req.sku());
        }
        Producto p = Producto.builder()
            .sku(req.sku())
            .nombre(req.nombre())
            .descripcion(req.descripcion())
            .precio(req.precio())
            .activo(Boolean.TRUE)
            .build();
        return ProductDto.from(repository.save(p));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        return ProductDto.from(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto findBySku(String sku) {
        Producto p = repository.findBySku(sku)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + sku));
        return ProductDto.from(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> findAll() {
        return repository.findAll().stream().map(ProductDto::from).toList();
    }

    @Override
    public ProductDto actualizar(Long id, ActualizarProductoRequest req) {
        Producto p = buscar(id);
        if (req.nombre() != null)      p.setNombre(req.nombre());
        if (req.descripcion() != null) p.setDescripcion(req.descripcion());
        if (req.precio() != null)      p.setPrecio(req.precio());
        if (req.activo() != null)      p.setActivo(req.activo());
        return ProductDto.from(repository.save(p));
    }

    private Producto buscar(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + id));
    }
}
