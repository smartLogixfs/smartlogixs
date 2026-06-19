import { order } from "../clients/order.js";
import { inventory } from "../clients/inventory.js";
import { shipping } from "../clients/shipping.js";

// GET /orders/:id/full → order + associated shipments + aggregated availability per product.
export async function orderFull(orderId: number | string) {
  const ord = await order.getById(orderId);

  const [shipments, availabilities] = await Promise.all([
    shipping.getByOrder(orderId).catch((err: any) => {
      console.error("[bff] shipments upstream:", err.message);
      return [];
    }),
    Promise.all(
      (ord.items || []).map((it: any) =>
        inventory.totalAvailable(it.productId)
          .then((r: any) => ({ productId: it.productId, available: r.available }))
          .catch(() => ({ productId: it.productId, available: null }))
      )
    ),
  ]);

  const availMap = new Map(availabilities.map((d: any) => [d.productId, d.available]));
  const items = (ord.items || []).map((it: any) => ({
    ...it,
    globalAvailable: availMap.get(it.productId) ?? null,
  }));

  return { ...ord, items, shipments };
}
