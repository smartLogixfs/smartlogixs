import "./instrument.js"; // Sentry/GlitchTip: debe ir primero para instrumentar Express.

import express from "express";
import morgan from "morgan";
import * as Sentry from "@sentry/node";

import { env } from "./config/env.js";
import healthRouter from "./routes/health.js";
import docsRouter from "./routes/docs.js";
import checkoutRouter from "./routes/checkout.js";
import ordersRouter from "./routes/orders.js";
import dashboardRouter from "./routes/dashboard.js";
import inventoryRouter from "./routes/inventory.js";
import proxyRouter from "./routes/proxy.js";
import { errorHandler, notFound } from "./middleware/errorHandler.js";

const app = express();

app.use(morgan("combined"));
app.use(express.json());

// Documentacion OpenAPI / Swagger UI (antes del proxy para que no se redirija).
app.use(docsRouter);

// Endpoints propios del BFF (compuestos + health) van antes del proxy.
app.use(healthRouter);
app.use(checkoutRouter);
app.use(ordersRouter);
app.use(dashboardRouter);
app.use(inventoryRouter);

// Passthrough genérico para CRUD simple de los MS.
app.use(proxyRouter);

app.use(notFound);

// Captura de errores en GlitchTip (antes del handler propio). No-op si no hay DSN.
Sentry.setupExpressErrorHandler(app);

app.use(errorHandler);

app.listen(env.PORT, "0.0.0.0", () => {
  console.log(`BFF listening on :${env.PORT}`);
  console.log(`  -> inventory: ${env.MS_INVENTORY_URL}`);
  console.log(`  -> order:     ${env.MS_ORDER_URL}`);
  console.log(`  -> shipping:  ${env.MS_SHIPPING_URL}`);
  console.log(`  -> user:      ${env.MS_USER_URL}`);
  console.log(`  -> auth:      ${env.MS_AUTH_URL}`);
  console.log(`  -> docs:      http://localhost:${env.PORT}/docs`);
});
