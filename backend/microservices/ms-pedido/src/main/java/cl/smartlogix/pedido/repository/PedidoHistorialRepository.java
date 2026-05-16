package cl.smartlogix.pedido.repository;

import cl.smartlogix.pedido.model.PedidoHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoHistorialRepository extends JpaRepository<PedidoHistorial, Long> {
    List<PedidoHistorial> findByPedido_IdPedidoOrderByCreatedAtAsc(Long idPedido);
}
