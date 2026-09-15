import type { AccountInfo } from '@azure/msal-browser';
import type { UserProfile } from '../types';

/** Claims relevantes que emite Entra ID en el ID/access token. */
export interface EntraClaims {
  name?: string;
  preferred_username?: string;
  email?: string;
  oid?: string;
  /** App Roles asignados al usuario (Paso 2 de la config de Entra). */
  roles?: string[];
  /** Scopes delegados concedidos, separados por espacio (solo en access token). */
  scp?: string;
  exp?: number;
}

/** Lee los claims del ID token cacheados en la cuenta MSAL. */
export function getClaims(account?: AccountInfo | null): EntraClaims {
  return (account?.idTokenClaims ?? {}) as EntraClaims;
}

/** Roles del usuario (claim `roles`). */
export function getRoles(account?: AccountInfo | null): string[] {
  return getClaims(account).roles ?? [];
}

/** Scopes del usuario (claim `scp`), como arreglo. */
export function getScopes(account?: AccountInfo | null): string[] {
  const scp = getClaims(account).scp;
  return scp ? scp.split(' ').filter(Boolean) : [];
}

/** ¿El usuario tiene el rol indicado? */
export function hasRole(account: AccountInfo | null | undefined, role: string): boolean {
  return getRoles(account).includes(role);
}

/** Construye el UserProfile de la UI a partir de los claims del token. */
export function toUserProfile(account?: AccountInfo | null): UserProfile {
  const c = getClaims(account);
  const roles = c.roles ?? [];
  return {
    name: c.name ?? account?.name ?? 'Usuario',
    email: c.preferred_username ?? c.email ?? account?.username ?? '',
    company: 'SmartLogix Partner',
    role: roles[0] ?? 'USER',
  };
}
