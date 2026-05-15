# ms-inventario

Microservicio de gestión de inventario (productos, bodegas, stock) del ecosistema SmartLogix.

**Stack**: Spring Boot 4.0.6 · Java 25 · PostgreSQL 16 · Flyway · JPA/Hibernate · Gradle 9.

## Responsabilidad

Dueño de productos, bodegas y stock. Es el **único MS autorizado para mutar existencias**.
Otros MS (ej. ms-pedido durante un checkout) deben llamar su API REST para reservar/liberar/mover stock — no acceden a su DB directamente.

## Modelo de dominio

| Tabla | Función |
|---|---|
| `productos` | Catálogo: SKU único, precio, activo |
| `bodegas` | Ubicaciones físicas |
| `stock` | Existencia por (producto, bodega) con `cantidad`, `cant_reservada`, `stock_minimo`. **Optimistic locking** con `@Version` |
| `movimientos_stock` | Bitácora de ENTRADA / SALIDA / RESERVA / LIBERACION |

Invariantes garantizadas a nivel DB:
- `cantidad >= 0`, `cant_reservada >= 0`, `cant_reservada <= cantidad`
- Único por `(id_producto, id_bodega)`

Detalle completo en [`docs/modelo-datos.md`](../../../docs/modelo-datos.md).

## API REST

| Método | Path | Descripción |
|---|---|---|
| **Productos** | | |
| POST | `/productos` | Crear (SKU único; conflict si existe) |
| GET | `/productos/{id}` | Obtener por ID |
| GET | `/productos/sku/{sku}` | Obtener por SKU |
| GET | `/productos` | Listar |
| PATCH | `/productos/{id}` | Actualizar (parcial: nombre, descripción, precio, activo) |
| **Bodegas** | | |
| POST | `/bodegas` | Crear |
| GET | `/bodegas/{id}` | Obtener por ID |
| GET | `/bodegas` | Listar |
| **Stock** | | |
| GET | `/stock/{idProducto}/{idBodega}` | Obtener stock en una bodega |
| GET | `/stock/producto/{idProducto}` | Listar stocks del producto en todas las bodegas |
| GET | `/stock/producto/{idProducto}/disponible` | Total disponible agregado |
| GET | `/stock/bajo` | Stocks bajo el `stock_minimo` |
| GET | `/stock/{idStock}/historial` | Movimientos del stock |
| POST | `/stock/entrada` | Suma cantidad (crea el stock si no existe) |
| POST | `/stock/salida` | Resta cantidad (409 si `disponible < cantidad`) |
| POST | `/stock/reservar` | Aumenta `cant_reservada` |
| POST | `/stock/liberar` | Disminuye `cant_reservada` |

Bajo concurrencia el `@Version` puede gatillar `OptimisticLockingFailureException` → respondido como `409 Conflicto de concurrencia` por `GlobalExceptionHandler`.

## Cómo ejecutar

### Vía Docker (recomendado)

```bash
docker compose up -d db-inventario ms-inventario
```

### Local (sin Docker)

Requiere Postgres 16 corriendo con DB/user `inventario`:

```bash
./gradlew bootRun
```

### Build de la imagen

```bash
docker build -t smartlogix-ms-inventario .
```

## Probar la API

```bash
# Crear producto
curl -X POST http://bff.smartlogix.localhost/inventario/productos \
  -H "Content-Type: application/json" \
  -d '{"sku": "SKU-001", "nombre": "Caja 30x20", "precio": 2500}'

# Crear bodega
curl -X POST http://bff.smartlogix.localhost/inventario/bodegas \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Bodega Central", "ubicacion": "Santiago"}'

# Entrada de stock (crea el registro si es la primera)
curl -X POST http://bff.smartlogix.localhost/inventario/stock/entrada \
  -H "Content-Type: application/json" \
  -d '{"idProducto": 1, "idBodega": 1, "cantidad": 100}'

# Reservar
curl -X POST http://bff.smartlogix.localhost/inventario/stock/reservar \
  -H "Content-Type: application/json" \
  -d '{"idProducto": 1, "idBodega": 1, "cantidad": 5, "referenciaPedido": "PED-20260513-AB12CD"}'

# Stock bajo
curl http://bff.smartlogix.localhost/inventario/stock/bajo
```

## Estructura del proyecto

```
src/main/java/cl/smartlogix/inventario/
├── InventarioApplication.java
├── controller/
│   ├── ProductController.java        # /productos
│   ├── WarehouseController.java      # /bodegas
│   ├── StockController.java          # /stock
│   └── GlobalExceptionHandler.java
├── service/
│   ├── ProductService(Impl).java
│   ├── WarehouseService(Impl).java
│   └── StockService(Impl).java       # entrada/salida/reservar/liberar
├── repository/
├── dto/
└── model/

src/main/resources/
├── application.properties
└── db/migration/V1__init_schema.sql
```

## Patrones aplicados

- **Repository / Service Layer / DTO**
- **Optimistic Locking** (`@Version` en `Stock`)
- **Event Sourcing simplificado**: cada operación deja un `MovimientoStock` con `referencia_pedido` (ID lógico cruzado con ms-pedido)
- **Aggregate Root**: `Stock` es la raíz; sus movimientos se crean en transacción atómica con el cambio de cantidades
