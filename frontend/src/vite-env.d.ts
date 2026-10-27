/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Base URL del API (gateway/BFF). Por defecto '/api'. */
  readonly VITE_API_BASE?: string;
  /** API key de Google Gemini para el AI Hub. Si está ausente, el AI Hub usa el modo simulado. */
  readonly VITE_GEMINI_API_KEY?: string;
  /** DSN de GlitchTip para el error tracking. Si está ausente, Sentry.init queda en no-op. */
  readonly VITE_GLITCHTIP_DSN?: string;
  /** Tenant (Directory) ID de Microsoft Entra ID. */
  readonly VITE_AZURE_TENANT_ID: string;
  /** Application (client) ID de la App Registration. */
  readonly VITE_AZURE_CLIENT_ID: string;
  /** Redirect URI registrado como SPA en Entra. */
  readonly VITE_AZURE_REDIRECT_URI?: string;
  /** Scope de la API expuesta en Entra (api://<client-id>/access_as_user). */
  readonly VITE_AZURE_API_SCOPE?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
