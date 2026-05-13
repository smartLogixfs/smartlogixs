package cl.smartlogix.inventario.repository;

import cl.smartlogix.inventario.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByProducto_IdProductoAndBodega_IdBodega(Long idProducto, Long idBodega);

    List<Stock> findByProducto_IdProducto(Long idProducto);

    @Query("""
        SELECT s FROM Stock s
        WHERE s.cantidad - s.cantReservada <= s.stockMinimo
    """)
    List<Stock> findConStockBajo();

    @Query("""
        SELECT COALESCE(SUM(s.cantidad - s.cantReservada), 0)
        FROM Stock s
        WHERE s.producto.idProducto = :idProducto
    """)
    int sumDisponibleByProducto(@Param("idProducto") Long idProducto);
}
