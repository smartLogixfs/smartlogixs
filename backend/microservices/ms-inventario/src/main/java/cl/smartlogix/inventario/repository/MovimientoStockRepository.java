package cl.smartlogix.inventario.repository;

import cl.smartlogix.inventario.model.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    List<MovimientoStock> findByStock_IdStockOrderByCreatedAtDesc(Long idStock);
    List<MovimientoStock> findByReferenciaPedido(String referenciaPedido);
}
