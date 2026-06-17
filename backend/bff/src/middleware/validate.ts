import { Request, Response, NextFunction } from "express";
import { ZodSchema } from "zod";

export const validate = (schema: ZodSchema, source: "body" | "query" | "params" = "body") => 
  (req: Request, _res: Response, next: NextFunction) => {
    const parsed = schema.parse((req as any)[source]);
    (req as any)[source] = parsed;
    next();
  };
