package cl.smartlogix.shipping.dto;

import cl.smartlogix.shipping.model.ShipmentTracking;
import cl.smartlogix.shipping.model.ShipmentState;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record ShipmentTrackingDto(
    @JsonProperty("trackingId") Long idSeguimiento,
    @JsonProperty("status") ShipmentState estado,
    @JsonProperty("location") String ubicacion,
    @JsonProperty("comment") String comentario,
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
