# Análisis de calidad y cobertura con SonarQube (local)

Stack self-hosted de **SonarQube Community Build** para analizar los
microservicios de SmartLogix y verificar el requisito EFT de **≥60 % de
cobertura** de pruebas unitarias.

El análisis lo hace el plugin **`org.sonarqube`** ya configurado en el
`build.gradle` de cada microservicio (`ms-auth`, `ms-inventory`, `ms-order`,
`ms-shipping`, `ms-user`), que envía a SonarQube el reporte XML de JaCoCo.

---

## 0. Requisito previo del host (Docker Desktop / WSL2)

SonarQube usa Elasticsearch, que exige `vm.max_map_count >= 524288`. En
Windows con Docker Desktop (backend WSL2) configúralo así (PowerShell):

```powershell
wsl -d docker-desktop sysctl -w vm.max_map_count=524288
```

Para que persista entre reinicios, crea/edita `C:\Users\<tu-usuario>\.wslconfig`:

```ini
[wsl2]
kernelCommandLine = sysctl.vm.max_map_count=524288
```

(luego `wsl --shutdown` y reabrir Docker Desktop).

---

## 1. Levantar SonarQube

```bash
docker compose -f infra/sonarqube/docker-compose.yml up -d
```

Tarda ~1–2 min en arrancar. Verifica que esté listo:

- Dashboard: http://localhost:9000
- Login inicial: **admin / admin** (te pedirá cambiar la contraseña).

## 2. Generar un token de análisis

En el dashboard: **My Account → Security → Generate Tokens** (tipo *Global
Analysis Token*). Copia el token (`sqa_...`).

## 3. Ejecutar el análisis de un microservicio

Desde la carpeta del servicio (ej. `backend/ms-user`):

```bash
./gradlew test jacocoTestReport sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=TU_TOKEN
```

`test jacocoTestReport` regenera la cobertura y `sonar` la publica. Repite
para cada microservicio. Cada uno aparece como un proyecto propio en el
dashboard (`smartlogix-ms-*`).

## 4. Apagar el stack

```bash
docker compose -f infra/sonarqube/docker-compose.yml down        # conserva datos
docker compose -f infra/sonarqube/docker-compose.yml down -v     # borra datos
```

---

## Notas

- Los tests son unitarios (Mockito), **no requieren** la base de datos de la
  aplicación; el análisis corre sin levantar el stack de SmartLogix.
- Cobertura de línea actual (JaCoCo): ms-auth 69.5 %, ms-inventory 92.4 %,
  ms-order 93.0 %, ms-shipping 79.5 %, ms-user 88.2 % — todos sobre el 60 %.
- El `projectKey`/`projectName` de cada servicio está fijado en su
  `build.gradle`, bloque `sonar { ... }`.
