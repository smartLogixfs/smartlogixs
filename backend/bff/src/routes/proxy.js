import { Router } from "express";
import { createProxyMiddleware } from "http-proxy-middleware";
import { env } from "../config/env.js";

const router = Router();

// express.json() ya parseó req.body — el stream HTTP quedó vacío y el proxy haría timeout.
// Re-serializamos el body parseado para que el upstream lo reciba.
const restreamBody = (proxyReq, req) => {
  if (!req.body || !Object.keys(req.body).length) return;
  const ct = (proxyReq.getHeader("content-type") || "").toString();
  if (!ct.includes("application/json")) return;
  const data = JSON.stringify(req.body);
  proxyReq.setHeader("content-length", Buffer.byteLength(data));
  proxyReq.write(data);
};

router.use(
  "/inventario",
  createProxyMiddleware({
    target: env.MS_INVENTORY_URL,
    changeOrigin: true,
    pathRewrite: { "^/inventario": "" },
    on: { proxyReq: restreamBody },
  })
);

// Express strippea el prefix antes de pasar al middleware: /pedidos → /, /pedidos/123 → /123.
// Re-agregamos el prefix usando req.originalUrl para evitar trailing slashes (Spring Boot 4
// no hace match de trailing slash por defecto: /pedidos OK, /pedidos/ → 404).
// /inventario pasa tal cual porque el MS expone /productos, /bodegas, /stock (sin prefix común).
router.use(
  "/pedidos",
  createProxyMiddleware({
    target: env.MS_ORDER_URL,
    changeOrigin: true,
    pathRewrite: (_path, req) => req.originalUrl,
    on: { proxyReq: restreamBody },
  })
);

router.use(
  "/envios",
  createProxyMiddleware({
    target: env.MS_SHIPPING_URL,
    changeOrigin: true,
    pathRewrite: (_path, req) => req.originalUrl,
    on: { proxyReq: restreamBody },
  })
);

router.use(
  "/usuarios",
  createProxyMiddleware({
    target: env.MS_USER_URL,
    changeOrigin: true,
    pathRewrite: (_path, req) => req.originalUrl,
    on: { proxyReq: restreamBody },
  })
);

router.use(
  "/auth",
  createProxyMiddleware({
    target: env.MS_AUTH_URL,
    changeOrigin: true,
    pathRewrite: (_path, req) => req.originalUrl,
    on: { proxyReq: restreamBody },
  })
);

export default router;
