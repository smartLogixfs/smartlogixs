package cl.smartlogix.shipping.service;

import cl.smartlogix.shipping.dto.CarrierDto;
import cl.smartlogix.shipping.dto.CreateCarrierRequest;
import cl.smartlogix.shipping.model.Carrier;
import cl.smartlogix.shipping.repository.CarrierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CarrierServiceImpl implements CarrierService {

    private final CarrierRepository repository;

    @Override
    public CarrierDto crear(CreateCarrierRequest req) {
        if (req.rut() != null && repository.findByRut(req.rut()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "RUT ya registrado: " + req.rut());
        }
        Carrier t = Carrier.builder()
            .nombre(req.nombre())
            .rut(req.rut())
            .telefonoContacto(req.telefonoContacto())
            .activo(Boolean.TRUE)
            .build();
        return CarrierDto.from(repository.save(t));
    }

    @Override
    @Transactional(readOnly = true)
    public CarrierDto findById(Long id) {
        return CarrierDto.from(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarrierDto> findAll() {
        return repository.findAll().stream().map(CarrierDto::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarrierDto> findActivos() {
        return repository.findByActivoTrue().stream().map(CarrierDto::from).toList();
    }

    private Carrier buscar(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transportista no encontrado: " + id));
    }
}
