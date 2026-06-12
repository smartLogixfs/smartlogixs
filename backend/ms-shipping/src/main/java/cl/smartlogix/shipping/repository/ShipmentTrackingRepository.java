package cl.smartlogix.shipping.repository;

import cl.smartlogix.shipping.model.ShipmentTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentTrackingRepository extends JpaRepository<ShipmentTracking, Long> {
    List<ShipmentTracking> findByEnvio_IdEnvioOrderByCreatedAtAsc(Long idEnvio);
}
