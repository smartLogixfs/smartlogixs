import { msPedido } from "../clients/msPedido.js";
import { msInventario } from "../clients/msInventario.js";
import { msEnvio } from "../clients/msEnvio.js";

// GET /pedidos/:id/full → pedido + envíos asociados + disponibilidad agregada por producto.
export async function pedidoFull(idPedido) {
  const pedido = await msPedido.getById(idPedido);

  const [envios, disponibilidades] = await Promise.all([
    msEnvio.getByPedido(idPedido).catch((err) => {
      console.error("[bff] envios upstream:", err.message);
      return [];
    }),
    Promise.all(
      pedido.items.map((it) =>
        msInventario.disponibleTotal(it.idProducto)
          .then((r) => ({ idProducto: it.idProducto, disponible: r.disponible }))
          .catch(() => ({ idProducto: it.idProducto, disponible: null }))
      )
    ),
  ]);

  const dispMap = new Map(disponibilidades.map((d) => [d.idProducto, d.disponible]));
  const items = pedido.items.map((it) => ({
    ...it,
    disponibleGlobal: dispMap.get(it.idProducto) ?? null,
  }));

  return { ...pedido, items, envios };
}
