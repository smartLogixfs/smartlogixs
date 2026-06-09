export const env = {
  PORT: Number(process.env.PORT) || 3000,
  MS_INVENTARIO_URL: process.env.MS_INVENTARIO_URL || "http://ms-inventario:8080",
  MS_PEDIDO_URL: process.env.MS_PEDIDO_URL || "http://ms-pedido:8080",
  MS_ENVIO_URL: process.env.MS_ENVIO_URL || "http://ms-envio:8080",
  MS_USUARIO_URL: process.env.MS_USUARIO_URL || "http://ms-usuario:8080",
  HTTP_TIMEOUT_MS: Number(process.env.HTTP_TIMEOUT_MS) || 5000,
};
