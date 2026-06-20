package cl.smartlogix.shipping.dto;

import cl.smartlogix.shipping.model.ShipmentTracking;
import cl.smartlogix.shipping.model.ShipmentState;

import java.time.OffsetDateTime;

public record ShipmentTrackingDto(
    Long trackingId,
    ShipmentState status,
    String location,
    String comment,
    OffsetDateTime createdAt
) {
    public static ShipmentTrackingDto from(ShipmentTracking t) {
        return new ShipmentTrackingDto(
            t.getId(),
            t.getStatus(),
            t.getLocation(),
            t.getComment(),
            t.getCreatedAt()
        );
    }
}
