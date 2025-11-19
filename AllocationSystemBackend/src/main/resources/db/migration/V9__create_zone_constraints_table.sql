-- V9__create_zone_constraints_table.sql
-- Migration to create zone_constraints table for managing zone-based internship type restrictions

CREATE TABLE zone_constraints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    zone_number INT NOT NULL,
    internship_type_id BIGINT NOT NULL,
    is_allowed BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_zone_constraint_internship_type 
        FOREIGN KEY (internship_type_id) REFERENCES internship_types(id) 
        ON DELETE CASCADE,
    
    -- Unique constraint on zone_number and internship_type_id combination
    CONSTRAINT uq_zone_internship_type UNIQUE (zone_number, internship_type_id),
    
    -- Check constraint for positive zone_number
    CONSTRAINT chk_zone_number_positive CHECK (zone_number > 0)
);

-- Indexes for performance optimization
CREATE INDEX idx_zone_constraints_zone_number ON zone_constraints(zone_number);
CREATE INDEX idx_zone_constraints_internship_type_id ON zone_constraints(internship_type_id);
CREATE INDEX idx_zone_constraints_is_allowed ON zone_constraints(is_allowed);
