-- V10: Drop users table as we now use Driver/Passenger directly for authentication
-- This completes the refactoring to eliminate the redundant User entity

DROP TABLE IF EXISTS users;