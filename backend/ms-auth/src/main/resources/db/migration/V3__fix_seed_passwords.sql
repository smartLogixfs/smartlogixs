-- Corrige el hash de los usuarios sembrados en V2.
--
-- El hash original de V2 era un placeholder de ejemplo que no correspondia a
-- ninguna contrasena real, por lo que el login fallaba con 401. Aqui se fija el
-- hash bcrypt de 'admin12345', que es la contrasena que el frontend ofrece en
-- los botones "Demo Admin" / "Demo Logistico" (frontend/src/pages/Login.tsx).
--
-- bcrypt(admin12345) con coste 12, prefijo $2a (compatible con Spring Security).
UPDATE users
SET password_hash = '$2a$12$4ukXNbq99YbfN3B4uIR/jOxNhIupEg8y7rGGbSsJJ8UhwECZwB2Vm'
WHERE email IN ('admin@example.com', 'user@example.com');
