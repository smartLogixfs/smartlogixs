package cl.smartlogix.envio.service;

import cl.smartlogix.envio.dto.CarrierDto;
import cl.smartlogix.envio.dto.CrearTransportistaRequest;

import java.util.List;

public interface CarrierService {
    CarrierDto crear(CrearTransportistaRequest req);
    CarrierDto findById(Long id);
    List<CarrierDto> findAll();
    List<CarrierDto> findActivos();
}
