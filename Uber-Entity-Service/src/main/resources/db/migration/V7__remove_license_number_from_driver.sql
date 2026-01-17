-- V8: Remove license_number field from driver table
-- This field is not used in any business logic

-- Drop the unique constraint first
ALTER TABLE driver DROP CONSTRAINT uc_driver_license_number;

-- Drop the license_number column
ALTER TABLE driver DROP COLUMN license_number;
