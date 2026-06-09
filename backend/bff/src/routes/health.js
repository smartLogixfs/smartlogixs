import { Router } from "express";

const router = Router();

router.get("/health", (_req, res) => {
  res.json({ status: "ok", service: "bff" });
});

router.get("/", (_req, res) => {
  res.json({
    service: "smartlogix-bff",
    version: "0.1.0",
    endpoints: {
      compuestos: ["GET /pedidos/:id/full", "POST /checkout", "GET /dashboard"],
      proxy: ["/inventario/*", "/pedidos/*", "/envios/*", "/usuarios/*", "/auth/*"],
    },
  });
});

export default router;
