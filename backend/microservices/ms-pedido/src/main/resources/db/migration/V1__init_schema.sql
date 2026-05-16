-- ============================================================
-- db-pedido · esquema inicial
-- Owner: ms-pedido (Spring Boot)
-- Patrón: Database per Service (sin FKs hacia otras DBs)
-- ============================================================

CREATE TABLE pedidos (
    id_pedido       BIGSERIAL PRIMARY KEY,
    codigo          VARCHAR(40)   NOT NULL UNIQUE,
    tipo            VARCHAR(20)   NOT NULL
                      CHECK (tipo IN ('ESTANDAR','EXPRESS')),
    estado          VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE'
                      CHECK (estado IN ('PENDIENTE','APROBADO','EN_PREPARACION',
                                        'ENVIADO','ENTREGADO','RECHAZADO','CANCELADO')),
    id_cliente      VARCHAR(64)   NOT NULL,
    id_marketplace  VARCHAR(64),
    subtotal        NUMERIC(12,2) NOT NULL DEFAULT 0,
    impuesto        NUMERIC(12,2) NOT NULL DEFAULT 0,
    total           NUMERIC(12,2) NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_pedidos_estado ON pedidos(estado);
CREATE INDEX idx_pedidos_cliente ON pedidos(id_cliente);

CREATE TABLE pedido_items (
    id_item          BIGSERIAL PRIMARY KEY,
    id_pedido        BIGINT       NOT NULL REFERENCES pedidos(id_pedido) ON DELETE CASCADE,
    id_producto      BIGINT       NOT NULL,             -- id lógico (ms-inventario), NO es FK
    sku              VARCHAR(64)  NOT NULL,
    cantidad         INTEGER      NOT NULL CHECK (cantidad > 0),
    precio_unitario  NUMERIC(12,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal         NUMERIC(12,2) NOT NULL CHECK (subtotal >= 0)
);
CREATE INDEX idx_items_pedido ON pedido_items(id_pedido);

CREATE TABLE pedido_historial (
    id_historial     BIGSERIAL PRIMARY KEY,
    id_pedido        BIGINT      NOT NULL REFERENCES pedidos(id_pedido) ON DELETE CASCADE,
    estado_anterior  VARCHAR(20)
                       CHECK (estado_anterior IS NULL OR estado_anterior IN (
                              'PENDIENTE','APROBADO','EN_PREPARACION',
                              'ENVIADO','ENTREGADO','RECHAZADO','CANCELADO')),
    estado_nuevo     VARCHAR(20) NOT NULL
                       CHECK (estado_nuevo IN (
                              'PENDIENTE','APROBADO','EN_PREPARACION',
                              'ENVIADO','ENTREGADO','RECHAZADO','CANCELADO')),
    motivo           VARCHAR(255),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_hist_pedido ON pedido_historial(id_pedido);
