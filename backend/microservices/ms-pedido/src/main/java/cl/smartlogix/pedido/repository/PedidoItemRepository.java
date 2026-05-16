package cl.smartlogix.pedido.repository;

import cl.smartlogix.pedido.model.PedidoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoItemRepository extends JpaRepository<PedidoItem, Long> {
    List<PedidoItem> findByPedido_IdPedido(Long idPedido);
}
