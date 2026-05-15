package cl.smartlogix.envio.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "envios", indexes = {
    @Index(name = "idx_envios_pedido", columnList = "id_pedido")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_envio")
    private Long idEnvio;

    /** ID lógico del pedido en ms-pedido — NO es FK física. */
    @Column(name = "id_pedido", nullable = false)
    private Long idPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transportista")
    private Transportista transportista;

    @Column(name = "tracking_number", unique = true, length = 60)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEnvio estado = EstadoEnvio.CREADO;

    @Column(name = "direccion_destino", nullable = false, length = 255)
    private String direccionDestino;

    @Column(length = 120)
    private String comuna;

    @Column(length = 120)
    private String region;

    @Column(name = "fecha_estimada")
    private LocalDate fechaEstimada;

    @Column(name = "fecha_entrega")
    private OffsetDateTime fechaEntrega;

    @OneToMany(mappedBy = "envio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EnvioSeguimiento> seguimiento = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public void addSeguimiento(EnvioSeguimiento s) {
        s.setEnvio(this);
        this.seguimiento.add(s);
    }
}
