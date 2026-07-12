import * as Sentry from "@sentry/node";

// Monitoreo de errores con GlitchTip (compatible con el SDK de Sentry).
// Debe importarse ANTES que cualquier otro modulo en server.ts para que el SDK
// pueda instrumentar Express. Si el DSN esta vacio, init() queda en no-op.
const dsn = process.env.BFF_GLITCHTIP_DSN;
if (dsn) {
  Sentry.init({
    dsn,
    environment: process.env.NODE_ENV || "development",
    tracesSampleRate: 0.1,
  });
}
