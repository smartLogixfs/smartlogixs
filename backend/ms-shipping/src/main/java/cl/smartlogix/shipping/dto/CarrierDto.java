package cl.smartlogix.shipping.dto;

import cl.smartlogix.shipping.model.Carrier;

public record CarrierDto(
    Long carrierId,
    String name,
    String rut,
    String contactPhone,
    Boolean active
) {
    public static CarrierDto from(Carrier c) {
        return new CarrierDto(
            c.getId(),
            c.getName(),
            c.getRut(),
            c.getContactPhone(),
            c.getActive()
        );
    }
}
