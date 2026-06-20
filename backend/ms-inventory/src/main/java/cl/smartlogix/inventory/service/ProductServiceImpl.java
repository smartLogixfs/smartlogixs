package cl.smartlogix.inventory.service;

import cl.smartlogix.inventory.dto.UpdateProductRequest;
import cl.smartlogix.inventory.dto.CreateProductRequest;
import cl.smartlogix.inventory.dto.ProductDto;
import cl.smartlogix.inventory.model.Product;
import cl.smartlogix.inventory.repository.ProductRepository;
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

    private final ProductRepository repository;

    @Override
    public ProductDto create(CreateProductRequest req) {
        if (repository.existsBySku(req.sku())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU ya existe: " + req.sku());
        }
        Product p = Product.builder()
            .sku(req.sku())
            .name(req.name())
            .description(req.description())
            .price(req.price())
            .active(Boolean.TRUE)
            .build();
        return ProductDto.from(repository.save(p));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        return ProductDto.from(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto findBySku(String sku) {
        Product p = repository.findBySku(sku)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + sku));
        return ProductDto.from(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> findAll() {
        return repository.findAll().stream().map(ProductDto::from).toList();
    }

    @Override
    public ProductDto update(Long id, UpdateProductRequest req) {
        Product p = find(id);
        if (req.name() != null)        p.setName(req.name());
        if (req.description() != null) p.setDescription(req.description());
        if (req.price() != null)       p.setPrice(req.price());
        if (req.active() != null)      p.setActive(req.active());
        return ProductDto.from(repository.save(p));
    }

    private Product find(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + id));
    }
}
