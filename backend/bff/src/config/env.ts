export const env = {
  PORT: Number(process.env.PORT) || 3000,
  MS_INVENTORY_URL: process.env.MS_INVENTORY_URL || "http://ms-inventory:8080",
  MS_ORDER_URL: process.env.MS_ORDER_URL || "http://ms-order:8080",
  MS_SHIPPING_URL: process.env.MS_SHIPPING_URL || "http://ms-shipping:8080",
  MS_USER_URL: process.env.MS_USER_URL || "http://ms-user:8080",
  MS_AUTH_URL: process.env.MS_AUTH_URL || "http://ms-auth:8081",
  HTTP_TIMEOUT_MS: Number(process.env.HTTP_TIMEOUT_MS) || 5000,
  // Circuit Breaker: fallos consecutivos para abrir y ventana antes de reintentar.
  CB_FAILURE_THRESHOLD: Number(process.env.CB_FAILURE_THRESHOLD) || 5,
  CB_RESET_TIMEOUT_MS: Number(process.env.CB_RESET_TIMEOUT_MS) || 15000,
  GLITCHTIP_DSN: process.env.BFF_GLITCHTIP_DSN || "",
  NODE_ENV: process.env.NODE_ENV || "development",
};
