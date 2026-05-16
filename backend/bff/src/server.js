import express from "express";
import morgan from "morgan";

import { env } from "./config/env.js";
import healthRouter from "./routes/health.js";
import checkoutRouter from "./routes/checkout.js";
import pedidosRouter from "./routes/pedidos.js";
import dashboardRouter from "./routes/dashboard.js";
import proxyRouter from "./routes/proxy.js";
import { errorHandler, notFound } from "./middleware/errorHandler.js";

const app = express();

app.use(morgan("combined"));
app.use(express.json());

// Endpoints propios del BFF (compuestos + health) van antes del proxy.
app.use(healthRouter);
app.use(checkoutRouter);
app.use(pedidosRouter);
app.use(dashboardRouter);

// Passthrough genérico para CRUD simple de los MS.
app.use(proxyRouter);

app.use(notFound);
app.use(errorHandler);

app.listen(env.PORT, "0.0.0.0", () => {
  console.log(`BFF listening on :${env.PORT}`);
  console.log(`  -> inventario: ${env.MS_INVENTARIO_URL}`);
  console.log(`  -> pedido:     ${env.MS_PEDIDO_URL}`);
  console.log(`  -> envio:      ${env.MS_ENVIO_URL}`);
});
