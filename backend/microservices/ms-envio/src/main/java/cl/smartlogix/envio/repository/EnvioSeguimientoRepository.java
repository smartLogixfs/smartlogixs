package cl.smartlogix.envio.repository;

import cl.smartlogix.envio.model.EnvioSeguimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvioSeguimientoRepository extends JpaRepository<EnvioSeguimiento, Long> {
    List<EnvioSeguimiento> findByEnvio_IdEnvioOrderByCreatedAtAsc(Long idEnvio);
}
