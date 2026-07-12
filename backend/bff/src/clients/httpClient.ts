import { env } from "../config/env.js";
import { getBreaker, CircuitOpenError } from "./circuitBreaker.js";

export class UpstreamError extends Error {
  status?: number;
  body?: any;
  service?: string;

  constructor(message: string, options: { status?: number; body?: any; service?: string } = {}) {
    super(message);
    this.name = "UpstreamError";
    this.status = options.status;
    this.body = options.body;
    this.service = options.service;
  }
}

interface RequestOptions {
  method?: string;
  body?: any;
  headers?: Record<string, string>;
}

export async function request(
  service: string,
  baseUrl: string,
  path: string,
  options: RequestOptions = {}
): Promise<any> {
  const { method = "GET", body, headers = {} } = options;
  const url = `${baseUrl}${path}`;
  const breaker = getBreaker(service);

  try {
    // El breaker envuelve la llamada: solo 5xx/red/timeout cuentan como fallo;
    // un 4xx significa que el servicio está sano (no debe abrir el circuito).
    const result = await breaker.execute(async () => {
      const controller = new AbortController();
      const timeout = setTimeout(() => controller.abort(), env.HTTP_TIMEOUT_MS);
      try {
        const res = await fetch(url, {
          method,
          headers: { "content-type": "application/json", ...headers },
          body: body !== undefined ? JSON.stringify(body) : undefined,
          signal: controller.signal,
        });
        const text = await res.text();
        const data = text ? safeJson(text) : null;
        if (res.status >= 500) {
          throw new UpstreamError(`${service} respondió ${res.status}`, {
            status: res.status,
            body: data ?? text,
            service,
          });
        }
        return { ok: res.ok, status: res.status, data, text };
      } finally {
        clearTimeout(timeout);
      }
    });

    if (!result.ok) {
      throw new UpstreamError(`${service} respondió ${result.status}`, {
        status: result.status,
        body: result.data ?? result.text,
        service,
      });
    }
    return result.data;
  } catch (err: any) {
    if (err instanceof CircuitOpenError) {
      throw new UpstreamError(`${service} no disponible (circuito abierto)`, { status: 503, service });
    }
    if (err instanceof UpstreamError) throw err;
    if (err.name === "AbortError") {
      throw new UpstreamError(`${service} timeout (${env.HTTP_TIMEOUT_MS}ms)`, { status: 504, service });
    }
    throw new UpstreamError(`${service} inalcanzable: ${err.message}`, { status: 502, service });
  }
}

function safeJson(text: string): any {
  try { return JSON.parse(text); } catch { return text; }
}
