-- V6: Add location fields to Booking and Passenger, and availability to Driver

-- ===========================
-- 1. Add new fields to Booking table
-- ===========================
ALTER TABLE booking
ADD COLUMN start_location_id BIGINT,
ADD COLUMN end_location_id BIGINT;

-- Add foreign key constraints for location fields
ALTER TABLE booking
ADD CONSTRAINT fk_booking_start_location FOREIGN KEY (start_location_id) REFERENCES geo_coordinates(id),
ADD CONSTRAINT fk_booking_end_location FOREIGN KEY (end_location_id) REFERENCES geo_coordinates(id);

-- Add index on driver_id for better query performance
CREATE INDEX idx_booking_driver ON booking(driver_id);

-- ===========================
-- 2. Add new fields to Passenger table
-- ===========================
ALTER TABLE passenger
ADD COLUMN rating DOUBLE,
ADD COLUMN last_known_location_id BIGINT,
ADD COLUMN home_id BIGINT;

-- Add foreign key constraints for GeoCoordinates
ALTER TABLE passenger
ADD CONSTRAINT fk_passenger_last_known_location FOREIGN KEY (last_known_location_id) REFERENCES geo_coordinates(id),
ADD CONSTRAINT fk_passenger_home FOREIGN KEY (home_id) REFERENCES geo_coordinates(id);

-- Add constraint to ensure passenger rating is between 0 and 5
ALTER TABLE passenger
ADD CONSTRAINT chk_passenger_rating CHECK (rating IS NULL OR (rating >= 0.00 AND rating <= 5.00));

-- Add index for passenger rating
CREATE INDEX idx_passenger_rating ON passenger(rating);

-- ===========================
-- 3. Add availability field to Driver table
-- ===========================
ALTER TABLE driver
ADD COLUMN is_available BOOLEAN NOT NULL DEFAULT FALSE;

-- Add index for is_available for efficient filtering of available drivers
CREATE INDEX idx_driver_availability ON driver(is_available);

