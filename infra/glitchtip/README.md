# Monitoreo de errores con GlitchTip (local)

Stack self-hosted de **GlitchTip** —plataforma open-source de *error tracking*
compatible con el protocolo y los SDK de **Sentry**— para capturar en runtime
los errores de todas las capas de SmartLogix (frontend, BFF y los 5
microservicios).

Cada componente se instrumenta con el SDK oficial de Sentry apuntando al **DSN**
del proyecto GlitchTip correspondiente. El DSN se lee **siempre de una variable
de entorno**; si está vacía, el SDK queda en *no-op* y la app funciona igual (no
hay DSNs versionados en el repo).

---

## 1. Levantar GlitchTip

```bash
docker compose -f infra/glitchtip/docker-compose.yml up -d
```

Arranca `db` (Postgres), `redis`, un job `migrate` (corre y termina) y los
contenedores `web` + `worker`. Tarda ~1 min.

- Dashboard: http://localhost:8000

> Recomendado fijar una `GLITCHTIP_SECRET_KEY` propia en tu `.env` antes de
> levantar (cualquier cadena larga aleatoria). El default sólo sirve para dev.

## 2. Crear cuenta, organización y proyectos

1. En http://localhost:8000 → **Register** (el registro abierto está activado).
2. Crea una organización, p. ej. **SmartLogix**.
3. Crea **un proyecto por componente** (plataforma sugerida entre paréntesis):

   | Proyecto       | Plataforma        | Variable de entorno del DSN |
   |----------------|-------------------|-----------------------------|
   | `frontend`     | React             | `VITE_GLITCHTIP_DSN`        |
   | `bff`          | Node.js / Express | `BFF_GLITCHTIP_DSN`         |
   | `ms-auth`      | Java              | `MS_AUTH_DSN`               |
   | `ms-user`      | Java              | `MS_USER_DSN`               |
   | `ms-order`     | Java              | `MS_ORDER_DSN`              |
   | `ms-inventory` | Java              | `MS_INVENTORY_DSN`          |
   | `ms-shipping`  | Java              | `MS_SHIPPING_DSN`           |

4. En cada proyecto, copia su **DSN** (`http://<key>@localhost:8000/<id>`) y
   pégalo en la variable correspondiente de tu `.env` (ver `.env.example`).

## 3. Levantar la app con los DSN puestos

```bash
docker compose up -d --build
```

- El **frontend** hornea su DSN en tiempo de *build* (Vite), así que si cambias
  `VITE_GLITCHTIP_DSN` hay que reconstruir su imagen (`--build`).
- **BFF** y **microservicios** leen el DSN en runtime desde el entorno.

## 4. Probar que llegan los eventos (endpoints de demo)

Cada capa expone endpoints `/demo` para provocar eventos en vivo durante la
presentación (Letra G). Dispáralos y verifica que aparecen en el proyecto
respectivo del dashboard. En dev los microservicios se publican en 8081-8085
(ver `docker-compose.override.yml`); el BFF en :3000.

| Capa | Provocar error (500 / excepción) | Provocar log de nivel ERROR |
|------|----------------------------------|-----------------------------|
| ms-auth      | `GET http://localhost:8081/demo/error` | `GET http://localhost:8081/demo/log` |
| ms-user      | `GET http://localhost:8082/demo/error` | `GET http://localhost:8082/demo/log` |
| ms-order     | `GET http://localhost:8083/demo/error` | `GET http://localhost:8083/demo/log` |
| ms-inventory | `GET http://localhost:8084/demo/error` | `GET http://localhost:8084/demo/log` |
| ms-shipping  | `GET http://localhost:8085/demo/error` | `GET http://localhost:8085/demo/log` |
| BFF          | `GET http://localhost:3000/demo/error` | `GET http://localhost:3000/demo/log` |
| Frontend     | Botón **"GlitchTip: probar error"** (abajo a la derecha) | — |

Cómo se captura en cada capa:

- **Microservicios**: `/demo/error` lanza una excepción que el
  `GlobalExceptionHandler` (catch-all `Exception`) registra con `log.error(...)`;
  `/demo/log` emite un `log.error(...)` directo. En ambos casos el appender
  `sentry-logback` (nivel `ERROR`) envía el evento a GlitchTip.
- **BFF**: `/demo/error` lanza una excepción capturada por
  `Sentry.setupExpressErrorHandler`; `/demo/log` usa `Sentry.captureMessage(..., "error")`.
- **Frontend**: el botón dispara un error de render que captura el
  `Sentry.ErrorBoundary` de `main.tsx`.

> Todos los `/demo` son sólo para demostración. Si el DSN del componente está
> vacío, el SDK queda en no-op y el endpoint responde igual (no se envía nada).

## 5. Apagar el stack

```bash
docker compose -f infra/glitchtip/docker-compose.yml down        # conserva datos
docker compose -f infra/glitchtip/docker-compose.yml down -v     # borra datos
```

---

## Despliegue en Kubernetes (opcional)

Hay manifiestos en `infra/glitchtip/k8s/` (namespace propio `glitchtip`), independientes
del `kustomization.yaml` raíz de la app:

```bash
kubectl apply -k infra/glitchtip/k8s
# exponer el dashboard:
kubectl -n glitchtip port-forward svc/glitchtip-web 8000:8000   # http://localhost:8000
```

Los DSN de los proyectos se rellenan en el ConfigMap `smartlogix-config`
(`infra/k8s/base/configmap.yaml`, claves `*_DSN`); los deployments los leen con
`optional: true`, así que pueden quedar vacíos (SDK en no-op). Como las imágenes de los
componentes se pre-construyen con tag, hay que **reconstruir** las imágenes para que
incluyan el SDK antes de redeployar.

---

## Notas

- Es un stack sólo para dev/demo local (consume RAM: web + worker + postgres +
  redis), igual que el de SonarQube en `infra/sonarqube/`.
- Los proyectos y sus DSN se crean **después** del primer arranque, por eso no
  se pueden versionar y van por variable de entorno.
- Los DSN de Sentry/GlitchTip son públicos por diseño (identifican el proyecto
  de ingesta, no dan acceso a los datos), por eso en Kubernetes van en el
  ConfigMap `smartlogix-config` y no en un Secret.
- Instrumentación por capa:
  - Frontend → `@sentry/react` en `frontend/src/main.tsx`.
  - BFF → `@sentry/node` en `backend/bff/src/instrument.ts`.
  - Microservicios → `io.sentry:sentry-logback` + `logback-spring.xml`.
