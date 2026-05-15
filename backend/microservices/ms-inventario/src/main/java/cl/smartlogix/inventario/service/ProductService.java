package cl.smartlogix.inventario.service;

import cl.smartlogix.inventario.dto.ActualizarProductoRequest;
import cl.smartlogix.inventario.dto.CrearProductoRequest;
import cl.smartlogix.inventario.dto.ProductDto;

import java.util.List;

public interface ProductService {
    ProductDto crear(CrearProductoRequest req);
    ProductDto findById(Long id);
    ProductDto findBySku(String sku);
    List<ProductDto> findAll();
    ProductDto actualizar(Long id, ActualizarProductoRequest req);
}
