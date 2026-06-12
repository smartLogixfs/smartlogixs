package cl.smartlogix.inventory.repository;

import cl.smartlogix.inventory.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByStock_IdStockOrderByCreatedAtDesc(Long idStock);
    List<StockMovement> findByReferenciaPedido(String referenciaPedido);
}
