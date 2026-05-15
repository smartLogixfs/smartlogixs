# SmartLogix

Monorepo del sistema SmartLogix — plataforma logística para PYMEs eCommerce.

Arquitectura de microservicios con **Traefik** como Ingress, **KrakenD** como API Gateway, **BFF** en Node.js y **microservicios Spring Boot** con **DB-per-Service**.

## Estructura

```
smartlogixs/
├── docker-compose.yml             # Orquesta el stack completo
├── .env.example                   # Variables (copiar a .env)
├── infra/
│   └── traefik/                   # Config Ingress Controller
│       ├── traefik.yml
│       └── dynamic/
│           ├── routers.yml        # routers por File provider
│           └── middlewares.yml
├── docs/
│   └── modelo-datos.md            # ER y máquinas de estado por MS
├── frontend/                      # React 19 + Vite + Nginx
└── backend/
    ├── bff/                       # Node.js 20 (Express + zod)
    └── microservices/
        ├── apigateway/            # KrakenD v2.10.2
        ├── ms-pedido/             # Spring Boot 4 / Java 25
        ├── ms-inventario/         # Spring Boot 4 / Java 25
        └── ms-envio/              # Spring Boot 4 / Java 25
```

## Topología

```
Internet
   │
   ▼
Traefik v3.5 (Ingress)         red: web (pública)
   ├── app.smartlogix.localhost      → Frontend
   ├── api.smartlogix.localhost      → API Gateway (KrakenD)
   ├── bff.smartlogix.localhost      → BFF (debug directo)
   └── traefik.smartlogix.localhost  → Dashboard

API Gateway → BFF → ms-{inventario,pedido,envio}
                          │
                          ▼
                    db-{...}                red: internal (privada)
```

- **`web`**: red pública donde Traefik enruta tráfico externo
- **`internal`** (`internal: true`): red aislada; las DBs no son alcanzables desde Internet

## Levantar el stack

### 1. Pre-requisitos

- Docker Desktop (Windows/Mac) o Docker Engine + Compose v2 (Linux)
- Editar el `hosts` de tu sistema:

**Windows** (`C:\Windows\System32\drivers\etc\hosts`, como administrador):
```
127.0.0.1 app.smartlogix.localhost
127.0.0.1 api.smartlogix.localhost
127.0.0.1 bff.smartlogix.localhost
127.0.0.1 traefik.smartlogix.localhost
```

> En Linux/Mac, `*.localhost` resuelve automáticamente; los browsers modernos también lo hacen sin tocar hosts.

### 2. Variables

```bash
cp .env.example .env
# editar passwords si lo necesitas
```

### 3. Build + up

```bash
docker compose up --build -d
```

Verificar:
```bash
docker compose ps
```

Esperado: 10 contenedores arriba (traefik, frontend, apigateway, bff, 3× ms, 3× db).

### 4. Smoke tests

| URL | Qué deberías ver |
|---|---|
| http://app.smartlogix.localhost | Frontend React |
| http://api.smartlogix.localhost/api/pedidos | Listado vía KrakenD → BFF |
| http://bff.smartlogix.localhost/health | `{"status":"ok","service":"bff"}` |
| http://bff.smartlogix.localhost/dashboard | Agregado de estado del sistema |
| http://traefik.smartlogix.localhost | Dashboard Traefik |

## Componentes

### Traefik (Ingress Controller v3.5)

- Provider **File** (en `infra/traefik/dynamic/routers.yml`) — descubrimiento por archivo, **no** por labels Docker
  - Workaround para el bug del provider Docker del cliente Go en Docker Desktop Windows
- Middlewares reutilizables en `dynamic/middlewares.yml`: `secure-headers`, `rate-limit`, `cors-api`
- Entrypoints: `web` (80), `websecure` (443, preparado), `traefik` (8080, dashboard)

### KrakenD (API Gateway v2.10.2)

- Configuración declarativa en `backend/microservices/apigateway/krakend.json`
- Routing: `/api/inventario/*`, `/api/pedidos/*`, `/api/envios/*` → BFF
- JWT validator preparado para activarse cuando exista emisor

### BFF (Node.js 20 + Express + zod)

