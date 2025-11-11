-- Initial schema for Allocation System (safe, minimal)
-- Creates a sample allocations table and a metadata table for demo purposes

CREATE TABLE allocations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Example seed row (can be removed)
INSERT INTO allocations (name, description) VALUES ('Initial allocation', 'Seed row created by V1 migration');
