import { env } from "../config/env.js";
import { request } from "./httpClient.js";

const SERVICE = "ms-envio";
const base = () => env.MS_ENVIO_URL;

export const msEnvio = {
  crear: (payload) => request(SERVICE, base(), "/envios", { method: "POST", body: payload }),
  getById: (id) => request(SERVICE, base(), `/envios/${id}`),
  getByPedido: (idPedido) => request(SERVICE, base(), `/envios/pedido/${idPedido}`),
  listar: (estado) => request(SERVICE, base(), `/envios${estado ? `?estado=${estado}` : ""}`),
};
