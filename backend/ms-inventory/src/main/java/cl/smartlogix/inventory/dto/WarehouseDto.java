package cl.smartlogix.inventory.dto;

import cl.smartlogix.inventory.model.Warehouse;

public record WarehouseDto(
    Long warehouseId,
    String name,
    String location,
    Boolean active
) {
    public static WarehouseDto from(Warehouse w) {
        return new WarehouseDto(
            w.getId(),
            w.getName(),
            w.getLocation(),
            w.getActive()
        );
    }
}
