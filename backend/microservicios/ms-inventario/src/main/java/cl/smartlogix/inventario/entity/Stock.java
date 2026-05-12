package cl.smartlogix.inventario.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;


@Entity
@Table(name = "stock")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Stock {
    @Entity
@Table(name = "stock")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idStock;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "id_bodega")
    private Bodega bodega;

    private Integer cantidad;

    private Integer cantReservada;

    private Integer stockMinimo;

    private OffsetDateTime updatedAt;

    
}
