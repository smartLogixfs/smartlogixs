import { order } from "../clients/order.js";
import { inventory } from "../clients/inventory.js";
import { shipping } from "../clients/shipping.js";

// GET /orders/:id/full → order + associated shipments + aggregated availability per product.
export async function pedidoFull(idPedido: number | string) {
  const pedido = await order.getById(idPedido);

  const [envios, disponibilidades] = await Promise.all([
    shipping.getByPedido(idPedido).catch((err: any) => {
      console.error("[bff] shipments upstream:", err.message);
      return [];
    }),
    Promise.all(
      (pedido.items || []).map((it: any) =>
        inventory.disponibleTotal(it.productId)
          .then((r: any) => ({ productId: it.productId, available: r.available }))
          .catch(() => ({ productId: it.productId, available: null }))
      )
    ),
  ]);

  const dispMap = new Map(disponibilidades.map((d: any) => [d.productId, d.available]));
  const items = (pedido.items || []).map((it: any) => ({
    ...it,
    globalAvailable: dispMap.get(it.productId) ?? null,
  }));

  return { ...pedido, items, shipments: envios };
}
