-- Seed de Movimientos de Stock
-- Refleja el ciclo de vida de los pedidos de ms-order (PED-2026-0001 .. 0004)
-- La cantidad y stock_minimo de la tabla `stock` ya esta en V2; aqui solo el historial.

-- Pedido 1 (ENVIADO): 5x ELE-4821-SL desde Muelle Central A
INSERT INTO movimientos_stock (id_stock, tipo, cantidad, referencia_pedido, created_at) VALUES
(1, 'ENTRADA',    150, 'COMPRA-PROV-LIDAR-001', '2026-06-01 08:00:00-04'),
(1, 'RESERVA',      5, 'PED-2026-0001',         '2026-06-04 09:30:00-04'),
(1, 'SALIDA',       5, 'PED-2026-0001',         '2026-06-05 09:00:00-04'),

-- Pedido 2 (EN_PREPARACION): 2x FAR-9904-SL desde Camara Fria B3
(2, 'ENTRADA',     15, 'COMPRA-PROV-VAC-001',   '2026-06-01 10:00:00-04'),
(2, 'RESERVA',      2, 'PED-2026-0002',         '2026-06-03 16:45:00-04'),

-- Pedido 3 (ENTREGADO): 120x ELE-2083-SL desde Muelle Central A
(3, 'ENTRADA',    500, 'COMPRA-PROV-IC-001',    '2026-06-01 09:00:00-04'),
(3, 'RESERVA',    120, 'PED-2026-0003',         '2026-06-02 11:30:00-04'),
(3, 'SALIDA',     120, 'PED-2026-0003',         '2026-06-03 09:15:00-04'),

-- Pedido 4 (APROBADO): pendiente de reserva (refleja stock 0 en bodega 3 para producto 4)
-- Sin movimientos aun. Se vera reflejado cuando el operador active la reserva.

-- Movimientos generales (compras de reabastecimiento)
(5, 'ENTRADA',     80, 'COMPRA-PROV-FRESH-001', '2026-06-02 06:00:00-04'),
(6, 'ENTRADA',    320, 'COMPRA-PROV-POL-001',   '2026-06-02 07:00:00-04');

-- Ajustar cantidades reales en stock para reflejar los movimientos (RESERVA y SALIDA)
-- Pedido 1: salida de 5 unidades en stock_id=1 (producto 1 / bodega 1)
UPDATE stock SET cantidad = cantidad - 5 WHERE id_stock = 1;

-- Pedido 2: 2 unidades reservadas en stock_id=2
UPDATE stock SET cant_reservada = cant_reservada + 2 WHERE id_stock = 2;

-- Pedido 3: salida de 120 unidades en stock_id=3
UPDATE stock SET cantidad = cantidad - 120 WHERE id_stock = 3;

-- Sincronizar secuencia
SELECT setval('movimientos_stock_id_movimiento_seq', (SELECT COALESCE(MAX(id_movimiento), 1) FROM movimientos_stock));
