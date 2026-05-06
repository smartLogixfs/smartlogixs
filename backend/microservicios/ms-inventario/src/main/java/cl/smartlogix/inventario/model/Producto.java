package cl.smartlogix.inventario.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    private String nombre;

    private String descripcion;

    @Column(unique = true)
    private String sku;

    private Double precio;

    private Boolean activo;
}
