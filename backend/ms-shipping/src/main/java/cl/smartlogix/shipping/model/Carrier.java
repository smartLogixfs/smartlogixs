package cl.smartlogix.shipping.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transportistas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transportista")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 120)
    private String name;

    @Column(unique = true, length = 20)
    private String rut;

    @Column(name = "telefono_contacto", length = 40)
    private String contactPhone;

    @Column(name = "activo", nullable = false)
    private Boolean active = Boolean.TRUE;
}
