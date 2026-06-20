import { env } from "../config/env.js";
import { request } from "./httpClient.js";

const SERVICE = "shipping";
const base = () => env.MS_SHIPPING_URL;

export const shipping = {
  create: (payload: any) => request(SERVICE, base(), "/shipments", { method: "POST", body: payload }),
  getById: (id: number | string) => request(SERVICE, base(), `/shipments/${id}`),
  getByOrder: (orderId: number | string) => request(SERVICE, base(), `/shipments/order/${orderId}`),
  list: (status?: string) => request(SERVICE, base(), `/shipments${status ? `?status=${status}` : ""}`),
};
