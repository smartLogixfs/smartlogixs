import {
  Product,
  ShipmentDto,
  CreateShipmentPayload,
  UpdateShipmentStatusPayload,
} from '../types';
import { InteractionRequiredAuthError } from '@azure/msal-browser';
import { msalInstance } from '../msal';
import { apiRequest } from '../authConfig';

const API_BASE = import.meta.env.VITE_API_BASE ?? '/api';

/** Extrae un mensaje legible de cualquier valor capturado en un catch. */
export function getErrorMessage(err: unknown): string {
  if (err instanceof Error) return err.message;
  if (typeof err === 'string') return err;
  return 'Error inesperado';
}

/**
 * Obtiene un access token de Entra para llamar a NUESTRA API (scope access_as_user).
 * Actúa como "interceptor": adjunta el Bearer en cada petición del apiClient.
 * Si el token silencioso falla por interacción requerida, dispara login por redirect.
 */
async function getAccessToken(): Promise<string | null> {
  const account = msalInstance.getActiveAccount() ?? msalInstance.getAllAccounts()[0];
  if (!account) return null;

  try {
    const result = await msalInstance.acquireTokenSilent({ ...apiRequest, account });
    return result.accessToken;
  } catch (err) {
    // Ojo: NO disparamos acquireTokenRedirect aquí para evitar bucles de
    // redirect (sobre todo mientras no hay backend). Registramos y devolvemos
    // null; la petición fallará y la UI puede pedir re-login si hace falta.
    if (err instanceof InteractionRequiredAuthError) {
      console.warn('[MSAL] Se requiere interacción para el token de la API. Vuelve a iniciar sesión si es necesario.');
    } else {
      console.error('[MSAL] No se pudo adquirir el access token:', err);
    }
    return null;
  }
}

interface ApiErrorBody {
  detail?: string;
  message?: string;
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = await getAccessToken();
  const headers = new Headers(options.headers || {});

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });

  if (!response.ok) {
    let errorDetail = 'Error en la petición';
    if (response.status === 401) errorDetail = 'No autenticado (401). Vuelve a iniciar sesión.';
    else if (response.status === 403) errorDetail = 'No autorizado (403). No tienes el rol requerido.';
    try {
      const errBody = (await response.json()) as ApiErrorBody;
      errorDetail = errBody.detail || errBody.message || errorDetail;
    } catch {
      // ignore: cuerpo no-JSON
    }
    throw new Error(errorDetail);
  }

  const text = await response.text();
  return (text ? JSON.parse(text) : null) as T;
}

export const api = {
  async getProducts(): Promise<Product[]> {
    return request<Product[]>('/inventory/products-with-stock');
  },

  async createProduct(product: Partial<Product>): Promise<Product> {
    return request<Product>('/inventory/products', {
      method: 'POST',
      body: JSON.stringify(product),
    });
  },

  async deleteProduct(id: string): Promise<void> {
    await request<void>(`/inventory/products/${id}`, {
      method: 'PATCH',
      body: JSON.stringify({ active: false }),
    });
  },

  // Map to ms-inventory/stock/in or out
  async adjustProductStock(
    productId: number | string,
    warehouseId: number | string,
    quantity: number,
    type: 'ENTRADA' | 'SALIDA'
  ): Promise<ShipmentDto | unknown> {
    const path = type === 'ENTRADA' ? '/inventory/stock/in' : '/inventory/stock/out';
    return request<unknown>(path, {
      method: 'POST',
      body: JSON.stringify({
        productId: Number(productId),
        warehouseId: Number(warehouseId),
        quantity: Math.abs(quantity),
        orderReference: 'Ajuste Manual UI',
      }),
    });
  },

  async getShipments(): Promise<ShipmentDto[]> {
    return request<ShipmentDto[]>('/shipments');
  },

  async createShipment(shipment: CreateShipmentPayload): Promise<ShipmentDto> {
    return request<ShipmentDto>('/shipments', {
      method: 'POST',
      body: JSON.stringify(shipment),
    });
  },

  async updateShipmentStatus(
    id: string,
    payload: UpdateShipmentStatusPayload
  ): Promise<ShipmentDto> {
    return request<ShipmentDto>(`/shipments/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify(payload),
    });
  },

  async getDashboard(): Promise<Record<string, unknown>> {
    return request<Record<string, unknown>>('/dashboard');
  },
};
