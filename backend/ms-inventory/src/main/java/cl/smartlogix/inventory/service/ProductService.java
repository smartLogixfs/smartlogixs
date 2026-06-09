package cl.smartlogix.inventory.service;

import cl.smartlogix.inventory.dto.ActualizarProductoRequest;
import cl.smartlogix.inventory.dto.CrearProductoRequest;
import cl.smartlogix.inventory.dto.ProductDto;

import java.util.List;

public interface ProductService {
    ProductDto crear(CrearProductoRequest req);
    ProductDto findById(Long id);
    ProductDto findBySku(String sku);
    List<ProductDto> findAll();
    ProductDto actualizar(Long id, ActualizarProductoRequest req);
}
