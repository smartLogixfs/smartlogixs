package cl.smartlogix.inventory.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bodegas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bodega")
    private Long idBodega;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 255)
    private String ubicacion;

    @Column(nullable = false)
    private Boolean activo = Boolean.TRUE;
}
