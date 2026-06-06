package cl.smartlogix.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MovimientoRequest(
    @NotNull(message = "idProducto es obligatorio")
    Long idProducto,

    @NotNull(message = "idBodega es obligatoria")
    Long idBodega,

    @NotNull @Min(value = 1, message = "cantidad debe ser >= 1")
    Integer cantidad,

    @Size(max = 64)
    String referenciaPedido
) {}
