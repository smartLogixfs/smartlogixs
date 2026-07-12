import { env } from "../config/env.js";

// Patrón Circuit Breaker para las llamadas del BFF a los microservicios.
// Evita golpear un servicio caído: tras N fallos consecutivos "abre" el circuito
// y falla rápido (fail-fast) durante una ventana; luego prueba en HALF_OPEN si el
// servicio se recuperó antes de volver a CLOSED.

export type CircuitState = "CLOSED" | "OPEN" | "HALF_OPEN";

export class CircuitOpenError extends Error {
  constructor(public readonly service: string) {
    super(`${service} no disponible (circuito abierto)`);
    this.name = "CircuitOpenError";
  }
}

export class CircuitBreaker {
  private state: CircuitState = "CLOSED";
  private failures = 0;
  private openUntil = 0;

  constructor(
    private readonly name: string,
    private readonly threshold = env.CB_FAILURE_THRESHOLD,
    private readonly resetTimeoutMs = env.CB_RESET_TIMEOUT_MS
  ) {}

  /** Estado actual; transiciona OPEN -> HALF_OPEN cuando vence la ventana. */
  getState(): CircuitState {
    if (this.state === "OPEN" && Date.now() >= this.openUntil) {
      this.state = "HALF_OPEN";
    }
    return this.state;
  }

  /** Ejecuta la operación protegida por el circuito. */
  async execute<T>(fn: () => Promise<T>): Promise<T> {
    if (this.getState() === "OPEN") {
      throw new CircuitOpenError(this.name); // fail-fast: no se llama al servicio
    }
    try {
      const result = await fn();
      this.onSuccess();
      return result;
    } catch (err) {
      this.onFailure();
      throw err;
    }
  }

  private onSuccess(): void {
    this.failures = 0;
    this.state = "CLOSED";
  }

  private onFailure(): void {
    this.failures++;
    // En HALF_OPEN un solo fallo reabre; en CLOSED se abre al llegar al umbral.
    if (this.state === "HALF_OPEN" || this.failures >= this.threshold) {
      this.state = "OPEN";
      this.openUntil = Date.now() + this.resetTimeoutMs;
    }
  }
}

// Un breaker por servicio destino (inventory, order, shipping, ...).
const registry = new Map<string, CircuitBreaker>();

export function getBreaker(service: string): CircuitBreaker {
  let breaker = registry.get(service);
  if (!breaker) {
    breaker = new CircuitBreaker(service);
    registry.set(service, breaker);
  }
  return breaker;
}
