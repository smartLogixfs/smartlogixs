import { env } from "../config/env.js";
import { request } from "./httpClient.js";

const SERVICE = "shipping";
const base = () => env.MS_SHIPPING_URL;

export const shipping = {
  crear: (payload: any) => request(SERVICE, base(), "/envios", { method: "POST", body: payload }),
  getById: (id: number | string) => request(SERVICE, base(), `/envios/${id}`),
  getByPedido: (idPedido: number | string) => request(SERVICE, base(), `/envios/pedido/${idPedido}`),
  listar: (estado?: string) => request(SERVICE, base(), `/envios${estado ? `?estado=${estado}` : ""}`),
};
