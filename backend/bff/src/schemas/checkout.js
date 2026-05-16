import { z } from "zod";

export const checkoutSchema = z.object({
  idCliente: z.string().min(1).max(64),
  idMarketplace: z.string().max(64).optional(),
  tipo: z.enum(["ESTANDAR", "EXPRESS", "MARKETPLACE"]).optional(),
  idBodega: z.number().int().positive(),
  envio: z.object({
    direccionDestino: z.string().min(1).max(255),
    comuna: z.string().max(120).optional(),
    region: z.string().max(120).optional(),
    fechaEstimada: z.string().date().optional(),
  }),
  items: z.array(z.object({
    idProducto: z.number().int().positive(),
    sku: z.string().min(1).max(64),
    cantidad: z.number().int().min(1),
    precioUnitario: z.number().nonnegative(),
  })).min(1),
});
