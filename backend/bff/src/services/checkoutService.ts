import { order } from "../clients/order.js";
import { inventory } from "../clients/inventory.js";
import { shipping } from "../clients/shipping.js";
import { UpstreamError } from "../clients/httpClient.js";

// Orchestration: create order → reserve stock per item → create shipment.
// If reservation fails, attempt to release already-made reservations (best-effort rollback).
export async function checkout(payload: any) {
  const order_ = await order.crear({
    customerId: payload.customerId,
    marketplaceId: payload.marketplaceId,
    type: payload.type,
    items: payload.items,
  });

  const reservasOk: any[] = [];
  try {
    for (const item of payload.items) {
      await inventory.reservar({
        productId: item.productId,
        warehouseId: payload.warehouseId,
        quantity: item.quantity,
        orderReference: order_.code,
      });
      reservasOk.push(item);
    }
  } catch (err: any) {
    await rollbackReservas(reservasOk, payload.warehouseId, order_.code);
    throw new UpstreamError(
      `Reserva de stock falló: ${err.message}. Reservas previas revertidas.`,
      { status: 409, service: "checkout", body: err.body }
    );
  }

  const shipment = await shipping.crear({
    orderId: order_.orderId,
    destinationAddress: payload.shipment.destinationAddress,
    district: payload.shipment.district,
    region: payload.shipment.region,
    estimatedDate: payload.shipment.estimatedDate,
  });

  return { order: order_, shipment };
}

async function rollbackReservas(reservas: any[], warehouseId: number | string, orderReference: string) {
  for (const item of reservas) {
    try {
      await inventory.liberar({
        productId: item.productId,
        warehouseId,
        quantity: item.quantity,
        orderReference,
      });
    } catch (err: any) {
      console.error("[bff] rollback release failed:", err.message);
    }
  }
}
