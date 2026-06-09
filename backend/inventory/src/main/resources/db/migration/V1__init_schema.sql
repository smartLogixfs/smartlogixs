-- ============================================================
-- db-inventario · esquema inicial
-- Owner: ms-inventario (Spring Boot)
-- Patrón: Database per Service (sin FKs hacia otras DBs)
-- ============================================================

CREATE TABLE bodegas (
    id_bodega   BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(120) NOT NULL,
    ubicacion   VARCHAR(255),
    activo      BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE productos (
    id_producto  BIGSERIAL PRIMARY KEY,
    sku          VARCHAR(64)   NOT NULL UNIQUE,
    nombre       VARCHAR(200)  NOT NULL,
    descripcion  TEXT,
    precio       NUMERIC(12,2) NOT NULL CHECK (precio >= 0),
    activo       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE stock (
    id_stock        BIGSERIAL PRIMARY KEY,
    id_producto     BIGINT      NOT NULL REFERENCES productos(id_producto),
    id_bodega       BIGINT      NOT NULL REFERENCES bodegas(id_bodega),
    cantidad        INTEGER     NOT NULL DEFAULT 0 CHECK (cantidad >= 0),
    cant_reservada  INTEGER     NOT NULL DEFAULT 0 CHECK (cant_reservada >= 0),
    stock_minimo    INTEGER     NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    version         BIGINT      NOT NULL DEFAULT 0,           -- optimistic locking (@Version)
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_stock_producto_bodega UNIQUE (id_producto, id_bodega),
    CONSTRAINT ck_stock_reserva_lte_cant CHECK (cant_reservada <= cantidad)
);

CREATE TABLE movimientos_stock (
    id_movimiento      BIGSERIAL PRIMARY KEY,
    id_stock           BIGINT       NOT NULL REFERENCES stock(id_stock),
    tipo               VARCHAR(20)  NOT NULL
                         CHECK (tipo IN ('ENTRADA','SALIDA','RESERVA','LIBERACION')),
    cantidad           INTEGER      NOT NULL,
    referencia_pedido  VARCHAR(64),   -- id lógico de ms-pedido (no es FK)
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_mov_stock ON movimientos_stock(id_stock);
CREATE INDEX idx_mov_referencia_pedido ON movimientos_stock(referencia_pedido);
