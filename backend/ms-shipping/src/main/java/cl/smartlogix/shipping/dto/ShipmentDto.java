package cl.smartlogix.shipping.dto;

import cl.smartlogix.shipping.model.Shipment;
import cl.smartlogix.shipping.model.ShipmentState;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record ShipmentDto(
    @JsonProperty("shipmentId") Long idEnvio,
    @JsonProperty("orderId") Long idPedido,
    @JsonProperty("carrierId") Long idTransportista,
    @JsonProperty("carrierName") String transportistaNombre,
    String trackingNumber,
    @JsonProperty("status") ShipmentState estado,
    @JsonProperty("destinationAddress") String direccionDestino,
    @JsonProperty("district") String comuna,
    String region,
    @JsonProperty("estimatedDate") LocalDate fechaEstimada,
    @JsonProperty("deliveryDate") OffsetDateTime fechaEntrega,
    @JsonProperty("tracking") List<ShipmentTrackingDto> seguimiento,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static ShipmentDto from(Shipment e) {
        return new ShipmentDto(
            e.getIdEnvio(),
            e.getIdPedido(),
            e.getTransportista() != null ? e.getTransportista().getIdTransportista() : null,
            e.getTransportista() != null ? e.getTransportista().getNombre() : null,
            e.getTrackingNumber(),
            e.getEstado(),
            e.getDireccionDestino(),
            e.getComuna(),
            e.getRegion(),
            e.getFechaEstimada(),
            e.getFechaEntrega(),
            e.getSeguimiento().stream().map(ShipmentTrackingDto::from).toList(),
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }
}
