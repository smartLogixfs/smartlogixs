package cl.smartlogix.inventory.dto;

import cl.smartlogix.inventory.model.Warehouse;
import com.fasterxml.jackson.annotation.JsonProperty;

public record WarehouseDto(
    @JsonProperty("warehouseId") Long idBodega,
    @JsonProperty("name") String nombre,
    @JsonProperty("location") String ubicacion,
    @JsonProperty("active") Boolean activo
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
