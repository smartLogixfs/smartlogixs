import { msPedido } from "../clients/msPedido.js";
import { msInventario } from "../clients/msInventario.js";
import { msEnvio } from "../clients/msEnvio.js";

const ESTADOS_PEDIDO = ["PENDIENTE", "APROBADO", "EN_PREPARACION", "ENVIADO", "ENTREGADO"];

export async function dashboard() {
  const [pedidosPorEstado, stockBajo, enviosEnRuta] = await Promise.all([
    pedidosPorEstadoMap(),
    msInventario.stockBajo().catch(() => []),
    msEnvio.listar("EN_RUTA").catch(() => []),
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
      msPedido.listar(estado)
        .then((arr) => [estado, arr.length])
        .catch(() => [estado, null])
    )
  );
  return Object.fromEntries(entries);
}
