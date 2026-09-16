import { PublicClientApplication, EventType } from '@azure/msal-browser';
import type { AuthenticationResult } from '@azure/msal-browser';
import { msalConfig } from './authConfig';

/** Instancia única de MSAL, compartida por la app y por el apiClient (interceptor). */
export const msalInstance = new PublicClientApplication(msalConfig);

/**
 * Inicializa MSAL y resuelve el redirect de vuelta desde Entra.
 * Debe llamarse (y esperarse) antes de renderizar la app.
 */
export async function initMsal(): Promise<void> {
  await msalInstance.initialize();

  // Procesa la respuesta cuando volvemos del login por redirect.
  // Si falla (p.ej. error AADSTS en la URL), lo registramos pero NO rompemos
  // el arranque: la app debe renderizar igual (login o dashboard).
  try {
    const result = await msalInstance.handleRedirectPromise();
    if (result?.account) {
      msalInstance.setActiveAccount(result.account);
    }
  } catch (err) {
    console.error('[MSAL] Error procesando el redirect de login:', err);
  }

  // Si ya había una sesión previa, activa la primera cuenta.
  if (!msalInstance.getActiveAccount() && msalInstance.getAllAccounts().length > 0) {
    msalInstance.setActiveAccount(msalInstance.getAllAccounts()[0]);
  }

  // Mantiene la cuenta activa sincronizada tras cada login exitoso.
  msalInstance.addEventCallback((event) => {
    if (event.eventType === EventType.LOGIN_SUCCESS && event.payload) {
      const payload = event.payload as AuthenticationResult;
      msalInstance.setActiveAccount(payload.account);
    }
  });
}
