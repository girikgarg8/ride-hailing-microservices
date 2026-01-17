-- V8: Add authentication fields to driver table
-- This allows drivers to authenticate directly without needing a separate User entity

ALTER TABLE driver
ADD COLUMN email VARCHAR(255) UNIQUE NOT NULL,
ADD COLUMN password VARCHAR(255) NOT NULL;

-- Add index for email lookups (authentication)
CREATE INDEX idx_driver_email ON driver(email);