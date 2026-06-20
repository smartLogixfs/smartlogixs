-- Semilla de Datos de Inventario (Bodegas, Productos y Stock Inicial)

INSERT INTO bodegas (id_bodega, nombre, ubicacion, activo) VALUES
(1, 'Muelle Central A', 'Santiago de Chile, CL - Sector Norte', true),
(2, 'Cámara Fría B3', 'Santiago de Chile, CL - Sector Sur (Frío)', true),
(3, 'Zona de Carga General O1', 'Santiago de Chile, CL - Sector Este', true),
(4, 'Terminal Expresa Sur', 'Valparaíso, CL - Muelle de Embarque', true);

INSERT INTO productos (id_producto, sku, nombre, descripcion, precio, activo) VALUES
(1, 'ELE-4821-SL', 'Sensores Láser LiDAR', 'Sensores LiDAR de alta precisión para navegación autónoma y robótica.', 150000.00, true),
(2, 'FAR-9904-SL', 'Vacunas Temperatura Controlada', 'Vacunas críticas que requieren mantener cadena de frío estricta (-20°C).', 2500000.00, true),
(3, 'ELE-2083-SL', 'Circuitos Integrados CMOS', 'Microcontroladores y circuitos lógicos para ensamble industrial.', 3500.00, true),
(4, 'AUT-7023-SL', 'Ejes para Chasis de Acero', 'Ejes de transmisión de acero reforzado para vehículos comerciales.', 85000.00, true),
(5, 'PER-5510-SL', 'Arándanos Orgánicos Premium', 'Arándanos de exportación seleccionados, almacenamiento en cámara fría.', 12000.00, true),
(6, 'GEN-3381-SL', 'Contenedores de Polímeros', 'Contenedores plásticos industriales de alta densidad.', 45000.00, true);

INSERT INTO stock (id_producto, id_bodega, cantidad, cant_reservada, stock_minimo, version) VALUES
(1, 1, 140, 0, 25, 0),
(2, 2, 12, 0, 30, 0),
(3, 1, 450, 0, 50, 0),
(4, 3, 0, 0, 15, 0),
(5, 2, 80, 0, 20, 0),
(6, 1, 320, 0, 40, 0);

-- Sincronizar secuencias
SELECT setval('bodegas_id_bodega_seq', (SELECT COALESCE(MAX(id_bodega), 1) FROM bodegas));
SELECT setval('productos_id_producto_seq', (SELECT COALESCE(MAX(id_producto), 1) FROM productos));
SELECT setval('stock_id_stock_seq', (SELECT COALESCE(MAX(id_stock), 1) FROM stock));
