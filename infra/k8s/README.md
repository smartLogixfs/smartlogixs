# SmartLogix en Kubernetes

Despliegue del stack en Kubernetes. **Desacoplado**: cada microservicio mantiene sus
propios manifests dentro de su carpeta (`backend/<ms>/k8s/` y `frontend/k8s/`).
Aqui en `infra/k8s/` solo viven los recursos compartidos (Namespace, ConfigMap,
Secret, Ingress) y el `kustomization.yaml` raiz que levanta todo el sistema.

## Estructura

```
infra/k8s/
├── base/                       # recursos compartidos (Namespace, ConfigMap, Secret template)
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secret.example.yaml     # template (sin credenciales reales)
│   └── kustomization.yaml
├── ingress.yaml                # reemplaza Traefik del docker-compose
├── kustomization.yaml          # orquestador raiz: levanta TODO
└── README.md

backend/
├── ms-inventory/k8s/   # Deployment + Service + DB StatefulSet + DB Service
├── ms-order/k8s/
├── ms-shipping/k8s/
├── ms-user/k8s/
├── ms-auth/k8s/
├── bff/k8s/            # Deployment + Service (sin DB)
└── api-gateway/k8s/    # Deployment + Service (sin DB)

frontend/k8s/           # Deployment + Service (Nginx)
```

Cada carpeta `k8s/` por servicio tiene su propio `kustomization.yaml`, por lo que se
puede desplegar de forma aislada (util para CI/CD o para reiniciar 1 solo MS).

## Pre-requisitos

- Un cluster k8s funcional (Docker Desktop, Minikube, Kind, k3d, EKS, GKE...).
- `kubectl` con contexto apuntando al cluster.
- Un Ingress Controller. Por defecto se usa `ingress-nginx`:
  ```bash
  kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml
  ```
- Imagenes Docker construidas y disponibles para el cluster con los tags
  `smartlogix/<servicio>:latest` (ver seccion "Build de imagenes" mas abajo).

## Despliegue: todo el stack

```bash
# 1) Crear el namespace y los recursos compartidos
kubectl apply -k infra/k8s/base

# 2) Crear el Secret de credenciales (NO esta en el kustomize, se inyecta desde el .env)
kubectl -n smartlogix create secret generic smartlogix-secret \
  --from-env-file=.env \
  --dry-run=client -o yaml | kubectl apply -f -

# 3) Crear el Secret de llaves JWT para ms-auth (NO esta en el kustomize)
kubectl -n smartlogix create secret generic ms-auth-keys \
  --from-file=private_key.pem \
  --from-file=public_key.pem \
  --dry-run=client -o yaml | kubectl apply -f -

# 4) Desplegar TODO via el orquestador raiz
kubectl apply -k infra/k8s
```

> El frontend hace fetch a `/api/...` relativo. El Ingress enruta `app.smartlogix.localhost/api/*` al `api-gateway` (same-origin, sin CORS). No es necesario rebuildear el frontend con `VITE_API_BASE` para k8s.

## Despliegue: un solo microservicio

Cada microservicio es autocontenido:

```bash
kubectl apply -k backend/ms-inventory/k8s
kubectl apply -k backend/ms-order/k8s
# etc.
```

Requisito: el `Namespace`, `ConfigMap` y `Secret` deben existir previamente
(`kubectl apply -k infra/k8s/base` + creacion del Secret).

## Build de imagenes

Construye localmente y, si usas Docker Desktop / Minikube, las imagenes estaran
disponibles para el cluster sin necesidad de push:

```bash
docker build -t smartlogix/ms-inventory:latest  backend/ms-inventory
docker build -t smartlogix/ms-order:latest      backend/ms-order
docker build -t smartlogix/ms-shipping:latest   backend/ms-shipping
docker build -t smartlogix/ms-user:latest       backend/ms-user
docker build -t smartlogix/ms-auth:latest       backend/ms-auth
docker build -t smartlogix/bff:latest           backend/bff
docker build -t smartlogix/api-gateway:latest   backend/api-gateway
docker build -t smartlogix/frontend:latest      frontend
```

Para Minikube: `eval $(minikube docker-env)` antes de los `docker build`.
Para Kind: `kind load docker-image smartlogix/<servicio>:latest`.

## Hosts locales

Agregar al archivo hosts (Windows: `C:\Windows\System32\drivers\etc\hosts`):

```
127.0.0.1 app.smartlogix.localhost
127.0.0.1 api.smartlogix.localhost
127.0.0.1 bff.smartlogix.localhost
```

## Verificacion

```bash
kubectl -n smartlogix get pods
kubectl -n smartlogix get svc
kubectl -n smartlogix get ingress
kubectl -n smartlogix logs deploy/ms-inventory
```

## Limpieza

```bash
kubectl delete -k infra/k8s
kubectl delete namespace smartlogix
```

## Notas

- Los Postgres usan `StatefulSet` + `PersistentVolumeClaim` (2 GiB por DB). Borrar
  el namespace tambien borra los PVC y los datos.
- Las probes de Spring Boot estan comentadas en cada `deployment.yaml` — activarlas
  tras agregar `spring-boot-starter-actuator` al `build.gradle` correspondiente.
- En produccion: gestionar el Secret con SealedSecrets, External Secrets Operator
  o Vault. `secret.example.yaml` es solo template.
