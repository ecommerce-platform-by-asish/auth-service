INSERT INTO users (id, email, password_hash, role, is_active, created_at, version)
VALUES
    ('a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d', 'admin@example.com', '$2a$10$uAzq6v0J.gxHPRy7EHhNxOk9PCaZYP9IrTOXztVkYNHhr4yDxN33W', 'ADMIN', true, CURRENT_TIMESTAMP, 1),
    ('b2c3d4e5-f6a7-4b6c-9d0e-1f2a3b4c5d6e', 'user@example.com', '$2a$10$uAzq6v0J.gxHPRy7EHhNxOk9PCaZYP9IrTOXztVkYNHhr4yDxN33W', 'USER', true, CURRENT_TIMESTAMP, 1);