package cl.smartlogix.inventory.service;

import cl.smartlogix.inventory.dto.CrearBodegaRequest;
import cl.smartlogix.inventory.dto.WarehouseDto;

import java.util.List;

public interface WarehouseService {
    WarehouseDto crear(CrearBodegaRequest req);
    WarehouseDto findById(Long id);
    List<WarehouseDto> findAll();
}
