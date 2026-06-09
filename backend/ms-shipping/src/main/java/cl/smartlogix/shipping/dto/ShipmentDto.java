package cl.smartlogix.shipping.dto;

import cl.smartlogix.shipping.model.Envio;
import cl.smartlogix.shipping.model.EstadoEnvio;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record ShipmentDto(
    Long idEnvio,
    Long idPedido,
    Long idTransportista,
    String transportistaNombre,
    String trackingNumber,
    EstadoEnvio estado,
    String direccionDestino,
    String comuna,
    String region,
    LocalDate fechaEstimada,
    OffsetDateTime fechaEntrega,
    List<ShipmentTrackingDto> seguimiento,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static ShipmentDto from(Envio e) {
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
