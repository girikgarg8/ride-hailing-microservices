-- V10: Fix schema discrepancies between database and JPA entities

ALTER TABLE driver 
MODIFY COLUMN is_available TINYINT(1) NOT NULL DEFAULT 0;