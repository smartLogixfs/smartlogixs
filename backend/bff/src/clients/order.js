import { env } from "../config/env.js";
import { request } from "./httpClient.js";

const SERVICE = "order";
const base = () => env.MS_ORDER_URL;

export const order = {
  crear: (payload) => request(SERVICE, base(), "/pedidos", { method: "POST", body: payload }),
  getById: (id) => request(SERVICE, base(), `/pedidos/${id}`),
  getByCodigo: (codigo) => request(SERVICE, base(), `/pedidos/codigo/${codigo}`),
  listar: (estado) => request(SERVICE, base(), `/pedidos${estado ? `?estado=${estado}` : ""}`),
  cambiarEstado: (id, payload) => request(SERVICE, base(), `/pedidos/${id}/estado`, { method: "PATCH", body: payload }),
};
