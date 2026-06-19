package cl.smartlogix.shipping.dto;

import cl.smartlogix.shipping.model.Carrier;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CarrierDto(
    @JsonProperty("carrierId") Long idTransportista,
    @JsonProperty("name") String nombre,
    String rut,
    @JsonProperty("contactPhone") String telefonoContacto,
    @JsonProperty("active") Boolean activo
) {
    public static CarrierDto from(Carrier t) {
        return new CarrierDto(
            t.getIdTransportista(),
            t.getNombre(),
            t.getRut(),
            t.getTelefonoContacto(),
            t.getActivo()
        );
    }
}
