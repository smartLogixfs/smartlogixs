package cl.smartlogix.order.repository;

import cl.smartlogix.order.model.PedidoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<PedidoItem, Long> {
    List<PedidoItem> findByPedido_IdPedido(Long idPedido);
}
