package cl.smartlogix.envio.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transportistas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transportista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transportista")
    private Long idTransportista;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(unique = true, length = 20)
    private String rut;

    @Column(name = "telefono_contacto", length = 40)
    private String telefonoContacto;

    @Column(nullable = false)
    private Boolean activo = Boolean.TRUE;
}
