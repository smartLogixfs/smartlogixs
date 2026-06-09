import { env } from "../config/env.js";
import { request } from "./httpClient.js";

const SERVICE = "shipping";
const base = () => env.MS_SHIPPING_URL;

export const shipping = {
  crear: (payload) => request(SERVICE, base(), "/envios", { method: "POST", body: payload }),
  getById: (id) => request(SERVICE, base(), `/envios/${id}`),
  getByPedido: (idPedido) => request(SERVICE, base(), `/envios/pedido/${idPedido}`),
  listar: (estado) => request(SERVICE, base(), `/envios${estado ? `?estado=${estado}` : ""}`),
};
