-- V4: Add GeoCoordinates, Address, and OTP tables

-- Create geo_coordinates table
CREATE TABLE geo_coordinates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL
);

-- Create address table
CREATE TABLE address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    state VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    zip_code VARCHAR(20),
    geo_coordinates_id BIGINT,
    CONSTRAINT fk_address_geo_coordinates FOREIGN KEY (geo_coordinates_id) REFERENCES geo_coordinates(id)
);

-- Create OTP table
CREATE TABLE otp (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    code VARCHAR(6) NOT NULL,
    sent_to_number VARCHAR(15) NOT NULL
);

-- Create index for fast phone number lookups
CREATE INDEX idx_otp_phone ON otp(sent_to_number);

-- Create index for geo_coordinates in address
CREATE INDEX idx_address_geo_coords ON address(geo_coordinates_id);

