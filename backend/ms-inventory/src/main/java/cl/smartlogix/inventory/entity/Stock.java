package cl.smartlogix.inventory.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
    name = "stock",
    uniqueConstraints = @UniqueConstraint(name = "uk_stock_producto_bodega", columnNames = {"id_producto", "id_bodega"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_stock")
    private Long idStock;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Product producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_bodega", nullable = false)
    private Warehouse bodega;

    @Column(nullable = false)
    private Integer cantidad = 0;

    @Column(name = "cant_reservada", nullable = false)
    private Integer cantReservada = 0;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 0;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = OffsetDateTime.now();
    }

    @Transient
    public int getDisponible() {
        return Math.max(0, this.cantidad - this.cantReservada);
    }
}
