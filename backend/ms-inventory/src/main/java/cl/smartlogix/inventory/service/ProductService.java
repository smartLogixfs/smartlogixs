package cl.smartlogix.inventory.service;

import cl.smartlogix.inventory.dto.UpdateProductRequest;
import cl.smartlogix.inventory.dto.CreateProductRequest;
import cl.smartlogix.inventory.dto.ProductDto;

import java.util.List;

public interface ProductService {
    ProductDto crear(CreateProductRequest req);
    ProductDto findById(Long id);
    ProductDto findBySku(String sku);
    List<ProductDto> findAll();
    ProductDto actualizar(Long id, UpdateProductRequest req);
}
