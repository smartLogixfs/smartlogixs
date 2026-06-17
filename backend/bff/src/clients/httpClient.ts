import { env } from "../config/env.js";

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
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), env.HTTP_TIMEOUT_MS);
  const url = `${baseUrl}${path}`;
  try {
    const res = await fetch(url, {
      method,
      headers: { "content-type": "application/json", ...headers },
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal: controller.signal,
    });
    const text = await res.text();
    const data = text ? safeJson(text) : null;
    if (!res.ok) {
      throw new UpstreamError(`${service} respondió ${res.status}`, {
        status: res.status,
        body: data ?? text,
        service,
      });
    }
    return data;
  } catch (err: any) {
    if (err instanceof UpstreamError) throw err;
    if (err.name === "AbortError") {
      throw new UpstreamError(`${service} timeout (${env.HTTP_TIMEOUT_MS}ms)`, { status: 504, service });
    }
    throw new UpstreamError(`${service} inalcanzable: ${err.message}`, { status: 502, service });
  } finally {
    clearTimeout(timeout);
  }
}

function safeJson(text: string): any {
  try { return JSON.parse(text); } catch { return text; }
}
