-- V3: Add DBConstant table for storing system configuration and constants

CREATE TABLE db_constant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    value TEXT
);

-- Create index for fast lookups by name
CREATE INDEX idx_db_constant_name ON db_constant(name);

