package cl.smartlogix.inventory.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "movimientos_stock", indexes = {
    @Index(name = "idx_mov_stock", columnList = "id_stock")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_stock", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private MovementType type;

    @Column(name = "cantidad", nullable = false)
    private Integer quantity;

    /** ID lógico del pedido (ms-order) — NO es FK física. */
    @Column(name = "referencia_pedido", length = 64)
    private String orderReference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
