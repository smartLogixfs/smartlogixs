import { order } from "../clients/order.js";
import { inventory } from "../clients/inventory.js";
import { shipping } from "../clients/shipping.js";

const ESTADOS_PEDIDO = ["PENDIENTE", "APROBADO", "EN_PREPARACION", "ENVIADO", "ENTREGADO"];

export async function dashboard() {
  const [pedidosPorEstado, stockBajo, enviosEnRuta] = await Promise.all([
    pedidosPorEstadoMap(),
    inventory.stockBajo().catch(() => []),
    shipping.listar("EN_RUTA").catch(() => []),
  ]);

  return {
    pedidos: pedidosPorEstado,
    stockBajo: {
      total: stockBajo.length,
      items: stockBajo.slice(0, 10),
    },
    enviosEnRuta: {
      total: enviosEnRuta.length,
      items: enviosEnRuta.slice(0, 10),
    },
    generatedAt: new Date().toISOString(),
  };
}

async function pedidosPorEstadoMap() {
  const entries = await Promise.all(
    ESTADOS_PEDIDO.map((estado) =>
      order.listar(estado)
        .then((arr) => [estado, arr.length])
        .catch(() => [estado, null])
    )
  );
  return Object.fromEntries(entries as any);
}
