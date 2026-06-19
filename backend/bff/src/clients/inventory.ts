import { env } from "../config/env.js";
import { request } from "./httpClient.js";

const SERVICE = "inventory";
const base = () => env.MS_INVENTORY_URL;

export const inventory = {
  productoById: (id: number | string) => request(SERVICE, base(), `/products/${id}`),
  productos: () => request(SERVICE, base(), "/products"),
  stockGet: (idProducto: number | string, idBodega: number | string) => request(SERVICE, base(), `/stock/${idProducto}/${idBodega}`),
  stockByProducto: (id: number | string) => request(SERVICE, base(), `/stock/product/${id}`),
  disponibleTotal: (idProducto: number | string) => request(SERVICE, base(), `/stock/product/${idProducto}/available`),
  stockBajo: () => request(SERVICE, base(), "/stock/low"),
  reservar: (payload: any) => request(SERVICE, base(), "/stock/reserve", { method: "POST", body: payload }),
  liberar: (payload: any) => request(SERVICE, base(), "/stock/release", { method: "POST", body: payload }),
};
