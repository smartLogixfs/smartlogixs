# SmartLogix

> Plataforma logística para PYMEs eCommerce. Monorepo con arquitectura de microservicios, Backend For Frontend, API Gateway, Ingress y bases de datos aisladas por servicio.

**Asignatura**: DSY1106 Desarrollo Fullstack III · Evaluación Parcial N°2

### READMEs por componente

| Componente | README |
|---|---|
| Frontend (React 19 + TS) | [`frontend/README.md`](frontend/README.md) |
| BFF (Node.js 20 + Express) | [`backend/bff/README.md`](backend/bff/README.md) |
| API Gateway (KrakenD) | [`backend/api-gateway/README.md`](backend/api-gateway/README.md) |
| ms-order | [`backend/ms-order/README.md`](backend/ms-order/README.md) |
| ms-inventory | [`backend/ms-inventory/README.md`](backend/ms-inventory/README.md) |
| ms-shipping | [`backend/ms-shipping/README.md`](backend/ms-shipping/README.md) |
| ms-user | [`backend/ms-user/README.md`](backend/ms-user/README.md) |
| ms-auth | [`backend/ms-auth/README.md`](backend/ms-auth/README.md) |


---

## Tabla de contenidos

1. [Resumen ejecutivo](#1-resumen-ejecutivo)
2. [Arquitectura](#2-arquitectura)
3. [Componentes](#3-componentes)
4. [Flujos clave](#4-flujos-clave)
5. [Modelo de datos](#5-modelo-de-datos)
6. [Patrones aplicados](#6-patrones-aplicados)
7. [Stack tecnológico](#7-stack-tecnológico)
8. [Levantar el stack](#8-levantar-el-stack)
9. [Estructura del proyecto](#9-estructura-del-proyecto)
10. [Estrategia de branching](#10-estrategia-de-branching)
11. [Sobre el arquetipo: Gradle vs Maven](#11-sobre-el-arquetipo-gradle-vs-maven)
12. [Documentación complementaria](#12-documentación-complementaria)
13. [Próximos pasos](#13-próximos-pasos)

---

## 1. Resumen ejecutivo

SmartLogix resuelve la coordinación logística de PYMEs eCommerce gestionando tres dominios desacoplados — **pedidos**, **inventario** y **envíos** — sobre una arquitectura de microservicios con consistencia eventual. El frontend (React 19 + Bootstrap) consume una API optimizada en un **BFF Node.js** que orquesta llamadas a los microservicios Spring Boot 4 y compone respuestas para cada pantalla. **KrakenD** actúa como API Gateway detrás de **Traefik** (Ingress), agregando rate limiting, JWT validation y CORS. Cada microservicio usa su propia base **PostgreSQL 16** versionada con Flyway, sin FKs cruzadas.

El proyecto demuestra **5 patrones arquitectónicos** (Microservicios, DB per Service, API Gateway, Ingress separado, BFF) y **más de 10 patrones de diseño** (Repository, Service Layer, DTO, State Machine, Optimistic Locking, Saga simplificada, Composite Service, Aggregate Root, Circuit-Breaker-lite, RFC 7807 Problem Detail).

---

## 2. Arquitectura

### 2.1 Diagrama de contenedores

```mermaid
flowchart LR
    User((Usuario))

    subgraph Web["Red pública: web"]
        Traefik["Traefik v3.5<br/>Ingress Controller<br/>:80 / :443"]
        Frontend["Frontend<br/>React 19 + Vite + Nginx<br/>app.smartlogix.localhost"]
        Krakend["KrakenD v2.10<br/>API Gateway<br/>api.smartlogix.localhost"]
        BFF["BFF<br/>Node.js 20 + Express<br/>bff.smartlogix.localhost"]
    end

    subgraph Internal["Red privada: internal"]
        MSPed["ms-order<br/>Spring Boot 4"]
        MSInv["ms-inventory<br/>Spring Boot 4"]
        MSEnv["ms-shipping<br/>Spring Boot 4"]
        DBPed[("db-order<br/>PostgreSQL 16")]
        DBInv[("db-inventory<br/>PostgreSQL 16")]
        DBEnv[("db-shipping<br/>PostgreSQL 16")]
    end

    User -->|HTTP/HTTPS| Traefik
    Traefik --> Frontend
    Traefik --> Krakend
    Traefik --> BFF
    Krakend -->|/api/*| BFF
    BFF -->|REST/JSON| MSPed
    BFF -->|REST/JSON| MSInv
    BFF -->|REST/JSON| MSEnv
    MSPed -->|JDBC| DBPed
    MSInv -->|JDBC| DBInv
    MSEnv -->|JDBC| DBEnv

    classDef edge fill:#e1f5ff,stroke:#0288d1
    classDef bff fill:#fff3e0,stroke:#f57c00
    classDef ms fill:#e8f5e9,stroke:#388e3c
    classDef db fill:#f3e5f5,stroke:#7b1fa2
    class Traefik,Frontend,Krakend edge
    class BFF bff
    class MSPed,MSInv,MSEnv ms
    class DBPed,DBInv,DBEnv db
```

### 2.2 Topología de red Docker

```mermaid
flowchart TB
    subgraph WebNet["red: web (pública)"]
        T[Traefik]
        F[Frontend]
        K[KrakenD]
        B[BFF]
    end

    subgraph IntNet["red: internal (internal=true, sin acceso a Internet)"]
        BB[BFF<br/><i>dual-homed</i>]
        KK[KrakenD<br/><i>dual-homed</i>]
        P[ms-order]
        I[ms-inventory]
        E[ms-shipping]
        DP[(db-order)]
        DI[(db-inventory)]
        DE[(db-shipping)]
    end

    B -.->|conecta<br/>vía interna| BB
    K -.->|conecta<br/>vía interna| KK
    BB --> P & I & E
    KK --> BB
    P --> DP
    I --> DI
    E --> DE
```

`web` es la red pública: ingress, frontend y los gateways. `internal` está marcada `internal: true` — Docker bloquea acceso saliente a Internet desde esa red. Las DBs **nunca** tienen puertos expuestos al host.

### 2.3 Flujo de una petición

```mermaid
sequenceDiagram
    autonumber
    participant U as Usuario
    participant T as Traefik (Ingress)
    participant K as KrakenD (Gateway)
    participant B as BFF
    participant M as ms-order
    participant D as db-order

    U->>T: GET app.smartlogix.localhost/dashboard
    T->>T: Match Host header → router
    T-->>U: HTML/JS (frontend)

    U->>T: GET api.smartlogix.localhost/api/pedidos
    T->>K: forward (con middlewares: CORS, rate-limit, secure-headers)
    K->>B: GET /pedidos
    B->>M: GET /pedidos (HTTP/JSON, timeout 5 s)
    M->>D: SELECT pedidos
    D-->>M: rows
    M-->>B: 200 OK JSON
    B-->>K: 200 OK JSON
    K-->>T: 200 OK JSON
    T-->>U: 200 OK JSON
```

---

## 3. Componentes

| Capa | Componente | Responsabilidad | Stack |
|---|---|---|---|
| Edge | **Traefik** | Ingress: routing por host, TLS termination (preparado), middlewares de seguridad | Traefik v3.5 |
| Gateway | **KrakenD** | API Gateway: routing `/api/*`, rate limiting, JWT validation, CORS | KrakenD v2.10 |
| Presentación | **Frontend** | SPA del operador logístico (5 pantallas) | React 19, Vite 5, Bootstrap 5, TypeScript 6 |
| Adaptación | **BFF** | Orquestación de MS para el frontend (saga checkout, dashboard agregado, proxy CRUD) | Node.js 20, Express 4, zod 3 |
| Negocio | **ms-order** | Pedidos + máquina de estados + auditoría | Spring Boot 4, Java 25, JPA, Flyway |
| Negocio | **ms-inventory** | Productos, bodegas, stock (con optimistic locking) | Spring Boot 4, Java 25, JPA, Flyway |
| Negocio | **ms-shipping** | Envíos, transportistas, seguimiento | Spring Boot 4, Java 25, JPA, Flyway |
| Negocio | **ms-user** | Usuarios y perfiles (servicio reutilizado) | Spring Boot, Java, JPA |
| Negocio | **ms-auth** | Login, register, JWT issuer + JWKS (servicio reutilizado) | Spring Boot, Java, JPA |
| Datos | **PostgreSQL × 5** | DB per service, aisladas en red privada | PostgreSQL 16-alpine |

### 3.1 Endpoints clave

| MS | Path base | Operaciones | Detalle |
|---|---|---|---|
| `ms-order` | `/pedidos` | `POST`, `GET /{id}`, `GET /codigo/{c}`, `GET /cliente/{id}`, `GET ?estado=`, `PATCH /{id}/estado` | [README](backend/ms-order/) |
| `ms-inventory` | `/productos`, `/bodegas`, `/stock` | CRUD productos/bodegas + `POST /stock/{entrada,salida,reservar,liberar}` | [README](backend/ms-inventory/) |
| `ms-shipping` | `/envios`, `/transportistas` | CRUD + `PATCH /{id}/{transportista,estado}` | [README](backend/ms-shipping/) |
| `ms-user` | `/usuarios` | CRUD usuarios (servicio reutilizado) | [README](backend/ms-user/) |
| `ms-auth` | `/auth` | Login, register, JWT/JWKS (servicio reutilizado) | [README](backend/ms-auth/) |
| `BFF` | (multiple) | `GET /dashboard`, `GET /pedidos/:id/full`, `POST /checkout`, proxy CRUD | [README](backend/bff/) |

---

## 4. Flujos clave

### 4.1 Checkout (saga simplificada)

El BFF coordina tres microservicios en una saga con compensaciones best-effort cuando algo falla en medio.

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente (Frontend)
    participant B as BFF
    participant P as ms-order
    participant I as ms-inventory
    participant E as ms-shipping

    C->>B: POST /checkout { idCliente, items, idBodega, envio }
    B->>B: validar con zod
    B->>P: POST /pedidos
    P-->>B: 201 + idPedido

    loop por cada item
        B->>I: POST /stock/reservar
        I-->>B: 200 / 409 (stock insuficiente)
    end

    alt todas las reservas OK
        B->>E: POST /envios
        E-->>B: 201 + tracking
        B-->>C: 200 + { pedido, envio, reservas }
    else alguna reserva falló
        Note over B: rollback best-effort
        B->>I: POST /stock/liberar (por cada reserva ya hecha)
        B-->>C: 4xx + ProblemDetail
    end
```

### 4.2 Frontend ↔ BFF (con proxy de Vite)

```mermaid
flowchart LR
    Vite["Vite dev :5173"]
    Page["DashboardPage.tsx"]
    Hook["useFetch&lt;T&gt;"]
    Client["apiClient.ts"]
    Proxy["Vite proxy<br/>/api/* → bff:80"]
    BFF["BFF :3000<br/>(via Traefik)"]

    Page --> Hook
    Hook --> Client
    Client -->|GET /api/dashboard| Vite
    Vite --> Proxy
    Proxy -->|Host: bff.smartlogix.localhost| BFF
    BFF -->|JSON DashboardResponse| Proxy
    Proxy -->|JSON| Client
    Client -->|tipado T| Hook
    Hook -->|FetchState<T>| Page
```

En dev se evita CORS reescribiendo `/api/*` a través del proxy de Vite. En prod, Nginx (que sirve el build) replica el mismo prefijo `/api/*` hacia el BFF dentro de la red Docker.

---

## 5. Modelo de datos

Tres agregados independientes, sin FKs cruzadas — los IDs que cruzan dominios son **identificadores lógicos** (strings o longs) que cada servicio valida vía API REST.

```mermaid
erDiagram
    PEDIDO ||--o{ PEDIDO_ITEM : tiene
    PEDIDO ||--o{ PEDIDO_HISTORIAL : audita
    PEDIDO {
        long idPedido PK
        string codigo "PED-YYYYMMDD-XXXXXX"
        enum estado
        string idCliente "ID lógico"
        decimal subtotal
        decimal impuesto
        decimal total
    }
    PEDIDO_ITEM {
        long idItem PK
        long idProducto "ID lógico → inventario"
        int cantidad
        decimal precioUnitario
    }

    PRODUCTO ||--o{ STOCK : "existe en"
    BODEGA ||--o{ STOCK : "almacena"
    STOCK ||--o{ MOVIMIENTO_STOCK : registra
    PRODUCTO {
        long idProducto PK
        string sku UK
        decimal precio
    }
    STOCK {
        long idStock PK
        int cantidad
        int cantReservada
        int stockMinimo
        long version "Optimistic lock"
    }

    ENVIO ||--o{ ENVIO_SEGUIMIENTO : registra
    TRANSPORTISTA ||--o{ ENVIO : "asigna"
    ENVIO {
        long idEnvio PK
        long idPedido "ID lógico → pedido"
        string trackingNumber UK "ENV-YYYYMMDD-XXXXXXXX"
        enum estado
        string direccionDestino
    }
```

Detalle completo en [`docs/modelo-datos.md`](docs/modelo-datos.md).

### 5.1 Máquinas de estado

```mermaid
stateDiagram-v2
    direction LR
    [*] --> PENDIENTE
    PENDIENTE --> APROBADO
    PENDIENTE --> RECHAZADO
    PENDIENTE --> CANCELADO
    APROBADO --> EN_PREPARACION
    APROBADO --> CANCELADO
    EN_PREPARACION --> ENVIADO
    EN_PREPARACION --> CANCELADO
    ENVIADO --> ENTREGADO
    RECHAZADO --> [*]
    ENTREGADO --> [*]
    CANCELADO --> [*]
```

*Estados de `Pedido`*: las transiciones se validan en `OrderServiceImpl` contra un `Map<EstadoPedido, Set<EstadoPedido>>` declarado como datos. Cualquier transición ilegal devuelve **409 Conflict** vía `GlobalExceptionHandler`.

```mermaid
stateDiagram-v2
    direction LR
    [*] --> CREADO
    CREADO --> ASIGNADO : asignar transportista
    ASIGNADO --> EN_RUTA
    EN_RUTA --> ENTREGADO
    EN_RUTA --> INCIDENCIA
    INCIDENCIA --> EN_RUTA : reintentar
    INCIDENCIA --> ENTREGADO
    ENTREGADO --> [*]
```

*Estados de `Envio`*: `INCIDENCIA` es una rama lateral **reintentable**. Cada transición persiste una fila en `envio_seguimiento` (audit log inmutable).

---

## 6. Patrones aplicados

### 6.1 Arquitectónicos

| Patrón | Dónde se ve | Problema que resuelve |
|---|---|---|
| **Microservicios** | 3 MS Spring Boot independientes | Despliegue y evolución por dominio, sin acoplamiento de releases |
| **Database per Service** | `db-order`, `db-inventory`, `db-shipping`, `db-user`, `db-auth` aisladas | Cada equipo evoluciona su schema sin coordinar |
| **API Gateway** | KrakenD v2.10 | Cross-cutting: rate limiting, JWT, CORS, sin contaminar los MS |
| **Ingress separado** | Traefik v3.5 + KrakenD | TLS/routing por host (edge) separado de policy de API |
| **Backend For Frontend** | Node.js + Express | Endpoint óptimo por pantalla, agregación, orquestación |

### 6.2 De diseño (selección — más detalle en cada README de componente)

- **Repository Pattern** (Spring Data JPA repositories)
- **Service Layer** (interfaz + impl, transaccional)
- **DTO** (records Java, inmutables, validables)
- **State Machine** (Pedido + Envio)
- **Optimistic Locking** (`@Version` en `Stock`)
- **Saga simplificada / Composite Service** (checkout con compensaciones)
- **Aggregate Root** (`Pedido` → items + historial; `Envio` → seguimiento; `Stock` → movimientos)
- **Circuit-Breaker-lite** (BFF: `AbortController` + tolerancia parcial en agregaciones)
- **RFC 7807 ProblemDetail** (formato unificado de errores en MS y BFF)
- **Schema-first migrations** (Flyway autoritativo, Hibernate en `ddl-auto=validate`)

Análisis completo en [`docs/analisis-patrones-arquetipos.pdf`](docs/analisis-patrones-arquetipos.pdf).

---

## 7. Stack tecnológico

| Capa | Tecnologías |
|---|---|
| Frontend | React 19, Vite 5, TypeScript 6, react-router-dom 7, react-bootstrap 5 |
| BFF | Node.js 20, Express 4, http-proxy-middleware 3, zod 3, morgan |
| Gateway | KrakenD v2.10 (declarativo via `krakend.json`) |
| Ingress | Traefik v3.5 (provider File) |
| Backend MS | Spring Boot 4.0.6, Java 25, Spring Web MVC, Spring Data JPA, Hibernate, Flyway, Bean Validation, Lombok |
| DB | PostgreSQL 16-alpine |
| Build | Gradle 9 (Groovy DSL) — ver [§11](#11-sobre-el-arquetipo-gradle-vs-maven) |
| Tests | JUnit 5, Mockito, Spring `@WebMvcTest` (en rama `feature/tests-unitarios`) |
| Contenedores | Docker + Docker Compose v2 |

---

## 8. Levantar el stack

### 8.1 Pre-requisitos

- **Docker Desktop** (Windows/Mac) o Docker Engine + Compose v2 (Linux)
- En Windows, agregar al `hosts` (`C:\Windows\System32\drivers\etc\hosts`, como administrador):

```
127.0.0.1 app.smartlogix.localhost
127.0.0.1 api.smartlogix.localhost
127.0.0.1 bff.smartlogix.localhost
127.0.0.1 traefik.smartlogix.localhost
```

> En Linux/Mac, `*.localhost` resuelve automáticamente; los browsers modernos también.

### 8.2 Build + up

```bash
cp .env.example .env       # editar passwords si lo necesitas
docker compose up --build -d
docker compose ps          # esperar 10 contenedores Up
```

### 8.3 Smoke tests

| URL | Qué deberías ver |
|---|---|
| http://app.smartlogix.localhost | Frontend React |
| http://api.smartlogix.localhost/api/pedidos | Listado vía KrakenD → BFF |
| http://bff.smartlogix.localhost/health | `{"status":"ok","service":"bff"}` |
| http://bff.smartlogix.localhost/dashboard | Agregados de estado del sistema |
| http://traefik.smartlogix.localhost | Dashboard Traefik |

### 8.4 Comandos útiles

```bash
docker compose logs -f bff               # logs en vivo de un servicio
docker compose up -d --build bff         # rebuild aislado
docker compose exec db-order psql -U pedido -d pedido   # conectarse a una DB
docker compose down                      # bajar (mantiene volúmenes)
docker compose down -v                   # bajar + borrar data
docker compose config                    # validar sintaxis
```

---

## 9. Estructura del proyecto

```
smartlogixs/
├── docker-compose.yml                # orquesta el stack completo
├── .env.example                      # variables (copiar a .env)
├── infra/
│   └── traefik/
│       ├── traefik.yml               # config estática
│       └── dynamic/                  # routers + middlewares
├── docs/
│   ├── modelo-datos.md               # ER y máquinas de estado
│   ├── analisis-patrones-arquetipos.pdf
│   ├── plan-branching.pdf
│   └── repositorios.txt
├── frontend/                         # SPA React 19 + TypeScript
│   ├── src/
│   │   ├── client/                   # apiClient.ts, useFetch.ts
│   │   ├── types/api.ts              # DTOs TS (espejo de los records Java)
│   │   ├── pages/                    # 5 páginas conectadas al BFF
│   │   └── components/               # Layout, Sidebar
│   └── vite.config.js                # proxy /api/* → BFF en dev
└── backend/
    ├── bff/                          # Node.js 20 + Express + zod
    │   └── src/{routes,services,clients,schemas,middleware}/
    ├── api-gateway/                  # KrakenD declarativo
    ├── ms-order/                     # Spring Boot 4 (pedidos)
    ├── ms-inventory/                 # Spring Boot 4 (inventario)
    ├── ms-shipping/                  # Spring Boot 4 (envíos)
    ├── ms-user/                      # Spring Boot (usuarios — reutilizado)
    └── ms-auth/                      # Spring Boot (auth/JWT — reutilizado)
```

---

## 10. Estrategia de branching

Se usa **GitFlow simplificado** con tres tipos de rama: `main` (estable, entregable), `develop` (integración) y `feature/<kebab-case>` (trabajo en curso). Cada feature se cierra con un **Pull Request** contra `develop` o `main` y se mergea preservando la historia (merge commit, no squash).

```mermaid
gitGraph
    commit id: "initial"
    branch develop
    commit id: "estructura inicial"
    branch feature/frontend-setup
    commit id: "setup TS + Vite"
    commit id: "dashboard visual"
    checkout main
    merge feature/frontend-setup tag: "PR #2"
    branch feature/improvement-pedido
    commit id: "DTOs + state machine"
    checkout main
    merge feature/improvement-pedido tag: "PR #4"
    branch feature/frontend-backend-integration
    commit id: "apiClient + types"
    commit id: "5 pages conectadas"
    checkout main
    merge feature/frontend-backend-integration tag: "PR #5"
```

Documento completo (estrategia, evidencia, gestión de conflictos): [`docs/plan-branching.pdf`](docs/plan-branching.pdf).

---

## 11. Sobre el arquetipo: Gradle vs Maven

La rúbrica EV2 menciona "arquetipos Maven". En este monorepo los 3 microservicios y el BFF se construyeron con **Gradle 9** porque:

1. **Spring Initializr (el arquetipo oficial de Spring Boot 4) emite por defecto proyectos Gradle**. Es la herramienta recomendada por VMware/Broadcom, equivalente conceptual al `mvn archetype:generate` pero con catálogo siempre actualizado.
2. **Los `build.gradle` cumplen el rol de un arquetipo**: estructura idéntica entre los 3 MS, plantilla copy-paste para crear un nuevo servicio.
3. **Performance**: `./gradlew bootJar` toma ~30 s por MS (incremental + build cache). El equivalente Maven sería 60–90 s.
4. **DSL declarativo más legible** que el XML de Maven.

Si la rúbrica exige Maven literal, basta con `mvn archetype:create-from-project` desde un MS de referencia para generar el arquetipo a partir del código actual (operación reversible de ~1–2 h). Análisis completo en [`docs/analisis-patrones-arquetipos.pdf`](docs/analisis-patrones-arquetipos.pdf) §5.

---

## 12. Documentación complementaria

| Documento | Propósito |
|---|---|
| [`docs/modelo-datos.md`](docs/modelo-datos.md) | ER detallado y máquinas de estado |
| [`docs/analisis-patrones-arquetipos.pdf`](docs/analisis-patrones-arquetipos.pdf) | Análisis de patrones de diseño y arquitectónicos |
| [`docs/plan-branching.pdf`](docs/plan-branching.pdf) | Estrategia de branching + evidencia + resolución de conflictos |
| [`docs/repositorios.txt`](docs/repositorios.txt) | Enlaces a GitHub por componente |

---

## 13. Próximos pasos

1. **Activar JWT real**: configurar el JWT validator de KrakenD con secret/issuer real (Auth0 / Keycloak)
2. **Activar HTTPS**: descomentar la sección `certificatesResolvers` en `traefik.yml`
3. **Circuit Breaker robusto**: agregar Resilience4j a los MS (hoy el BFF tiene el equivalente lite)
4. **Healthchecks de MS**: agregar `spring-boot-starter-actuator` + `HEALTHCHECK` en los Dockerfiles (implementado en la rama `feature/tests-unitarios`, pendiente de merge)
5. **Tests unitarios** por servicio con cobertura ≥70% (implementado en la rama `feature/tests-unitarios`, pendiente de merge)
6. **Arquetipo Maven explícito** si la rúbrica lo exige literalmente
