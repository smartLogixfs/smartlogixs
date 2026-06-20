import { env } from "../config/env.js";
import { request } from "./httpClient.js";

const SERVICE = "inventory";
const base = () => env.MS_INVENTORY_URL;

export const inventory = {
  productById: (id: number | string) => request(SERVICE, base(), `/products/${id}`),
  products: () => request(SERVICE, base(), "/products"),
  stockGet: (productId: number | string, warehouseId: number | string) => request(SERVICE, base(), `/stock/${productId}/${warehouseId}`),
  stockByProduct: (id: number | string) => request(SERVICE, base(), `/stock/product/${id}`),
  totalAvailable: (productId: number | string) => request(SERVICE, base(), `/stock/product/${productId}/available`),
  lowStock: () => request(SERVICE, base(), "/stock/low"),
  reserve: (payload: any) => request(SERVICE, base(), "/stock/reserve", { method: "POST", body: payload }),
  release: (payload: any) => request(SERVICE, base(), "/stock/release", { method: "POST", body: payload }),
};
