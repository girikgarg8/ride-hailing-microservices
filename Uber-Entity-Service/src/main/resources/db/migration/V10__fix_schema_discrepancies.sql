-- V10: Fix schema discrepancies between database and JPA entities

-- Fix is_available column type (bit(1) -> boolean/tinyint(1))
ALTER TABLE driver 
MODIFY COLUMN is_available TINYINT(1) NOT NULL DEFAULT 0;

-- Add proper foreign key constraints for GeoCoordinates relationships
ALTER TABLE driver
ADD CONSTRAINT fk_driver_last_known_location 
    FOREIGN KEY (last_known_location_id) REFERENCES geo_coordinates(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_driver_home 
    FOREIGN KEY (home_id) REFERENCES geo_coordinates(id) ON DELETE SET NULL;

-- Add constraint for rating validation (0.00 to 5.00)
ALTER TABLE driver
ADD CONSTRAINT chk_driver_rating 
    CHECK (rating IS NULL OR (rating >= 0.00 AND rating <= 5.00));

-- Add index for driver approval status (used in queries)
CREATE INDEX idx_driver_approval_status ON driver(driver_approval_status);

-- Add index for availability (used in driver matching)
CREATE INDEX idx_driver_availability ON driver(is_available);

-- Add index for rating (used in driver ranking)
CREATE INDEX idx_driver_rating ON driver(rating);