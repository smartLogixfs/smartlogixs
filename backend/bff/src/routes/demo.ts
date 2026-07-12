import { Router, Request, Response } from "express";
import * as Sentry from "@sentry/node";

// Endpoints de demostracion para la Letra G (registro de errores y logs con GlitchTip).
// Permiten provocar eventos en la capa BFF y verlos en el dashboard. Solo para demo.
const router = Router();

router.get("/demo/health", (_req: Request, res: Response) => {
  res.json({ service: "bff", status: "ok", timestamp: new Date().toISOString() });
});

router.get("/demo/log", (_req: Request, res: Response) => {
  // Mensaje de nivel error capturado directamente por el SDK (no-op si no hay DSN).
  Sentry.captureMessage("[DEMO] Evento de log de prueba enviado a GlitchTip desde el BFF", "error");
  res.json({ sent: "mensaje 'error' enviado a GlitchTip" });
});

router.get("/demo/error", (_req: Request, _res: Response) => {
  // Excepcion no controlada: Express la reenvia al error handler y la captura
  // Sentry.setupExpressErrorHandler (registrado en server.ts) -> GlitchTip.
  throw new Error("[DEMO] Excepcion de prueba para GlitchTip desde el BFF");
});

export default router;
