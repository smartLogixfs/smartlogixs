INSERT INTO users (name, email, password_hash, role, enabled)
VALUES
    ('Admin User', 'admin@example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiH5uL9fQ5YdExgkt1/3uzMdGII4XES', 'ADMIN', TRUE),
    ('Default User', 'user@example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiH5uL9fQ5YdExgkt1/3uzMdGII4XES', 'USER', TRUE)
ON CONFLICT (email) DO NOTHING;
