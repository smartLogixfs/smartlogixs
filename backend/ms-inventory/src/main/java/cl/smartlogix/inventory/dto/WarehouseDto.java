package cl.smartlogix.inventory.dto;

import cl.smartlogix.inventory.model.Warehouse;

public record WarehouseDto(
    Long idBodega,
    String nombre,
    String ubicacion,
    Boolean activo
) {
    public static WarehouseDto from(Warehouse b) {
        return new WarehouseDto(
            b.getIdBodega(),
            b.getNombre(),
            b.getUbicacion(),
            b.getActivo()
        );
    }
}
