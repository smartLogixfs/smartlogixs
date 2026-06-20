package cl.smartlogix.order.repository;

import cl.smartlogix.order.model.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    List<OrderHistory> findByOrder_IdOrderByCreatedAtAsc(Long orderId);
}
