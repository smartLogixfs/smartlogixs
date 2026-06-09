import { env } from "../config/env.js";
import { request } from "./httpClient.js";

const SERVICE = "ms-inventario";
const base = () => env.MS_INVENTARIO_URL;

export const msInventario = {
  productoById: (id) => request(SERVICE, base(), `/productos/${id}`),
  stockGet: (idProducto, idBodega) => request(SERVICE, base(), `/stock/${idProducto}/${idBodega}`),
  disponibleTotal: (idProducto) => request(SERVICE, base(), `/stock/producto/${idProducto}/disponible`),
  stockBajo: () => request(SERVICE, base(), "/stock/bajo"),
  reservar: (payload) => request(SERVICE, base(), "/stock/reservar", { method: "POST", body: payload }),
  liberar: (payload) => request(SERVICE, base(), "/stock/liberar", { method: "POST", body: payload }),
};
