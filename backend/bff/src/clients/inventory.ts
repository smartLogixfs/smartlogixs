import { env } from "../config/env.js";
import { request } from "./httpClient.js";

const SERVICE = "inventory";
const base = () => env.MS_INVENTORY_URL;

export const inventory = {
  productoById: (id: number | string) => request(SERVICE, base(), `/productos/${id}`),
  productos: () => request(SERVICE, base(), "/productos"),
  stockGet: (idProducto: number | string, idBodega: number | string) => request(SERVICE, base(), `/stock/${idProducto}/${idBodega}`),
  stockByProducto: (id: number | string) => request(SERVICE, base(), `/stock/producto/${id}`),
  disponibleTotal: (idProducto: number | string) => request(SERVICE, base(), `/stock/producto/${idProducto}/disponible`),
  stockBajo: () => request(SERVICE, base(), "/stock/bajo"),
  reservar: (payload: any) => request(SERVICE, base(), "/stock/reservar", { method: "POST", body: payload }),
  liberar: (payload: any) => request(SERVICE, base(), "/stock/liberar", { method: "POST", body: payload }),
};
