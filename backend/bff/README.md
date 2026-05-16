# SmartLogix BFF

Backend For Frontend del sistema SmartLogix. Orquesta llamadas a los microservicios y expone una API optimizada para el cliente frontend.

**Stack**: Node.js 20 · Express 4 · http-proxy-middleware 3 · zod 3 · morgan.

## Responsabilidad

Capa de **adaptación** entre el frontend y la malla de microservicios.
Dos tipos de endpoints:

1. **Proxy passthrough** — CRUD simple sobre cada MS (`/inventario/*`, `/pedidos/*`, `/envios/*`)
2. **Compuestos / orquestados** — agregan datos de varios MS o coordinan un flujo:
   - `GET /pedidos/:id/full` — pedido + envíos asociados + disponibilidad de stock por producto
   - `POST /checkout` — orquesta: crear pedido → reservar stock por ítem → crear envío (con rollback best-effort)
   - `GET /dashboard` — cuentas de pedidos por estado, top stock bajo, envíos en ruta

## API

| Método | Path | Descripción |
|---|---|---|
| GET | `/health` | Health check (`{status: ok, service: bff}`) |
| GET | `/` | Manifiesto: lista de endpoints |
| GET | `/pedidos/:id/full` | Pedido completo + envíos + disponibilidad agregada por producto |
| POST | `/checkout` | Orquestación pedido → reserva stock → envío |
| GET | `/dashboard` | Vista agregada para el panel |
| ANY | `/inventario/*` | Proxy a ms-inventario |
| ANY | `/pedidos/*` | Proxy a ms-pedido |
| ANY | `/envios/*` | Proxy a ms-envio |

## Cómo ejecutar

### Vía Docker (recomendado)

Desde la raíz del monorepo:

```bash
docker compose up -d bff
```

Levanta el BFF + sus dependencias (los 3 MS).

### Local (sin Docker)

```bash
cd backend/bff
npm install
npm run dev          # con --watch
# o
npm start
```

Variables de entorno (todas con defaults para correr sin Docker apuntando a localhost si tienes los MS arriba):

| Variable | Default | Función |
|---|---|---|
| `PORT` | `3000` | Puerto HTTP del BFF |
| `MS_INVENTARIO_URL` | `http://ms-inventario:8080` | URL base del MS de inventario |
| `MS_PEDIDO_URL` | `http://ms-pedido:8080` | URL base del MS de pedido |
| `MS_ENVIO_URL` | `http://ms-envio:8080` | URL base del MS de envío |
| `HTTP_TIMEOUT_MS` | `5000` | Timeout para llamadas a MS |

## Probar la API

```bash
# Health
curl http://bff.smartlogix.localhost/health

# Checkout end-to-end (asume que tienes productos y bodega creados)
curl -X POST http://bff.smartlogix.localhost/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "idCliente": "CL-001",
    "idBodega": 1,
    "envio": {
      "direccionDestino": "Av. Providencia 1234",
      "comuna": "Providencia",
      "region": "RM"
    },
    "items": [
      {"idProducto": 1, "sku": "SKU-001", "cantidad": 2, "precioUnitario": 5000}
    ]
  }'

# Dashboard
curl http://bff.smartlogix.localhost/dashboard

# Pedido completo
curl http://bff.smartlogix.localhost/pedidos/1/full
```

## Estructura del proyecto

```
src/
├── server.js                   # entry point: registra middleware y rutas
├── config/
│   └── env.js                  # vars de entorno con defaults
├── clients/                    # wrappers fetch con timeout y errores tipados
│   ├── httpClient.js           # UpstreamError + AbortController
│   ├── msPedido.js
│   ├── msInventario.js
│   └── msEnvio.js
├── schemas/                    # validación zod
│   └── checkout.js
├── middleware/
│   ├── errorHandler.js         # ProblemDetail-style errors
│   └── validate.js
├── services/                   # lógica de orquestación
│   ├── checkoutService.js      # con rollback best-effort
│   ├── pedidoComposerService.js
│   └── dashboardService.js
└── routes/
    ├── health.js
    ├── checkout.js
    ├── pedidos.js
    ├── dashboard.js
    └── proxy.js
```

## Manejo de errores

Centralizado en `middleware/errorHandler.js`. Convierte excepciones en RFC 7807-style JSON:

- `ZodError` → 400 con `errors: { campo: mensaje }`
- `UpstreamError` (HTTP fallido a un MS / timeout / inalcanzable) → propaga el status del upstream (504 si timeout, 502 si inalcanzable)
- Cualquier otro error → 500 con log

## Patrones aplicados

- **BFF** (Backend For Frontend)
- **Composite Service** (agregación de respuestas de varios MS en paralelo con `Promise.all`)
- **Saga simplificada** (orquestación de pasos con rollback best-effort en `checkoutService`)
- **Circuit-Breaker-lite**: cada `client/` envuelve `fetch` con timeout vía `AbortController`; `dashboardService` y `pedidoComposerService` toleran fallos parciales con `.catch()`
- **Schema Validation** (zod) antes de procesar la request
