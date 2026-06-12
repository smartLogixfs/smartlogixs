package cl.smartlogix.inventory.service;

import cl.smartlogix.inventory.dto.CreateWarehouseRequest;
import cl.smartlogix.inventory.dto.WarehouseDto;
import cl.smartlogix.inventory.model.Warehouse;
import cl.smartlogix.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository repository;

    @Override
    public WarehouseDto crear(CreateWarehouseRequest req) {
        Warehouse b = Warehouse.builder()
            .nombre(req.nombre())
            .ubicacion(req.ubicacion())
            .activo(Boolean.TRUE)
            .build();
        return WarehouseDto.from(repository.save(b));
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseDto findById(Long id) {
        return WarehouseDto.from(repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bodega no encontrada: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDto> findAll() {
        return repository.findAll().stream().map(WarehouseDto::from).toList();
    }
}
