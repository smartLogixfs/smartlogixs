package cl.smartlogix.inventory.repository;

import cl.smartlogix.inventory.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByProduct_IdAndWarehouse_Id(Long productId, Long warehouseId);

    List<Stock> findByProduct_Id(Long productId);

    @Query("""
        SELECT s FROM Stock s
        WHERE s.quantity - s.reservedQuantity <= s.minStock
    """)
    List<Stock> findLowStock();

    @Query("""
        SELECT COALESCE(SUM(s.quantity - s.reservedQuantity), 0)
        FROM Stock s
        WHERE s.product.id = :productId
    """)
    int sumAvailableByProduct(@Param("productId") Long productId);
}
