-- V2: Add Car and Color entities

-- Create color table
CREATE TABLE color (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL
);

-- Create car table
CREATE TABLE car (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    registration_number VARCHAR(255) UNIQUE NOT NULL,
    brand VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    car_type ENUM('XL','SEDAN','HATCHBACK','COMPACT_SUV','SUV') NOT NULL,
    color_id BIGINT,
    driver_id BIGINT,
    CONSTRAINT fk_car_color FOREIGN KEY (color_id) REFERENCES color(id),
    CONSTRAINT fk_car_driver FOREIGN KEY (driver_id) REFERENCES driver(id)
);

-- Note: Car owns the OneToOne relationship with Driver (car.driver_id FK only)
-- Driver uses @OneToOne(mappedBy="driver") - no FK needed on driver table

-- Insert default colors
INSERT INTO color (created_at, updated_at, name) VALUES
(NOW(), NOW(), 'White'),
(NOW(), NOW(), 'Black'),
(NOW(), NOW(), 'Silver'),
(NOW(), NOW(), 'Red'),
(NOW(), NOW(), 'Blue'),
(NOW(), NOW(), 'Grey'),
(NOW(), NOW(), 'Brown'),
(NOW(), NOW(), 'Green');

