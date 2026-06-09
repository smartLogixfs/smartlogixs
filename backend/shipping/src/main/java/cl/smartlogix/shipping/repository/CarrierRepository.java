package cl.smartlogix.shipping.repository;

import cl.smartlogix.shipping.model.Transportista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarrierRepository extends JpaRepository<Transportista, Long> {
    Optional<Transportista> findByRut(String rut);
    List<Transportista> findByActivoTrue();
}
