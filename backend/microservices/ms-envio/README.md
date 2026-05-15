# ms-envio

Microservicio de gestión de envíos y transportistas del ecosistema SmartLogix.

**Stack**: Spring Boot 4.0.6 · Java 25 · PostgreSQL 16 · Flyway · JPA/Hibernate · Gradle 9.

## Responsabilidad

Genera envíos a partir de un `id_pedido` (ID lógico de ms-pedido), asigna transportista, y registra la trazabilidad del recorrido.
Cada cambio de estado del envío genera una fila en `envio_seguimiento` con ubicación y comentario.

## Modelo de dominio

| Tabla | Función |
|---|---|
| `transportistas` | Catálogo de couriers/empresas (nombre, RUT, contacto) |
| `envios` | Envío con `tracking_number` único `ENV-YYYYMMDD-XXXXXXXX`, estado, dirección destino |
| `envio_seguimiento` | Tracking line-by-line: estado, ubicación, comentario, fecha |

Máquina de estados:
```
CREADO → ASIGNADO → EN_RUTA → ENTREGADO
                       ↓  ↑
                  INCIDENCIA (reintentable)
```

Detalle completo en [`docs/modelo-datos.md`](../../../docs/modelo-datos.md).

## API REST

| Método | Path | Descripción |
|---|---|---|
| **Envíos** | | |
| POST | `/envios` | Crear envío para un pedido (genera tracking, estado inicial `CREADO`) |
| GET | `/envios/{id}` | Obtener por ID |
| GET | `/envios/tracking/{trackingNumber}` | Obtener por número de tracking |
| GET | `/envios/pedido/{idPedido}` | Envíos asociados a un pedido |
| GET | `/envios?estado=EN_RUTA` | Listar (filtro opcional por estado) |
| GET | `/envios/{id}/seguimiento` | Historial de seguimiento |
| PATCH | `/envios/{id}/transportista` | Asignar transportista (solo si estado `CREADO`) |
| PATCH | `/envios/{id}/estado` | Cambiar estado (valida transición permitida) |
| **Transportistas** | | |
| POST | `/transportistas` | Crear (RUT único si se provee) |
| GET | `/transportistas/{id}` | Obtener por ID |
| GET | `/transportistas?activo=true` | Listar (filtro opcional por activos) |

Al transicionar a `ENTREGADO` se setea automáticamente `fecha_entrega = now()`.

## Cómo ejecutar

### Vía Docker (recomendado)

```bash
docker compose up -d db-envio ms-envio
```

### Local (sin Docker)

Requiere Postgres 16 corriendo con DB/user `envio`:

```bash
./gradlew bootRun
```

### Build de la imagen

```bash
docker build -t smartlogix-ms-envio .
```

## Probar la API

```bash
# Crear transportista
curl -X POST http://bff.smartlogix.localhost/envios/transportistas \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Chilexpress", "rut": "96.756.430-3"}'

# Crear envío (necesita un id_pedido lógico)
curl -X POST http://bff.smartlogix.localhost/envios \
  -H "Content-Type: application/json" \
  -d '{
    "idPedido": 1,
    "direccionDestino": "Av. Providencia 1234",
    "comuna": "Providencia",
    "region": "RM"
  }'

# Asignar transportista
curl -X PATCH http://bff.smartlogix.localhost/envios/1/transportista \
  -H "Content-Type: application/json" \
  -d '{"idTransportista": 1}'

# Cambiar estado con ubicación
curl -X PATCH http://bff.smartlogix.localhost/envios/1/estado \
  -H "Content-Type: application/json" \
  -d '{"estado": "EN_RUTA", "ubicacion": "Centro de distribución", "comentario": "Salió del depósito"}'

# Tracking público
curl http://bff.smartlogix.localhost/envios/tracking/ENV-20260513-ABCDEF12
```

## Estructura del proyecto

```
src/main/java/cl/smartlogix/envio/
├── EnvioApplication.java
├── controller/
│   ├── ShipmentController.java       # /envios
│   ├── CarrierController.java        # /transportistas
│   └── GlobalExceptionHandler.java
├── service/
│   ├── ShipmentService(Impl).java    # máquina de estados, generación de tracking
│   └── CarrierService(Impl).java
├── repository/
├── dto/
└── model/

src/main/resources/
├── application.properties
└── db/migration/V1__init_schema.sql
```

## Patrones aplicados

- **Repository / Service Layer / DTO**
- **State Machine** con `INCIDENCIA` como rama lateral reintentable
- **Eventual Tracking**: cada transición persiste un `EnvioSeguimiento` (audit log inmutable)
- **Aggregate Root**: `Envio` cascade-persiste `EnvioSeguimiento`; cuando se asigna transportista solo se permite en estado `CREADO` y el transportista debe estar `activo`
