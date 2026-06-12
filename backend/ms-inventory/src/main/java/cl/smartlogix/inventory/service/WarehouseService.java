package cl.smartlogix.inventory.service;

import cl.smartlogix.inventory.dto.CreateWarehouseRequest;
import cl.smartlogix.inventory.dto.WarehouseDto;

import java.util.List;

public interface WarehouseService {
    WarehouseDto crear(CreateWarehouseRequest req);
    WarehouseDto findById(Long id);
    List<WarehouseDto> findAll();
}
