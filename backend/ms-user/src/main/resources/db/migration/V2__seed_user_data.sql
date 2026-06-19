-- Seed de Usuarios (clientes B2B / operadores logisticos)
-- El campo password aqui es para login interno del MS (no es el flujo principal — el flujo
-- de autenticacion vive en ms-auth con bcrypt). Estos hashes son placeholders SHA-256 dev.

INSERT INTO usuarios (id, email, nombre, password, telefono, direccion, region, comuna) VALUES
(1, 'admin@smartlogix.cl',    'Administrador Principal', 'placeholder-hash-1', '+56 9 5555 0001', 'Av. Apoquindo 4500, Of. 1201', 'Metropolitana', 'Las Condes'),
(2, 'cliente1@logixcorp.cl',  'Eduardo Silva Martinez',  'placeholder-hash-2', '+56 9 5555 0102', 'Calle Toesca 2890',             'Metropolitana', 'Santiago'),
(3, 'cliente2@nordtech.cl',   'Maria Jose Lopez',        'placeholder-hash-3', '+56 9 5555 0203', 'Av. Brasil 1234',               'Valparaiso',    'Valparaiso'),
(4, 'cliente3@bionord.cl',    'Carlos Andres Pena',      'placeholder-hash-4', '+56 9 5555 0304', 'Av. Vicuna Mackenna 5500',      'Metropolitana', 'La Florida'),
(5, 'operador1@smartlogix.cl','Ana Belen Torres',        'placeholder-hash-5', '+56 9 5555 0405', 'Camino a Melipilla 8200',       'Metropolitana', 'Maipu'),
(6, 'operador2@smartlogix.cl','Pablo Hernandez Soto',    'placeholder-hash-6', '+56 9 5555 0506', 'Av. Espana 1680',               'Valparaiso',    'Vina del Mar');

-- Sincronizar secuencia
SELECT setval('usuarios_id_seq', (SELECT COALESCE(MAX(id), 1) FROM usuarios));
