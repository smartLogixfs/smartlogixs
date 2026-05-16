# ms-pedido

Microservicio de gestión de pedidos del ecosistema SmartLogix.

**Stack**: Spring Boot 4.0.6 · Java 25 · PostgreSQL 16 · Flyway · JPA/Hibernate · Gradle 9.

## Responsabilidad

Dueño del agregado **Pedido**. Persiste pedidos, sus ítems y la auditoría de cambios de estado.
No comparte tablas con otros MS (`Database per Service`). Los IDs de cliente / producto / marketplace son **lógicos** — no hay FK cruzadas.

## Modelo de dominio

| Tabla | Función |
|---|---|
| `pedidos` | Cabecera: código `PED-YYYYMMDD-XXXXXX`, estado, totales |
| `pedido_items` | Detalle: producto, sku, cantidad, precio unitario, subtotal |
| `pedido_historial` | Auditoría de transiciones de estado |

Máquina de estados:
```
PENDIENTE → APROBADO → EN_PREPARACION → ENVIADO → ENTREGADO
PENDIENTE → RECHAZADO
* → CANCELADO (antes de ENVIADO)
```

Detalle completo en [`docs/modelo-datos.md`](../../../docs/modelo-datos.md).

## API REST

Base path: `/pedidos`

| Método | Path | Descripción |
|---|---|---|
| POST | `/pedidos` | Crear pedido (valida items, calcula totales con IVA 19%) |
| GET | `/pedidos/{id}` | Obtener por ID |
| GET | `/pedidos/codigo/{codigo}` | Obtener por código |
| GET | `/pedidos/cliente/{idCliente}` | Listar pedidos de un cliente |
| GET | `/pedidos?estado=APROBADO` | Listar (filtro opcional por estado) |
| PATCH | `/pedidos/{id}/estado` | Cambiar estado (valida transición permitida) |

Validación de entrada con Bean Validation. Errores devueltos como RFC 7807 `application/problem+json` por `GlobalExceptionHandler`.

## Cómo ejecutar

### Vía Docker (recomendado)

Desde la raíz del monorepo:

```bash
docker compose up -d db-pedido ms-pedido
```

Variables que toma del compose:
- `SERVER_PORT` (default 8080)
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`

### Local (sin Docker)

Requiere Postgres 16 corriendo en `localhost:5432` con DB/user `pedido`:

```bash
./gradlew bootRun
```

Por defecto se conecta a `jdbc:postgresql://localhost:5432/pedido` (sobrescribir vía env vars si difiere).

### Build de la imagen

```bash
docker build -t smartlogix-ms-pedido .
```

## Probar la API

Desde el host con Traefik corriendo:

```bash
# Crear un pedido
curl -X POST http://bff.smartlogix.localhost/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "idCliente": "CL-001",
    "tipo": "ESTANDAR",
    "items": [
      {"idProducto": 1, "sku": "SKU-001", "cantidad": 2, "precioUnitario": 5000}
    ]
  }'

# Listar
curl http://bff.smartlogix.localhost/pedidos

# Cambiar estado
curl -X PATCH http://bff.smartlogix.localhost/pedidos/1/estado \
  -H "Content-Type: application/json" \
  -d '{"estado": "APROBADO", "motivo": "Pago confirmado"}'
```

## Estructura del proyecto

```
src/main/java/cl/smartlogix/pedido/
├── PedidoApplication.java
├── controller/
│   ├── OrderController.java          # /pedidos
│   └── GlobalExceptionHandler.java
├── service/
│   ├── OrderService.java             # interfaz
│   └── OrderServiceImpl.java         # máquina de estados, cálculo de totales
├── repository/                       # Spring Data JPA
├── dto/                              # records de I/O + Bean Validation
└── model/                            # entidades JPA

src/main/resources/
├── application.properties            # ddl-auto=validate, flyway enabled
└── db/migration/V1__init_schema.sql  # schema autoritativo
```

## Patrones aplicados

- **Repository Pattern** (Spring Data JPA)
- **Service Layer** (interfaz + impl, transaccional)
- **DTO** (records con `from(Entity)` estáticos)
- **State Machine** (transiciones de estado validadas en `OrderServiceImpl`)
- **Auditoría** (`pedido_historial` registra cada cambio)
