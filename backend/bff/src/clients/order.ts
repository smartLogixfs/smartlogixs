import { env } from "../config/env.js";
import { request } from "./httpClient.js";

const SERVICE = "order";
const base = () => env.MS_ORDER_URL;

export const order = {
  crear: (payload: any) => request(SERVICE, base(), "/orders", { method: "POST", body: payload }),
  getById: (id: number | string) => request(SERVICE, base(), `/orders/${id}`),
  getByCodigo: (codigo: string) => request(SERVICE, base(), `/orders/code/${codigo}`),
  listar: (estado?: string) => request(SERVICE, base(), `/orders${estado ? `?status=${estado}` : ""}`),
  cambiarEstado: (id: number | string, payload: any) => request(SERVICE, base(), `/orders/${id}/status`, { method: "PATCH", body: payload }),
};
