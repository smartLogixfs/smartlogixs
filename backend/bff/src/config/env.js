export const env = {
  PORT: Number(process.env.PORT) || 3000,
  MS_INVENTORY_URL: process.env.MS_INVENTORY_URL || "http://inventory:8080",
  MS_ORDER_URL: process.env.MS_ORDER_URL || "http://order:8080",
  MS_SHIPPING_URL: process.env.MS_SHIPPING_URL || "http://shipping:8080",
  MS_USER_URL: process.env.MS_USER_URL || "http://user:8080",
  MS_AUTH_URL: process.env.MS_AUTH_URL || "http://auth:8081",
  HTTP_TIMEOUT_MS: Number(process.env.HTTP_TIMEOUT_MS) || 5000,
};
