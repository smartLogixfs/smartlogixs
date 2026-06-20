package cl.smartlogix.shipping.repository;

import cl.smartlogix.shipping.model.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarrierRepository extends JpaRepository<Carrier, Long> {
    Optional<Carrier> findByRut(String rut);
    List<Carrier> findByActiveTrue();
}
