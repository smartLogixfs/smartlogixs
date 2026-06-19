-- Seed de Pedidos
-- Coherente con envios de ms-shipping (id_pedido 1-4) y productos de ms-inventory (id_producto 1-6)

INSERT INTO pedidos (id_pedido, codigo, tipo, estado, id_cliente, id_marketplace, subtotal, impuesto, total) VALUES
(1, 'PED-2026-0001', 'EXPRESS',  'ENVIADO',         'CLI-001', 'MKT-MELI',     750000.00, 142500.00,  892500.00),
(2, 'PED-2026-0002', 'ESTANDAR', 'EN_PREPARACION',  'CLI-002', 'MKT-FALABELLA', 5000000.00, 950000.00, 5950000.00),
(3, 'PED-2026-0003', 'EXPRESS',  'ENTREGADO',       'CLI-003', 'MKT-MELI',     420000.00,  79800.00,  499800.00),
(4, 'PED-2026-0004', 'ESTANDAR', 'APROBADO',        'CLI-001', 'MKT-RIPLEY',   135000.00,  25650.00,  160650.00);

-- Items del pedido (id_producto es lógico, refiere a productos de ms-inventario sin FK)
INSERT INTO pedido_items (id_pedido, id_producto, sku, cantidad, precio_unitario, subtotal) VALUES
-- Pedido 1: Sensores LiDAR (premium tech)
(1, 1, 'ELE-4821-SL', 5,  150000.00,  750000.00),
-- Pedido 2: Vacunas + arándanos (cadena de frío)
(2, 2, 'FAR-9904-SL', 2, 2500000.00, 5000000.00),
-- Pedido 3: Circuitos integrados (volumen)
(3, 3, 'ELE-2083-SL', 120, 3500.00,   420000.00),
-- Pedido 4: Mix automotriz + contenedores
(4, 4, 'AUT-7023-SL', 1,   85000.00,   85000.00),
(4, 6, 'GEN-3381-SL', 1,   45000.00,   45000.00),
(4, 5, 'PER-5510-SL', 1,    5000.00,    5000.00);

-- Historial de cambios de estado por pedido
INSERT INTO pedido_historial (id_pedido, estado_anterior, estado_nuevo, motivo, created_at) VALUES
(1, NULL,             'PENDIENTE',      'Pedido creado desde marketplace MELI',           '2026-06-04 08:15:00-04'),
(1, 'PENDIENTE',      'APROBADO',       'Stock disponible verificado',                     '2026-06-04 09:30:00-04'),
(1, 'APROBADO',       'EN_PREPARACION', 'Asignado a equipo de bodega Muelle Central A',    '2026-06-04 11:20:00-04'),
(1, 'EN_PREPARACION', 'ENVIADO',        'Lote cargado en transporte FedLogix',             '2026-06-05 09:00:00-04'),

(2, NULL,             'PENDIENTE',      'Pedido recibido desde Falabella',                 '2026-06-03 14:00:00-04'),
(2, 'PENDIENTE',      'APROBADO',       'Validacion de cadena de frio confirmada',         '2026-06-03 16:45:00-04'),
(2, 'APROBADO',       'EN_PREPARACION', 'En proceso en Camara Fria B3',                    '2026-06-04 08:00:00-04'),

(3, NULL,             'PENDIENTE',      'Pedido B2B grande desde MELI',                    '2026-06-02 10:00:00-04'),
(3, 'PENDIENTE',      'APROBADO',       'Credito del cliente aprobado',                    '2026-06-02 11:30:00-04'),
(3, 'APROBADO',       'EN_PREPARACION', 'Picking en Muelle Central A',                     '2026-06-02 14:00:00-04'),
(3, 'EN_PREPARACION', 'ENVIADO',        'Despachado via SmartFreight CL',                  '2026-06-03 09:15:00-04'),
(3, 'ENVIADO',        'ENTREGADO',      'Confirmacion de recepcion en Montevideo',         '2026-06-05 17:30:00-04'),

(4, NULL,             'PENDIENTE',      'Pedido desde Ripley',                             '2026-06-06 09:00:00-04'),
(4, 'PENDIENTE',      'APROBADO',       'Pago verificado, sin reserva de stock pendiente', '2026-06-06 12:00:00-04');

-- Sincronizar secuencias
SELECT setval('pedidos_id_pedido_seq',        (SELECT COALESCE(MAX(id_pedido), 1)    FROM pedidos));
SELECT setval('pedido_items_id_item_seq',     (SELECT COALESCE(MAX(id_item), 1)      FROM pedido_items));
SELECT setval('pedido_historial_id_historial_seq', (SELECT COALESCE(MAX(id_historial), 1) FROM pedido_historial));
