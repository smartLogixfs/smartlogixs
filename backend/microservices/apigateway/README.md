# API Gateway (KrakenD)

API Gateway del ecosistema SmartLogix.

**Stack**: KrakenD v2.10.2.

## Posición en la arquitectura

El **punto único de entrada** al sistema es **Traefik** (Ingress Controller), no este componente.
KrakenD vive **detrás** de Traefik y atiende exclusivamente las rutas del host `api.smartlogix.localhost`:

```
Internet → Traefik (Ingress) ──┬── app.smartlogix.localhost    → Frontend
                                ├── api.smartlogix.localhost    → KrakenD (este servicio)
                                ├── bff.smartlogix.localhost    → BFF (debug directo)
                                └── traefik.smartlogix.localhost → Dashboard

                                                                  KrakenD
                                                                    │
                                                                    ▼
                                                                  BFF → ms-{...}
```

## Responsabilidad

Sobre el tráfico que Traefik le entrega (`api.smartlogix.localhost/*`), KrakenD aplica:

- **Routing**: mapea `/api/*` hacia los endpoints internos del **BFF** (no llama a los MS directamente)
- **Rate limiting** y throttling configurables por endpoint
- **JWT validation** (preparado, se activa cuando exista emisor)
- **CORS**, headers, compresión

KrakenD es declarativo: toda la lógica vive en `krakend.json`.

## Cómo ejecutar

### Vía Docker (recomendado)

Desde la raíz del monorepo:

```bash
docker compose up -d apigateway
```

### Local (sin Docker)

```bash
docker run -p 8080:8080 -v $(pwd)/krakend.json:/etc/krakend/krakend.json \
  devopsfaith/krakend:2.10.2 run -c /etc/krakend/krakend.json
```

### Validar la config sin levantar el servicio

```bash
docker run --rm -v $(pwd)/krakend.json:/etc/krakend/krakend.json \
  devopsfaith/krakend:2.10.2 check -c /etc/krakend/krakend.json
```

## Probar

A través de Traefik (con `hosts` configurado):

```bash
curl http://api.smartlogix.localhost/api/inventario/productos
curl http://api.smartlogix.localhost/api/pedidos
curl http://api.smartlogix.localhost/api/envios
```

## Estructura

```
apigateway/
├── Dockerfile          # base devopsfaith/krakend
├── krakend.json        # configuración declarativa (única fuente de verdad)
└── README.md
```

## Configuración relevante

El `krakend.json` define:

- `endpoints[]`: cada uno con `endpoint` (path expuesto) y `backend[]` (host destino dentro de la red `internal`)
- `extra_config`: middlewares globales (CORS, JWT, logging)

JWT validator preparado, activación cuando exista emisor real (Auth0, Keycloak, etc.). Hoy las rutas pasan sin autenticación para facilitar el desarrollo.

## Patrones aplicados

- **API Gateway** (Fowler) — agrega cross-cutting concerns (auth, rate limit, CORS) sobre la rama de tráfico que le delega el Ingress
- **Ingress + API Gateway separados** — Traefik es el punto único de entrada (TLS, routing por host); KrakenD especializa solo el subdominio `api.*`
- **Backend-for-frontend separation** — KrakenD enruta al BFF, no llama a los MS directo
- **Declarative configuration** — sin código compilado; toda la lógica vive en JSON
