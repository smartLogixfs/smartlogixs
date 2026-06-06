package cl.smartlogix.pedido.repository;

import cl.smartlogix.pedido.model.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    List<OrderHistory> findByPedido_IdPedidoOrderByCreatedAtAsc(Long idPedido);
}
