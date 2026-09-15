# Configuración de Microsoft Entra ID (Azure AD) para SmartLogix

Guía paso a paso para dejar el **IDaaS** listo antes de correr el frontend (MSAL) y el BFF (validación JWT).
Reutilizamos la App Registration que ya existe en `mi-app-msal`.

## Datos de tu tenant (ya existentes)

| Dato | Valor |
|---|---|
| **Tenant ID (Directory)** | `fd3a5752-a5c4-4102-a150-ab53dc03d8e7` |
| **Client ID (Application)** | `5370e922-a731-4bd3-9aa0-1e88e557d317` |
| **Redirect URI dev** | `http://localhost:5173` (frontend actual corre en `:3000`, ver paso 3) |

> Portal: https://entra.microsoft.com → **Identity → Applications → App registrations** → abre la app con el Client ID de arriba.
> (Equivalente en https://portal.azure.com → **Microsoft Entra ID → App registrations**.)

---

## Paso 1 — Exponer la API (scope `access_as_user`)

Esto hace que el **access token** que pide el frontend tenga `aud` = tu API, para que el **BFF pueda validarlo**.
Sin esto, si pides `User.Read`, recibes un token de **Microsoft Graph** que el BFF NO puede validar.

1. App registration → menú **Expose an API**.
2. **Application ID URI** → *Add* → acepta el sugerido `api://5370e922-a731-4bd3-9aa0-1e88e557d317` → **Save**.
3. **Add a scope**:
   - **Scope name:** `access_as_user`
   - **Who can consent:** `Admins and users`
   - **Admin consent display name:** `Acceder a la API de SmartLogix`
   - **Admin consent description:** `Permite a la app llamar a la API de SmartLogix en nombre del usuario`
   - **State:** `Enabled` → **Add scope**.
4. El scope completo queda: `api://5370e922-a731-4bd3-9aa0-1e88e557d317/access_as_user`.

---

## Paso 2 — Definir App Roles (para autorización por rol)

La pauta exige **leer roles y scopes** y **autorizar por rol**. Los App Roles llegan en el claim `roles` del token.

1. App registration → menú **App roles** → **Create app role** (repite por cada rol):

| Display name | Allowed member types | Value | Description |
|---|---|---|---|
| Inventory Read | Users/Groups | `Inventory.Read` | Leer inventario |
| Inventory Write | Users/Groups | `Inventory.Write` | Modificar inventario |
| Orders Read | Users/Groups | `Orders.Read` | Leer pedidos |
| Orders Write | Users/Groups | `Orders.Write` | Crear/checkout pedidos |
| Shipments Read | Users/Groups | `Shipments.Read` | Leer envíos |
| Shipments Write | Users/Groups | `Shipments.Write` | Modificar envíos |
| Users Read | Users/Groups | `Users.Read` | Leer usuarios |
| Users Write | Users/Groups | `Users.Write` | Modificar usuarios |

2. Deja todos en **Do you want to enable this app role? = Yes**.

### Asignar roles a los usuarios de prueba
1. Portal Entra → **Identity → Applications → Enterprise applications** → busca **SmartLogix** (misma app, vista de "Enterprise app").
2. **Users and groups → Add user/group**:
   - Usuario **admin** → asígnale TODOS los roles (`*.Read` + `*.Write`).
   - Usuario **normal** → solo los `*.Read`.

> Si "User assignment required?" está en *Yes* (Properties de la Enterprise app), solo los usuarios asignados podrán entrar. Para la demo puede quedar en *Yes* (más realista) o *No*.

---

## Paso 3 — Redirect URIs (SPA) y logout

1. App registration → menú **Authentication**.
2. En **Platform configurations**, si no existe, **Add a platform → Single-page application (SPA)**.
3. **Redirect URIs** (agrega los que uses):
   - `http://localhost:5173`
   - `http://localhost:3000`  ← el frontend actual arranca en `:3000` (`vite --port=3000`)
   - (luego) la URL pública de la Static Web App / Container App, ej. `https://smartlogix-web.azurestaticapps.net`
4. **Front-channel logout URL** (opcional): `http://localhost:3000`.
5. En **Implicit grant and hybrid flows**: NO marques nada (con SPA + PKCE no hace falta). **Save**.

---

## Paso 4 — (Opcional) forzar que el token sea v2 y traiga roles

- El endpoint v2.0 (`/oauth2/v2.0/token`) ya se usa por defecto con MSAL Browser. El **issuer** será:
  `https://login.microsoftonline.com/fd3a5752-a5c4-4102-a150-ab53dc03d8e7/v2.0`
- Los **App Roles** aparecen en el claim `roles` tanto del ID token como del access token.
- No necesitas editar el manifiesto salvo que quieras `accessTokenAcceptedVersion: 2` explícito (App registration → **Manifest**).

---

## Valores que usarán frontend y BFF (resumen)

Frontend (`frontend/.env`):
```
VITE_AZURE_TENANT_ID=fd3a5752-a5c4-4102-a150-ab53dc03d8e7
VITE_AZURE_CLIENT_ID=5370e922-a731-4bd3-9aa0-1e88e557d317
VITE_AZURE_REDIRECT_URI=http://localhost:3000
VITE_AZURE_API_SCOPE=api://5370e922-a731-4bd3-9aa0-1e88e557d317/access_as_user
VITE_API_BASE=/api
```

BFF (`backend/bff/.env`):
```
AZURE_TENANT_ID=fd3a5752-a5c4-4102-a150-ab53dc03d8e7
# Audience aceptada = client-id y/o el App ID URI
AZURE_API_AUDIENCE=5370e922-a731-4bd3-9aa0-1e88e557d317
AZURE_ISSUER=https://login.microsoftonline.com/fd3a5752-a5c4-4102-a150-ab53dc03d8e7/v2.0
AZURE_JWKS_URI=https://login.microsoftonline.com/fd3a5752-a5c4-4102-a150-ab53dc03d8e7/discovery/v2.0/keys
```

## Checklist de verificación
- [ ] Paso 1: scope `access_as_user` visible en *Expose an API*.
- [ ] Paso 2: 8 App Roles creados y asignados a los usuarios de prueba.
- [ ] Paso 3: redirect URIs `http://localhost:3000` (y `:5173`) como **SPA**.
- [ ] Al iniciar sesión en el frontend, el token decodificado en https://jwt.ms muestra `aud` = tu client-id, `iss` = `.../v2.0`, y `roles: [...]`.
