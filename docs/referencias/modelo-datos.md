# SmartLogix — Modelo de Datos

Documenta el esquema persistido por cada microservicio bajo el patrón **Database per Service**: cada MS es dueño de su propia base de datos PostgreSQL 16 y no comparte tablas ni FKs físicas con los demás.

- Owners: `ms-inventario` (db-inventario), `ms-pedido` (db-pedido), `ms-envio` (db-envio).
- Aislamiento: las 3 DBs viven en la red Docker `internal` sin puertos expuestos al host.
- Versionado del schema: **Flyway** (`src/main/resources/db/migration/V1__init_schema.sql` en cada MS). Hibernate corre con `ddl-auto=validate`.
- Acoplamiento entre dominios: **IDs lógicos**, NO foreign keys físicas. La consistencia se garantiza con orquestación (BFF/Saga) y Circuit Breaker, no con `JOIN` ni constraints cruzadas.

---

## 1. Vista global — Database per Service y enlaces lógicos

```mermaid
flowchart LR
    subgraph INV[db-inventario · owner ms-inventario]
        direction TB
        BODEGAS[(bodegas)]
        PRODUCTOS[(productos)]
        STOCK[(stock)]
        MOVS[(movimientos_stock)]
        PRODUCTOS -- "1..N" --> STOCK
        BODEGAS -- "1..N" --> STOCK
        STOCK -- "1..N" --> MOVS
    end

    subgraph PED[db-pedido · owner ms-pedido]
        direction TB
        PEDIDOS[(pedidos)]
        ITEMS[(pedido_items)]
        HIST[(pedido_historial)]
        PEDIDOS -- "1..N" --> ITEMS
        PEDIDOS -- "1..N" --> HIST
    end

    subgraph ENV[db-envio · owner ms-envio]
        direction TB
        TRANSP[(transportistas)]
        ENVIOS[(envios)]
        SEG[(envio_seguimiento)]
        TRANSP -- "0..N" --> ENVIOS
        ENVIOS -- "1..N" --> SEG
    end

    ITEMS -. "id_producto<br/>(ID lógico)" .-> PRODUCTOS
    MOVS  -. "referencia_pedido<br/>(ID lógico)" .-> PEDIDOS
    ENVIOS -. "id_pedido<br/>(ID lógico)" .-> PEDIDOS

    classDef logical stroke-dasharray: 5 5;
```

> **Líneas continuas** = FKs físicas (`REFERENCES`) dentro de la misma DB.
> **Líneas punteadas** = IDs lógicos que cruzan dominios. Se resuelven por API/eventos, nunca por `JOIN`.

---

## 2. ER detallado — `db-inventario`

```mermaid
erDiagram
    BODEGAS ||--o{ STOCK : "almacena"
    PRODUCTOS ||--o{ STOCK : "tiene_existencia"
    STOCK ||--o{ MOVIMIENTOS_STOCK : "registra"

    BODEGAS {
        bigserial id_bodega PK
        varchar nombre
        varchar ubicacion
        boolean activo
    }
    PRODUCTOS {
        bigserial id_producto PK
        varchar sku UK
        varchar nombre
        text descripcion
        numeric precio
        boolean activo
        timestamptz created_at
        timestamptz updated_at
    }
    STOCK {
        bigserial id_stock PK
        bigint id_producto FK
        bigint id_bodega FK
        integer cantidad
        integer cant_reservada
        integer stock_minimo
        bigint version "optimistic lock"
        timestamptz updated_at
    }
    MOVIMIENTOS_STOCK {
        bigserial id_movimiento PK
        bigint id_stock FK
        varchar tipo "ENTRADA/SALIDA/RESERVA/LIBERACION"
        integer cantidad
        varchar referencia_pedido "ID lógico ms-pedido"
        timestamptz created_at
    }
```

**Invariantes (check constraints):**
- `cantidad >= 0`, `cant_reservada >= 0`, `stock_minimo >= 0`
- `cant_reservada <= cantidad` (no se puede reservar más de lo que hay)
- Unicidad: `(id_producto, id_bodega)` → un solo registro de stock por SKU/bodega
- `tipo` ∈ {ENTRADA, SALIDA, RESERVA, LIBERACION}

---

## 3. ER detallado — `db-pedido`

```mermaid
erDiagram
    PEDIDOS ||--o{ PEDIDO_ITEMS : "contiene"
    PEDIDOS ||--o{ PEDIDO_HISTORIAL : "audita"

    PEDIDOS {
        bigserial id_pedido PK
        varchar codigo UK "PED-YYYYMMDD-XXXXXX"
        varchar tipo "ESTANDAR/EXPRESS"
        varchar estado "máquina de estados"
        varchar id_cliente "ID lógico"
        varchar id_marketplace "Shopify/MercadoLibre"
        numeric subtotal
        numeric impuesto
        numeric total
        timestamptz created_at
        timestamptz updated_at
    }
    PEDIDO_ITEMS {
        bigserial id_item PK
        bigint id_pedido FK
        bigint id_producto "ID lógico ms-inventario"
        varchar sku
        integer cantidad
        numeric precio_unitario
        numeric subtotal
    }
    PEDIDO_HISTORIAL {
        bigserial id_historial PK
        bigint id_pedido FK
        varchar estado_anterior
        varchar estado_nuevo
        varchar motivo
        timestamptz created_at
    }
```

**Máquina de estados** (implementada en `OrderServiceImpl`):

