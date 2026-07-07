# SmartLogix en Kubernetes

Despliegue del stack en Kubernetes. **Desacoplado**: cada microservicio mantiene sus propios manifests dentro de su carpeta (`backend/<ms>/k8s/` y `frontend/k8s/`). Aquí en `infra/k8s/` solo viven los recursos compartidos (Namespace, ConfigMap, Secret template, Ingress) y el `kustomization.yaml` raíz que levanta todo el sistema.

← Volver a [README raíz](../../README.md)

---

## Tabla de contenidos

1. [Estructura](#1-estructura)
2. [Pre-requisitos](#2-pre-requisitos)
3. [Build de imágenes](#3-build-de-imágenes)
4. [Despliegue: todo el stack](#4-despliegue-todo-el-stack)
5. [Despliegue: un solo MS](#5-despliegue-un-solo-ms)
6. [Hosts locales](#6-hosts-locales)
7. [Verificación](#7-verificación)
8. [Limpieza](#8-limpieza)
9. [Notas y workarounds](#9-notas-y-workarounds)

---

## 1. Estructura

```
infra/k8s/
├── base/                       # recursos compartidos
│   ├── namespace.yaml
│   ├── configmap.yaml          # vars de entorno comunes (URLs internas, DB names)
│   ├── secret.example.yaml     # template (sin credenciales reales)
│   └── kustomization.yaml
├── ingress-traefik.yaml        # ingress Traefik (por defecto): IngressRoute + Middlewares
├── ingress.yaml                # ingress-nginx (alternativa)
├── kustomization.yaml          # orquestador raíz con bloque images: (newTag por servicio)
└── README.md

backend/
├── ms-inventory/k8s/   # Deployment + Service + DB StatefulSet + DB Service
├── ms-order/k8s/
├── ms-shipping/k8s/
├── ms-user/k8s/
├── ms-auth/k8s/        # con volume mount para llaves PEM (Secret smartlogix-keys)
├── bff/k8s/            # Deployment + Service (sin DB)
└── api-gateway/k8s/    # Deployment + Service (el ConfigMap de krakend.json se genera en backend/api-gateway/)

frontend/k8s/           # Deployment + Service (Nginx con bundle Vite)
```

Cada carpeta `k8s/` por servicio tiene su propio `kustomization.yaml`, por lo que se puede desplegar de forma aislada (útil para CI/CD o para reiniciar 1 solo MS).

## 2. Pre-requisitos

- Un cluster k8s funcional (Docker Desktop k8s, Minikube, Kind, k3d, EKS, GKE...).
- `kubectl` con contexto apuntando al cluster.
- Un Ingress Controller. Por defecto se usa **Traefik** (aporta los CRDs IngressRoute/Middleware):
  ```bash
  helm repo add traefik https://traefik.github.io/charts
  helm install traefik traefik/traefik -n traefik --create-namespace
  ```
  Alternativa nginx: cambiar el recurso `ingress-traefik.yaml` por `ingress.yaml` en `infra/k8s/kustomization.yaml`.
- Llaves RSA (`private_key.pem` y `public_key.pem`) en el directorio raíz del repo para `ms-auth`.

## 3. Build de imágenes

Construye localmente con los tags que esperan los manifests. Si usas Docker Desktop k8s, las imágenes del daemon están disponibles para el cluster sin push:

```bash
docker build -t smartlogix/ms-inventory:latest  backend/ms-inventory
docker build -t smartlogix/ms-order:latest      backend/ms-order
docker build -t smartlogix/ms-shipping:latest   backend/ms-shipping
docker build -t smartlogix/ms-user:latest       backend/ms-user
docker build -t smartlogix/ms-auth:latest       backend/ms-auth
docker build -t smartlogix/bff:latest           backend/bff
docker build -t smartlogix/api-gateway:latest   backend/api-gateway
docker build --build-arg VITE_API_BASE=/api -t smartlogix/frontend:latest  frontend
```

- **Minikube**: `eval $(minikube docker-env)` antes de los `docker build`.
- **Kind**: `kind load docker-image smartlogix/<servicio>:latest`.

> El frontend se construye con `VITE_API_BASE=/api` para usar same-origin con el ingress (no hace falta CORS en k8s).

## 4. Despliegue: todo el stack

```bash
# 1) Crear el namespace y los recursos compartidos
kubectl apply -k infra/k8s/base

# 2) Crear el Secret de credenciales (NO está en el kustomize, se inyecta desde el .env)
kubectl -n smartlogix create secret generic smartlogix-secret \
  --from-env-file=.env \
  --dry-run=client -o yaml | kubectl apply -f -

# 3) Crear el Secret con las llaves RSA para JWT (ms-auth las monta en /app/keys)
kubectl -n smartlogix create secret generic smartlogix-keys \
  --from-file=private_key.pem=./private_key.pem \
  --from-file=public_key.pem=./public_key.pem \
  --dry-run=client -o yaml | kubectl apply -f -

# 4) Desplegar TODO via el orquestador raíz
kubectl apply -k infra/k8s
```

> El `configMapGenerator` del krakend.json vive en `backend/api-gateway/kustomization.yaml` (nivel del gateway), donde `krakend.json` es local — por eso `kubectl apply -k infra/k8s` funciona sin flags. Para desplegar solo el gateway: `kubectl apply -k backend/api-gateway`.

## 5. Despliegue: un solo MS

Cada microservicio es autocontenido:

```bash
kubectl apply -k backend/ms-inventory/k8s
kubectl apply -k backend/ms-order/k8s
# etc.
```

Requisito: el `Namespace`, `ConfigMap` y los Secrets deben existir previamente (`kubectl apply -k infra/k8s/base` + creación de los Secrets).

## 6. Hosts locales

Agregar al archivo hosts (Windows: `C:\Windows\System32\drivers\etc\hosts`, requiere permisos admin):

```
127.0.0.1 app.smartlogix.localhost
127.0.0.1 api.smartlogix.localhost
127.0.0.1 bff.smartlogix.localhost
```

> En Chrome/Edge `*.localhost` se resuelve a `127.0.0.1` automáticamente sin tocar el hosts (RFC 6761). En Firefox hay que activar `network.dns.localDomains` o agregar al hosts.

## 7. Verificación

```bash
# Estado de pods (esperado: 13 Running 1/1)
kubectl -n smartlogix get pods

# Servicios e ingress
kubectl -n smartlogix get svc,ingress

# Logs de un MS
kubectl -n smartlogix logs deploy/ms-inventory --tail 50

# Smoke test desde el host
curl --resolve app.smartlogix.localhost:80:127.0.0.1 http://app.smartlogix.localhost/

# Login + request autenticada
TOKEN=$(curl -sS -X POST --resolve app.smartlogix.localhost:80:127.0.0.1 \
  http://app.smartlogix.localhost/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@smartlogix.cl","password":"..."}' | jq -r .accessToken)

curl --resolve app.smartlogix.localhost:80:127.0.0.1 \
  -H "Authorization: Bearer $TOKEN" \
  http://app.smartlogix.localhost/api/inventory/products
```

### 7.1 Acceder a Swagger UI de un MS

Los MS no exponen su puerto al host por defecto. Usar port-forward temporal:

```bash
kubectl -n smartlogix port-forward svc/ms-order 8080:8080
# abrir http://localhost:8080/swagger-ui.html
```

## 8. Limpieza

```bash
kubectl delete -k infra/k8s
kubectl delete namespace smartlogix      # también borra los PVC y la data
```

## 9. Notas y workarounds

### 9.1 ConfigMap del krakend.json

El ConfigMap `krakend-config` se genera desde `backend/api-gateway/krakend.json` vía `configMapGenerator` en `backend/api-gateway/kustomization.yaml` (a nivel del gateway, no dentro de `k8s/`, para no violar el load-restrictor de kustomize). Cualquier cambio al JSON produce un nuevo hash y dispara rolling update del Deployment.

### 9.2 Tags de imagen y cache de containerd

Docker Desktop k8s usa **containerd** como runtime, que cachea imágenes por **digest** (no por tag). Re-taguear `:latest` localmente NO refresca los pods automáticamente.

**Solución**: el `infra/k8s/kustomization.yaml` tiene un bloque `images:` con `newTag` por servicio. Cambiar el tag (ej. `:v2`, `:eng-full`) y re-aplicar dispara un refresh real.

```yaml
images:
  - name: smartlogix/ms-inventory
    newTag: eng-full
```

Flujo recomendado tras un cambio de código:

```bash
docker compose --env-file .env build <svc>
docker tag smartlogix-<svc>:latest smartlogix/<svc>:vN
# actualizar newTag en infra/k8s/kustomization.yaml
kubectl apply -k infra/k8s
```

### 9.3 Bug Flyway en Spring Boot 4.0

ms-inventory, ms-order, ms-shipping y ms-user corren Spring Boot 4.0.6, donde la auto-config de Flyway no garantiza el orden previo a Hibernate. Con `ddl-auto=validate` y DB vacía, Hibernate falla con `Schema validation: missing table [bodegas]`.

**Workaround**: aplicar SQL manualmente vía psql tras el primer deploy:

```bash
cat backend/ms-inventory/src/main/resources/db/migration/V1__init_schema.sql | \
  kubectl -n smartlogix exec -i db-inventory-0 -- psql -U inventario -d inventario
```

`ms-auth` corre Spring Boot 3.5.0 y no tiene este problema (Flyway corre automáticamente).

### 9.4 Env var ordering (`$(POSTGRES_*_DB)`)

K8s sustituye `$(VAR)` en `env[].value` solo si `VAR` se declaró **antes** en la lista. Los deployments de los MS Spring están corregidos para declarar `POSTGRES_*_DB` antes de `SPRING_DATASOURCE_URL`.

### 9.5 Pendientes (probes)

Las probes de Spring Boot están comentadas en cada `deployment.yaml`. Activarlas tras agregar `spring-boot-starter-actuator` al `build.gradle` correspondiente.

### 9.6 Producción

- Gestionar Secrets con **SealedSecrets**, **External Secrets Operator** o **Vault**. Los archivos `*.example.yaml` son solo templates.
- TLS termination en el ingress (cert-manager + Let's Encrypt).
- Llaves RSA en un secret manager externo (no en el repo).
- HPA (HorizontalPodAutoscaler) por MS según métricas de CPU/memoria.
