-- Semilla de Datos de Envío (Transportistas, Envíos y Seguimiento Inicial)

INSERT INTO transportistas (id_transportista, nombre, rut, telefono_contacto, activo) VALUES
(1, 'FedLogix International', '76.120.340-K', '+56 2 2345 6789', true),
(2, 'DHS Express', '77.890.120-3', '+56 2 2890 1234', true),
(3, 'SmartFreight CL', '76.543.210-9', '+56 2 2765 4321', true),
(4, 'CargoNorte S.A.', '88.333.222-1', '+56 2 2999 8888', true);

INSERT INTO envios (id_envio, id_pedido, id_transportista, tracking_number, estado, direccion_destino, comuna, region, fecha_estimada) VALUES
(1, 1, 1, 'SL-589230', 'EN_RUTA', 'Buenos Aires, AR', 'Aconcagua', 'Valparaíso', '2026-06-08'),
(2, 2, 2, 'SL-702139', 'INCIDENCIA', 'Bogotá, CO', 'Bogota', 'Bogota', '2026-06-07'),
(3, 3, 3, 'SL-441209', 'ENTREGADO', 'Montevideo, UY', 'Montevideo', 'Montevideo', '2026-06-05'),
(4, 4, 4, 'SL-993214', 'CREADO', 'Santiago de Chile, CL', 'Santiago', 'Metropolitana', '2026-06-09');

INSERT INTO envio_seguimiento (id_seguimiento, id_envio, estado, ubicacion, comentario, created_at) VALUES
(1, 1, 'CREADO', 'Santiago de Chile, CL', 'Lote clasificado y cargado en el muelle de exportaciones general.', '2026-06-05 09:15:00-04'),
(2, 1, 'EN_RUTA', 'Paso Fronterizo Los Libertadores', 'Vehículo reporta cruce de aduana verificado de manera exitosa.', '2026-06-06 14:30:00-04'),
(3, 2, 'EN_RUTA', 'Guayaquil, EC', 'El transportista DHS Express continuó el tránsito después de reabastecimiento.', '2026-06-05 13:40:00-04'),
(4, 2, 'INCIDENCIA', 'Centro de Distribución Quito', 'Congestión vial debido a mantenimiento de carretera interprovincial.', '2026-06-06 11:20:00-04'),
(5, 3, 'CREADO', 'São Paulo, BR', 'Lote aprobado y firmado en muelle este de embarques.', '2026-06-03 10:00:00-04'),
(6, 3, 'ENTREGADO', 'Montevideo, UY', 'Mercancía recibida a conformidad por el operador de control local.', '2026-06-05 17:30:00-04'),
(7, 4, 'CREADO', 'Guayaquil, EC', 'Orden de carga confirmada. Vehículo asignado para pick-up.', '2026-06-06 15:00:00-04');

-- Sincronizar secuencias
SELECT setval('transportistas_id_transportista_seq', (SELECT COALESCE(MAX(id_transportista), 1) FROM transportistas));
SELECT setval('envios_id_envio_seq', (SELECT COALESCE(MAX(id_envio), 1) FROM envios));
SELECT setval('envio_seguimiento_id_seguimiento_seq', (SELECT COALESCE(MAX(id_seguimiento), 1) FROM envio_seguimiento));
