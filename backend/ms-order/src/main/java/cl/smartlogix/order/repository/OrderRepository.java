package cl.smartlogix.order.repository;

import cl.smartlogix.order.model.OrderStatus;
import cl.smartlogix.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByCodigo(String codigo);
    List<Order> findByEstado(OrderStatus estado);
    List<Order> findByIdCliente(String idCliente);
}
