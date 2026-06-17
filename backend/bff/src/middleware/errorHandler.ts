import { Request, Response, NextFunction } from "express";
import { ZodError } from "zod";
import { UpstreamError } from "../clients/httpClient.js";

export function notFound(_req: Request, res: Response) {
  res.status(404).json({
    status: 404,
    title: "Not Found",
    timestamp: new Date().toISOString(),
  });
}

// eslint-disable-next-line no-unused-vars
export function errorHandler(err: any, req: Request, res: Response, _next: NextFunction) {
  const ts = new Date().toISOString();

  if (err instanceof ZodError) {
    const fields = Object.fromEntries(err.issues.map((i) => [i.path.join("."), i.message]));
    return res.status(400).json({
      status: 400,
      title: "Validación fallida",
      detail: "Uno o más campos no cumplen las restricciones",
      errors: fields,
      timestamp: ts,
    });
  }

  if (err instanceof UpstreamError) {
    const status = err.status && err.status >= 400 && err.status < 600 ? err.status : 502;
    return res.status(status).json({
      status,
      title: `Error upstream (${err.service})`,
      detail: err.message,
      upstream: err.body ?? null,
      timestamp: ts,
    });
  }

  console.error("[bff] unhandled error:", err);
  return res.status(500).json({
    status: 500,
    title: "Internal Server Error",
    detail: err.message,
    timestamp: ts,
  });
}
