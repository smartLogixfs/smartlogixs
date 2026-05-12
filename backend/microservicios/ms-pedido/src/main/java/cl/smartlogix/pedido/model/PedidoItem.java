package cl.smartlogix.pedido.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pedido_items", indexes = {
    @Index(name = "idx_items_pedido", columnList = "id_pedido")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Long idItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    /** ID lógico del producto en ms-inventario — NO es FK física. */
    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(nullable = false, length = 64)
    private String sku;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
