package cl.smartlogix.shipping.dto;

import cl.smartlogix.shipping.model.Transportista;

public record CarrierDto(
    Long idTransportista,
    String nombre,
    String rut,
    String telefonoContacto,
    Boolean activo
) {
    public static CarrierDto from(Transportista t) {
        return new CarrierDto(
            t.getIdTransportista(),
            t.getNombre(),
            t.getRut(),
            t.getTelefonoContacto(),
            t.getActivo()
        );
    }
}
