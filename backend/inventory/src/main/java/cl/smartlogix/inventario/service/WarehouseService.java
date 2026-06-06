package cl.smartlogix.inventario.service;

import cl.smartlogix.inventario.dto.CrearBodegaRequest;
import cl.smartlogix.inventario.dto.WarehouseDto;

import java.util.List;

public interface WarehouseService {
    WarehouseDto crear(CrearBodegaRequest req);
    WarehouseDto findById(Long id);
    List<WarehouseDto> findAll();
}
