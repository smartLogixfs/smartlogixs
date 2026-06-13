package cl.smartlogix.shipping.dto;

import cl.smartlogix.shipping.model.ShipmentTracking;
import cl.smartlogix.shipping.model.ShipmentState;

import java.time.OffsetDateTime;

public record ShipmentTrackingDto(
    Long idSeguimiento,
    ShipmentState estado,
    String ubicacion,
    String comentario,
    OffsetDateTime createdAt
) {
    public static ShipmentTrackingDto from(ShipmentTracking s) {
        return new ShipmentTrackingDto(
            s.getIdSeguimiento(),
            s.getEstado(),
            s.getUbicacion(),
            s.getComentario(),
            s.getCreatedAt()
        );
    }
}
