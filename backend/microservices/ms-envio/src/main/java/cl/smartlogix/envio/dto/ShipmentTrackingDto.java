package cl.smartlogix.envio.dto;

import cl.smartlogix.envio.model.EnvioSeguimiento;
import cl.smartlogix.envio.model.EstadoEnvio;

import java.time.OffsetDateTime;

public record ShipmentTrackingDto(
    Long idSeguimiento,
    EstadoEnvio estado,
    String ubicacion,
    String comentario,
    OffsetDateTime createdAt
) {
    public static ShipmentTrackingDto from(EnvioSeguimiento s) {
        return new ShipmentTrackingDto(
            s.getIdSeguimiento(),
            s.getEstado(),
            s.getUbicacion(),
            s.getComentario(),
            s.getCreatedAt()
        );
    }
}
