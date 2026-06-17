import { order } from "../clients/order.js";
import { inventory } from "../clients/inventory.js";
import { shipping } from "../clients/shipping.js";

// GET /pedidos/:id/full → pedido + envíos asociados + disponibilidad agregada por producto.
export async function pedidoFull(idPedido: number | string) {
  const pedido = await order.getById(idPedido);

  const [envios, disponibilidades] = await Promise.all([
    shipping.getByPedido(idPedido).catch((err: any) => {
      console.error("[bff] envios upstream:", err.message);
      return [];
    }),
    Promise.all(
      (pedido.items || []).map((it: any) =>
        inventory.disponibleTotal(it.idProducto)
          .then((r: any) => ({ idProducto: it.idProducto, disponible: r.disponible }))
          .catch(() => ({ idProducto: it.idProducto, disponible: null }))
      )
    ),
  ]);

  const dispMap = new Map(disponibilidades.map((d: any) => [d.idProducto, d.disponible]));
  const items = (pedido.items || []).map((it: any) => ({
    ...it,
    disponibleGlobal: dispMap.get(it.idProducto) ?? null,
  }));

  return { ...pedido, items, envios };
}
