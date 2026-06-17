import { env } from "../config/env.js";
import { request } from "./httpClient.js";

const SERVICE = "order";
const base = () => env.MS_ORDER_URL;

export const order = {
  crear: (payload: any) => request(SERVICE, base(), "/pedidos", { method: "POST", body: payload }),
  getById: (id: number | string) => request(SERVICE, base(), `/pedidos/${id}`),
  getByCodigo: (codigo: string) => request(SERVICE, base(), `/pedidos/codigo/${codigo}`),
  listar: (estado?: string) => request(SERVICE, base(), `/pedidos${estado ? `?estado=${estado}` : ""}`),
  cambiarEstado: (id: number | string, payload: any) => request(SERVICE, base(), `/pedidos/${id}/estado`, { method: "PATCH", body: payload }),
};
