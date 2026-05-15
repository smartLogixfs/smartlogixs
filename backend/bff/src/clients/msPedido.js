import { env } from "../config/env.js";
import { request } from "./httpClient.js";

const SERVICE = "ms-pedido";
const base = () => env.MS_PEDIDO_URL;

export const msPedido = {
  crear: (payload) => request(SERVICE, base(), "/pedidos", { method: "POST", body: payload }),
  getById: (id) => request(SERVICE, base(), `/pedidos/${id}`),
  getByCodigo: (codigo) => request(SERVICE, base(), `/pedidos/codigo/${codigo}`),
  listar: (estado) => request(SERVICE, base(), `/pedidos${estado ? `?estado=${estado}` : ""}`),
  cambiarEstado: (id, payload) => request(SERVICE, base(), `/pedidos/${id}/estado`, { method: "PATCH", body: payload }),
};
