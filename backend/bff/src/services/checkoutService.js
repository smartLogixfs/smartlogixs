import { order } from "../clients/order.js";
import { inventory } from "../clients/inventory.js";
import { shipping } from "../clients/shipping.js";
import { UpstreamError } from "../clients/httpClient.js";

// Orquestación: crear pedido → reservar stock por ítem → crear envío.
// Si falla la reserva, se intenta liberar las reservas ya hechas (rollback best-effort).
export async function checkout(payload) {
  const pedido = await order.crear({
    idCliente: payload.idCliente,
    idMarketplace: payload.idMarketplace,
    tipo: payload.tipo,
    items: payload.items,
  });

  const reservasOk = [];
  try {
    for (const item of payload.items) {
      await inventory.reservar({
        idProducto: item.idProducto,
        idBodega: payload.idBodega,
        cantidad: item.cantidad,
        referenciaPedido: pedido.codigo,
      });
      reservasOk.push(item);
    }
  } catch (err) {
    await rollbackReservas(reservasOk, payload.idBodega, pedido.codigo);
    throw new UpstreamError(
      `Reserva de stock falló: ${err.message}. Reservas previas revertidas.`,
      { status: 409, service: "checkout", body: err.body }
    );
  }

  const envio = await shipping.crear({
    idPedido: pedido.idPedido,
    direccionDestino: payload.envio.direccionDestino,
    comuna: payload.envio.comuna,
    region: payload.envio.region,
    fechaEstimada: payload.envio.fechaEstimada,
  });

  return { pedido, envio };
}

async function rollbackReservas(reservas, idBodega, referenciaPedido) {
  for (const item of reservas) {
    try {
      await inventory.liberar({
        idProducto: item.idProducto,
        idBodega,
        cantidad: item.cantidad,
        referenciaPedido,
      });
    } catch (err) {
      console.error("[bff] rollback liberar falló:", err.message);
    }
  }
}
