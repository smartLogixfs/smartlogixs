package cl.smartlogix.shipping.repository;

import cl.smartlogix.shipping.model.EnvioSeguimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentTrackingRepository extends JpaRepository<EnvioSeguimiento, Long> {
    List<EnvioSeguimiento> findByEnvio_IdEnvioOrderByCreatedAtAsc(Long idEnvio);
}