```mermaid
stateDiagram-v2
    [*] --> PENDIENTE
    PENDIENTE --> APROBADO
    PENDIENTE --> RECHAZADO
    PENDIENTE --> CANCELADO
    APROBADO --> EN_PREPARACION
    APROBADO --> CANCELADO
    EN_PREPARACION --> ENVIADO
    EN_PREPARACION --> CANCELADO
    ENVIADO --> ENTREGADO
    ENTREGADO --> [*]
    RECHAZADO --> [*]
    CANCELADO --> [*]
```

**Invariantes:**
- `cantidad > 0`, `precio_unitario >= 0`, `subtotal >= 0`
- Toda transición de estado escribe una fila en `pedido_historial` con `estado_anterior` / `estado_nuevo` / `motivo`
- `ON DELETE CASCADE` desde `pedidos` hacia `pedido_items` y `pedido_historial`

---

## 4. ER detallado — `db-envio`

```mermaid
erDiagram
    TRANSPORTISTAS ||--o{ ENVIOS : "asignado_a"
    ENVIOS ||--o{ ENVIO_SEGUIMIENTO : "trackea"

    TRANSPORTISTAS {
        bigserial id_transportista PK
        varchar nombre
        varchar rut UK
        varchar telefono_contacto
        boolean activo
    }
    ENVIOS {
        bigserial id_envio PK
        bigint id_pedido "ID lógico ms-pedido"
        bigint id_transportista FK "nullable"
        varchar tracking_number UK
        varchar estado "CREADO/ASIGNADO/EN_RUTA/ENTREGADO/INCIDENCIA"
        varchar direccion_destino
        varchar comuna
        varchar region
        date fecha_estimada
        timestamptz fecha_entrega
        timestamptz created_at
        timestamptz updated_at
    }
    ENVIO_SEGUIMIENTO {
        bigserial id_seguimiento PK
        bigint id_envio FK
        varchar estado
        varchar ubicacion
        varchar comentario
        timestamptz created_at
    }
```

**Máquina de estados del envío:**

```mermaid
stateDiagram-v2
    [*] --> CREADO
    CREADO --> ASIGNADO
    ASIGNADO --> EN_RUTA
    EN_RUTA --> ENTREGADO
    EN_RUTA --> INCIDENCIA
    INCIDENCIA --> EN_RUTA : "reintento"
    INCIDENCIA --> [*] : "devolución"
    ENTREGADO --> [*]
```

**Invariantes:**
- `id_pedido` siempre presente, pero NO referencia `pedidos` (cross-DB)
- `id_transportista` es nullable: el envío puede crearse antes de asignar
- `ON DELETE CASCADE` desde `envios` hacia `envio_seguimiento`

---

## 5. Diccionario de IDs lógicos (cruces entre dominios)

Estos campos identifican entidades de OTRO microservicio. No hay constraint que los valide a nivel DB — la consistencia se gestiona en la capa de aplicación.

| Origen | Campo | Apunta a | Cuándo se materializa |
|---|---|---|---|
| `db-pedido.pedido_items.id_producto` | bigint | `db-inventario.productos.id_producto` | Al crear el pedido, el BFF resuelve el producto contra ms-inventario y guarda su `id_producto` |
| `db-pedido.pedidos.id_cliente` | varchar(64) | sistema externo (ERP/CRM de la PYME) | Provisto por el marketplace o el cliente al crear el pedido |
| `db-pedido.pedidos.id_marketplace` | varchar(64) | sistema externo (Shopify, MercadoLibre) | ID nativo del marketplace de origen |
| `db-inventario.movimientos_stock.referencia_pedido` | varchar(64) | `db-pedido.pedidos.codigo` | Cuando ms-pedido reserva stock, ms-inventario registra el movimiento con el código del pedido |
| `db-envio.envios.id_pedido` | bigint | `db-pedido.pedidos.id_pedido` | Al crear el envío, el BFF lo genera desde un pedido ya APROBADO |

**Regla de oro:** si necesitas datos de otro dominio (ej. el nombre del producto en una vista de pedido), los pide el BFF al MS dueño y los **agrega en respuesta**, nunca por JOIN entre DBs.

---

## 6. Trazabilidad código ↔ schema

| Entidad JPA | Tabla SQL | Script Flyway |
|---|---|---|
| `cl.smartlogix.inventory.model.Bodega` | `bodegas` | `ms-inventario/.../V1__init_schema.sql` |
| `cl.smartlogix.inventory.model.Producto` | `productos` | idem |
| `cl.smartlogix.inventory.model.Stock` | `stock` | idem |
| `cl.smartlogix.inventory.model.MovimientoStock` | `movimientos_stock` | idem |
| `cl.smartlogix.order.model.Pedido` | `pedidos` | `ms-pedido/.../V1__init_schema.sql` |
| `cl.smartlogix.order.model.PedidoItem` | `pedido_items` | idem |
| `cl.smartlogix.order.model.PedidoHistorial` | `pedido_historial` | idem |
| `cl.smartlogix.shipping.model.Transportista` | `transportistas` | `ms-envio/envio/.../V1__init_schema.sql` |
| `cl.smartlogix.shipping.model.Envio` | `envios` | idem |
| `cl.smartlogix.shipping.model.EnvioSeguimiento` | `envio_seguimiento` | idem |

---

## 7. Cómo regenerar este documento

Los diagramas son Mermaid embebido y se renderizan nativamente en:
- GitHub (vista preview del repo)
- VS Code con extensión `bierner.markdown-mermaid`
- IntelliJ IDEA con el plugin Mermaid

Para exportar a PNG/SVG: usar la CLI `@mermaid-js/mermaid-cli` o pegarlos en https://mermaid.live.
