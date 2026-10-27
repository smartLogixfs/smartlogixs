import type { Configuration, RedirectRequest } from '@azure/msal-browser';
import { LogLevel } from '@azure/msal-browser';

const tenantId = import.meta.env.VITE_AZURE_TENANT_ID;
const clientId = import.meta.env.VITE_AZURE_CLIENT_ID;

/**
 * Configuración base de MSAL contra Microsoft Entra ID (Azure AD).
 * El authority apunta al tenant; los tokens los emite el endpoint v2.0.
 */
export const msalConfig: Configuration = {
  auth: {
    clientId,
    authority: `https://login.microsoftonline.com/${tenantId}`,
    redirectUri: import.meta.env.VITE_AZURE_REDIRECT_URI ?? window.location.origin,
    postLogoutRedirectUri: import.meta.env.VITE_AZURE_REDIRECT_URI ?? window.location.origin,
  },
  cache: {
    cacheLocation: 'localStorage',
  },
  system: {
    loggerOptions: {
      loggerCallback: (level, message, containsPii) => {
        if (containsPii) return;
        if (level === LogLevel.Error) console.error(message);
      },
      logLevel: LogLevel.Error,
    },
  },
};

/**
 * Scope de NUESTRA API (definido en "Expose an API" de Entra).
 * IMPORTANTE: pedimos este scope y NO 'User.Read', para que el access token
 * tenga aud = la API de SmartLogix y el BFF pueda validarlo.
 */
export const apiScope =
  import.meta.env.VITE_AZURE_API_SCOPE ?? `api://${clientId}/access_as_user`;

/** Request usado en el login (redirect). Incluye openid/profile + el scope de la API. */
export const loginRequest: RedirectRequest = {
  scopes: ['openid', 'profile', apiScope],
};

/** Request usado para pedir el access token silencioso al llamar al backend. */
export const apiRequest = {
  scopes: [apiScope],
};
