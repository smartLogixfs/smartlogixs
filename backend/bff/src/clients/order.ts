import { env } from "../config/env.js";
import { request } from "./httpClient.js";

const SERVICE = "order";
const base = () => env.MS_ORDER_URL;

export const order = {
  create: (payload: any) => request(SERVICE, base(), "/orders", { method: "POST", body: payload }),
  getById: (id: number | string) => request(SERVICE, base(), `/orders/${id}`),
  getByCode: (code: string) => request(SERVICE, base(), `/orders/code/${code}`),
  list: (status?: string) => request(SERVICE, base(), `/orders${status ? `?status=${status}` : ""}`),
  changeStatus: (id: number | string, payload: any) => request(SERVICE, base(), `/orders/${id}/status`, { method: "PATCH", body: payload }),
};
