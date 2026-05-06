package cl.smartlogix.inventario.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bodegas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bodega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBodega;

    private String nombre;

    private String ubicacion;
}
