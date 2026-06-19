import { order } from "../clients/order.js";
import { inventory } from "../clients/inventory.js";
import { shipping } from "../clients/shipping.js";
import { UpstreamError } from "../clients/httpClient.js";

// Orchestration: create order → reserve stock per item → create shipment.
// If reservation fails, attempt to release already-made reservations (best-effort rollback).
export async function checkout(payload: any) {
  const createdOrder = await order.create({
    customerId: payload.customerId,
    marketplaceId: payload.marketplaceId,
    type: payload.type,
    items: payload.items,
  });

  const reservedItems: any[] = [];
  try {
    for (const item of payload.items) {
      await inventory.reserve({
        productId: item.productId,
        warehouseId: payload.warehouseId,
        quantity: item.quantity,
        orderReference: createdOrder.code,
      });
      reservedItems.push(item);
    }
  } catch (err: any) {
    await rollbackReservations(reservedItems, payload.warehouseId, createdOrder.code);
    throw new UpstreamError(
      `Reserva de stock falló: ${err.message}. Reservas previas revertidas.`,
      { status: 409, service: "checkout", body: err.body }
    );
  }

  const shipment = await shipping.create({
    orderId: createdOrder.orderId,
    destinationAddress: payload.shipment.destinationAddress,
    district: payload.shipment.district,
    region: payload.shipment.region,
    estimatedDate: payload.shipment.estimatedDate,
  });

  return { order: createdOrder, shipment };
}

async function rollbackReservations(items: any[], warehouseId: number | string, orderReference: string) {
  for (const item of items) {
    try {
      await inventory.release({
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
