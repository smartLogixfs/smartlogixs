package cl.smartlogix.inventory.repository;

import cl.smartlogix.inventory.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findBySku(String sku);
    boolean existsBySku(String sku);
}
