package cl.smartlogix.inventory.repository;

import cl.smartlogix.inventory.model.Bodega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<Bodega, Long> {
}
