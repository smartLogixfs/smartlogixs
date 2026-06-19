import { Router, Request, Response, NextFunction } from "express";
import { inventory } from "../clients/inventory.js";
import { env } from "../config/env.js";
import { UpstreamError } from "../clients/httpClient.js";

const router = Router();

// Helper to map category from SKU prefix
function getCategoryFromSku(sku: string): string {
  const prefix = (sku || "").split("-")[0]?.toUpperCase();
  if (prefix === "ELE") return "Electrónica";
  if (prefix === "FAR") return "Farmacéutico";
  if (prefix === "AUT") return "Automotriz";
  if (prefix === "PER") return "Perecederos";
  return "General";
}

// Map warehouse name/location to ID
function getWarehouseId(location: string): number {
  const loc = (location || "").toLowerCase();
  if (loc.includes("fría") || loc.includes("fria") || loc.includes("b3")) return 2;
  if (loc.includes("carga") || loc.includes("general") || loc.includes("o1")) return 3;
  if (loc.includes("expresa") || loc.includes("sur")) return 4;
  return 1; // Default to Muelle Central A
}

// Direct HTTP request helper to communicate within BFF services
async function requestRaw(method: string, path: string, body: any): Promise<any> {
  const url = `${env.MS_INVENTORY_URL}${path}`;
  const res = await fetch(url, {
    method,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new UpstreamError(`ms-inventory respondió ${res.status}: ${text}`, { status: res.status, service: "inventory" });
  }
  return res.json();
}

router.get("/inventory/products-with-stock", async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const products = await inventory.productos();
    const result = await Promise.all(
      products.map(async (p: any) => {
        const stocks = await inventory.stockByProducto(p.idProducto).catch(() => []);
        const totalQty = stocks.reduce((sum: number, s: any) => sum + (s.cantidad || 0), 0);
        const minStock = stocks.length > 0 ? Math.min(...stocks.map((s: any) => s.stockMinimo || 0)) : 15;
        const locations = stocks.map((s: any) => `${s.bodega} (Cant: ${s.cantidad})`).join(", ") || "Sin Ubicación";
        
        let status = "Disponible";
        if (totalQty <= 0) status = "Agotado";
        else if (totalQty <= minStock) status = "Bajo Stock";

        return {
          id: String(p.idProducto),
          name: p.nombre,
          sku: p.sku,
          category: getCategoryFromSku(p.sku),
          quantity: totalQty,
          minStock: minStock,
          location: locations,
          status: status,
          lastUpdated: p.updatedAt ? p.updatedAt.substring(0, 10) : new Date().toISOString().substring(0, 10)
        };
      })
    );
    res.json(result);
  } catch (err) {
    next(err);
  }
});

// Intercept product creation to automatically set up initial stock
router.post("/inventory/products", async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { name, sku, category, quantity, minStock, location } = req.body;
    
    // Create product in ms-inventory
    const productPayload = {
      sku: sku || `GEN-${Math.floor(1000 + Math.random()*9000)}-SL`,
      nombre: name,
      descripcion: `Producto de categoría ${category || "General"} ubicado en ${location || "Muelle Central A"}.`,
      precio: 100.0
    };

    const createdProduct = await requestRaw("POST", "/products", productPayload);
    const idProducto = createdProduct.idProducto;

    // Set up initial stock if quantity is provided
    if (idProducto && quantity !== undefined) {
      const warehouseId = getWarehouseId(location || "");
      await requestRaw("POST", "/stock/in", {
        idProducto,
        idBodega: warehouseId,
        cantidad: Number(quantity) || 0,
        referenciaPedido: "Ingreso Manual"
      }).catch(err => {
        console.error("Error setting initial stock for product:", err);
      });
    }

    res.status(201).json({
      id: String(idProducto),
      name: createdProduct.nombre,
      sku: createdProduct.sku,
      category: category || "General",
      quantity: Number(quantity) || 0,
      minStock: Number(minStock) || 15,
      location: location || "Muelle Central A",
      status: Number(quantity) <= 0 ? "Agotado" : Number(quantity) <= (Number(minStock) || 15) ? "Bajo Stock" : "Disponible",
      lastUpdated: new Date().toISOString().substring(0, 10)
    });
  } catch (err) {
    next(err);
  }
});

export default router;