- Estructura modular: `clients/`, `services/`, `routes/`, `middleware/`, `schemas/`
- **Endpoints compuestos** (valor agregado del BFF):
  - `GET /pedidos/:id/full` — pedido + envíos + disponibilidad de stock por producto
  - `POST /checkout` — orquesta crear pedido → reservar stock → crear envío (con rollback best-effort)
  - `GET /dashboard` — agregados (pedidos por estado, stock bajo, envíos en ruta)
- **Endpoints proxy** para CRUD simple: `/inventario/*`, `/pedidos/*`, `/envios/*`
- Manejo centralizado de errores (RFC 7807 ProblemDetail), validación con zod, `UpstreamError` con timeout

### Microservicios (Spring Boot 4 + Java 25)

Cada MS:
- **JPA + Hibernate** en modo `ddl-auto=validate` (Flyway es la fuente de verdad del schema)
- **Flyway** con `V1__init_schema.sql` por servicio
- **Bean Validation** en DTOs, `@RestControllerAdvice` con `GlobalExceptionHandler` global
- **Spring Data JPA repositories**, **service layer** transaccional
- Endpoints REST documentados en el README de cada uno

Servicios:

| MS | Responsable de | Endpoints clave |
|---|---|---|
| [`ms-pedido`](backend/microservices/ms-pedido/) | Pedidos + máquina de estados | `POST /pedidos`, `PATCH /pedidos/{id}/estado` |
| [`ms-inventario`](backend/microservices/ms-inventario/) | Productos, Bodegas, Stock (con `@Version` optimistic lock) | `POST /stock/{entrada,salida,reservar,liberar}` |
| [`ms-envio`](backend/microservices/ms-envio/) | Envíos, Transportistas, Seguimiento | `POST /envios`, `PATCH /envios/{id}/{transportista,estado}` |

### PostgreSQL 16 (DB-per-Service)

- 3 instancias separadas (`db-inventario`, `db-pedido`, `db-envio`)
- Aisladas en red `internal`, sin puertos expuestos al host
- Volúmenes persistentes: `pg-{inventario,pedido,envio}-data`
- Healthcheck con `pg_isready`

## Patrones aplicados

A nivel arquitectónico y de código, este monorepo demuestra:

- **Microservicios** con **Database per Service** (sin FKs cruzadas)
- **API Gateway** (KrakenD) + **Ingress** (Traefik) separados
- **Backend For Frontend** con orquestación de varios MS (saga simplificada para checkout)
- **Repository / Service Layer / DTO** en cada MS
- **State Machine** para Pedido y Envio (transiciones validadas en el service)
- **Optimistic Locking** (`@Version`) en `Stock`
- **Circuit-Breaker-lite** en BFF (fetch con `AbortController` + tolerancia a fallos parciales en agregaciones)
- **RFC 7807 ProblemDetail** para errores en todos los servicios
- **Schema-first migrations** con Flyway, validadas por Hibernate

Detalle en cada README de componente y en `docs/modelo-datos.md`.

## Comandos útiles

```bash
# Logs en vivo
docker compose logs -f
docker compose logs -f bff

# Rebuild de un servicio específico
docker compose up -d --build bff

# Conectar a una DB
docker compose exec db-inventario psql -U inventario -d inventario

# Bajar todo (mantiene volúmenes)
docker compose down

# Bajar todo y borrar volúmenes (⚠ pierde data)
docker compose down -v

# Validar sintaxis del compose
docker compose config
```

## Próximos pasos

1. **Activar JWT real**: configurar el JWT validator de KrakenD con secret/issuer real (Auth0 / Keycloak)
2. **Activar HTTPS**: descomentar la sección `certificatesResolvers` en `traefik.yml`
3. **Circuit Breaker robusto**: agregar Resilience4j a los MS (hoy el BFF tiene el equivalente lite)
4. **Healthchecks de MS**: agregar `spring-boot-starter-actuator` y descomentar HEALTHCHECK en los Dockerfiles
5. **Tests unitarios** por servicio (cobertura, parte de la rúbrica EV2)
6. **Arquetipo Maven** (parte de la rúbrica EV2) — hoy usamos Gradle; se puede generar uno con `mvn archetype:create-from-project` desde un MS de referencia, o mantener Gradle y argumentar la elección
