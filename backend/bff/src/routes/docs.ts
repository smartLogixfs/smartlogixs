import { Router, Request, Response } from "express";
import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, resolve } from "node:path";
import swaggerUi from "swagger-ui-express";
import YAML from "yaml";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const openapiPath = resolve(__dirname, "../openapi.yaml");
const openapiDoc = YAML.parse(readFileSync(openapiPath, "utf8"));

const router = Router();

router.get("/openapi.json", (_req: Request, res: Response) => {
  res.json(openapiDoc);
});

router.use(
  "/docs",
  swaggerUi.serve,
  swaggerUi.setup(openapiDoc, {
    customSiteTitle: "SmartLogix BFF — API Docs",
    swaggerOptions: { persistAuthorization: true },
  })
);

export default router;
