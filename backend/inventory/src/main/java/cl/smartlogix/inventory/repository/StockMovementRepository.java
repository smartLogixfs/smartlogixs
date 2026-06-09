package cl.smartlogix.inventory.repository;

import cl.smartlogix.inventory.model.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<MovimientoStock, Long> {
    List<MovimientoStock> findByStock_IdStockOrderByCreatedAtDesc(Long idStock);
    List<MovimientoStock> findByReferenciaPedido(String referenciaPedido);
}
