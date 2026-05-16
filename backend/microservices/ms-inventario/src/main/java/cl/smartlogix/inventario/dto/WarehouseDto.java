package cl.smartlogix.inventario.dto;

import cl.smartlogix.inventario.model.Bodega;

public record WarehouseDto(
    Long idBodega,
    String nombre,
    String ubicacion,
    Boolean activo
) {
    public static WarehouseDto from(Bodega b) {
        return new WarehouseDto(
            b.getIdBodega(),
            b.getNombre(),
            b.getUbicacion(),
            b.getActivo()
        );
    }
}
