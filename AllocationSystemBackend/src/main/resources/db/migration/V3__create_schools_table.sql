-- Migration: Create schools table
-- Version: V3
-- Description: Creates the SCHOOL table for managing participating schools in the allocation system

CREATE TABLE schools (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    school_name VARCHAR(255) NOT NULL UNIQUE,
    school_type VARCHAR(50) NOT NULL,
    zone_number INT NOT NULL,
    address VARCHAR(500),
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    distance_from_center DECIMAL(10, 2),
    transport_accessibility VARCHAR(255),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_school_type ON schools(school_type);
CREATE INDEX idx_zone_number ON schools(zone_number);
CREATE INDEX idx_is_active ON schools(is_active);

-- Insert sample data for testing
INSERT INTO schools (school_name, school_type, zone_number, address, latitude, longitude, distance_from_center, transport_accessibility, contact_email, contact_phone, is_active) 
VALUES 
    ('Passau Elementary School', 'PRIMARY', 1, 'Innstrasse 1, 94032 Passau', 48.5734053, 13.4579944, 2.5, 'Bus Line 1, 3', 'contact@passau-elementary.de', '+49841123456', TRUE),
    ('St. Nikola Middle School', 'MIDDLE', 1, 'Nikolastr. 10, 94032 Passau', 48.5772405, 13.4614867, 1.8, 'Bus Line 2, 4', 'info@nikola-middle.de', '+49841234567', TRUE),
    ('Gymnasium Leopoldinum', 'SECONDARY', 2, 'Leopoldstr. 14, 94032 Passau', 48.5747123, 13.4610789, 2.1, 'Bus Line 1, 2, 5', 'office@leopoldinum.de', '+49841345678', TRUE);
