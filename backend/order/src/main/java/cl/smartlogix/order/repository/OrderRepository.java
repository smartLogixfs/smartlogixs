package cl.smartlogix.order.repository;

import cl.smartlogix.order.model.EstadoPedido;
import cl.smartlogix.order.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByCodigo(String codigo);
    List<Pedido> findByEstado(EstadoPedido estado);
    List<Pedido> findByIdCliente(String idCliente);
}
