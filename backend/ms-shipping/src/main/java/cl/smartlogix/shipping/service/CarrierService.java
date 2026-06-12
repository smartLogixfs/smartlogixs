package cl.smartlogix.shipping.service;

import cl.smartlogix.shipping.dto.CarrierDto;
import cl.smartlogix.shipping.dto.CreateCarrierRequest;

import java.util.List;

public interface CarrierService {
    CarrierDto crear(CreateCarrierRequest req);
    CarrierDto findById(Long id);
    List<CarrierDto> findAll();
    List<CarrierDto> findActivos();
}
