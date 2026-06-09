package cl.smartlogix.shipping.repository;

import cl.smartlogix.shipping.model.Envio;
import cl.smartlogix.shipping.model.EstadoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Envio, Long> {
    Optional<Envio> findByTrackingNumber(String trackingNumber);
    List<Envio> findByIdPedido(Long idPedido);
    List<Envio> findByEstado(EstadoEnvio estado);
}
