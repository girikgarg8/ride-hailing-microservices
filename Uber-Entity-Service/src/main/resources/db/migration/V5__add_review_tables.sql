-- V5: Add Review entities with JOINED inheritance strategy and Driver fields

-- ===========================
-- 1. Add new fields to Driver table
-- ===========================
ALTER TABLE driver
ADD COLUMN driver_approval_status ENUM('APPROVED', 'DENIED', 'PENDING'),
ADD COLUMN last_known_location_id BIGINT,
ADD COLUMN home_id BIGINT,
ADD COLUMN active_city VARCHAR(255),
ADD COLUMN rating DOUBLE;

-- Add foreign key constraints for GeoCoordinates
ALTER TABLE driver
ADD CONSTRAINT fk_driver_last_known_location FOREIGN KEY (last_known_location_id) REFERENCES geo_coordinates(id),
ADD CONSTRAINT fk_driver_home FOREIGN KEY (home_id) REFERENCES geo_coordinates(id);

-- Add indexes for driver fields
CREATE INDEX idx_driver_approval_status ON driver(driver_approval_status);
CREATE INDEX idx_driver_rating ON driver(rating);

-- Add constraint to ensure driver rating is between 0 and 5
ALTER TABLE driver
ADD CONSTRAINT chk_driver_rating CHECK (rating IS NULL OR (rating >= 0.00 AND rating <= 5.00));

-- ===========================
-- 2. Create Review tables
-- ===========================

-- Create parent table: booking_review (stores common fields)
CREATE TABLE booking_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    rating DOUBLE,
    booking_id BIGINT NOT NULL,
    CONSTRAINT fk_review_booking FOREIGN KEY (booking_id) REFERENCES booking(id),
    CONSTRAINT uk_review_booking UNIQUE (booking_id)  -- One review per booking
);

-- Create child table: passenger_review (stores PassengerReview-specific fields)
-- Uses JOINED strategy: shares the same ID as parent via FK
CREATE TABLE passenger_review (
    id BIGINT PRIMARY KEY,
    passenger_review_count VARCHAR(255) NOT NULL,
    passenger_rating VARCHAR(255) NOT NULL,
    CONSTRAINT fk_passenger_review_booking_review FOREIGN KEY (id) REFERENCES booking_review(id)
);

-- Create indexes for performance
CREATE INDEX idx_review_booking ON booking_review(booking_id);
CREATE INDEX idx_review_rating ON booking_review(rating);

-- Add constraint to ensure review rating is between 0 and 5
ALTER TABLE booking_review
ADD CONSTRAINT chk_review_rating CHECK (rating IS NULL OR (rating >= 0.00 AND rating <= 5.00));

