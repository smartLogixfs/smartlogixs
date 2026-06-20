package cl.smartlogix.shipping.dto;

import cl.smartlogix.shipping.model.Shipment;
import cl.smartlogix.shipping.model.ShipmentState;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record ShipmentDto(
    Long shipmentId,
    Long orderId,
    Long carrierId,
    String carrierName,
    String trackingNumber,
    ShipmentState status,
    String destinationAddress,
    String district,
    String region,
    LocalDate estimatedDate,
    OffsetDateTime deliveryDate,
    List<ShipmentTrackingDto> tracking,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static ShipmentDto from(Shipment s) {
        return new ShipmentDto(
            s.getId(),
            s.getOrderId(),
            s.getCarrier() != null ? s.getCarrier().getId() : null,
            s.getCarrier() != null ? s.getCarrier().getName() : null,
            s.getTrackingNumber(),
            s.getStatus(),
            s.getDestinationAddress(),
            s.getDistrict(),
            s.getRegion(),
            s.getEstimatedDate(),
            s.getDeliveryDate(),
            s.getTracking().stream().map(ShipmentTrackingDto::from).toList(),
            s.getCreatedAt(),
            s.getUpdatedAt()
        );
    }
}
