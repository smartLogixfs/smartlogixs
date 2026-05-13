-- ============================================================
-- db-envio · esquema inicial
-- Owner: ms-envio (Spring Boot)
-- Patrón: Database per Service (sin FKs hacia otras DBs)
-- ============================================================

CREATE TABLE transportistas (
    id_transportista   BIGSERIAL PRIMARY KEY,
    nombre             VARCHAR(120) NOT NULL,
    rut                VARCHAR(20) UNIQUE,
    telefono_contacto  VARCHAR(40),
    activo             BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE envios (
    id_envio           BIGSERIAL PRIMARY KEY,
    id_pedido          BIGINT       NOT NULL,                                  -- id lógico (ms-pedido), NO es FK
    id_transportista   BIGINT       REFERENCES transportistas(id_transportista),
    tracking_number    VARCHAR(60)  UNIQUE,
    estado             VARCHAR(20)  NOT NULL DEFAULT 'CREADO'
                         CHECK (estado IN ('CREADO','ASIGNADO','EN_RUTA','ENTREGADO','INCIDENCIA')),
    direccion_destino  VARCHAR(255) NOT NULL,
    comuna             VARCHAR(120),
    region             VARCHAR(120),
    fecha_estimada     DATE,
    fecha_entrega      TIMESTAMPTZ,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_envios_pedido ON envios(id_pedido);
CREATE INDEX idx_envios_estado ON envios(estado);

CREATE TABLE envio_seguimiento (
    id_seguimiento  BIGSERIAL PRIMARY KEY,
    id_envio        BIGINT       NOT NULL REFERENCES envios(id_envio) ON DELETE CASCADE,
    estado          VARCHAR(20)  NOT NULL
                      CHECK (estado IN ('CREADO','ASIGNADO','EN_RUTA','ENTREGADO','INCIDENCIA')),
    ubicacion       VARCHAR(255),
    comentario      VARCHAR(500),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_seguimiento_envio ON envio_seguimiento(id_envio);
