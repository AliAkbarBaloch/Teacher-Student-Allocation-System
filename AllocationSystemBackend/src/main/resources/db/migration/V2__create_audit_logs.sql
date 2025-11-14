-- Migration to create audit_logs table
-- V2__create_audit_logs.sql

CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    user_identifier VARCHAR(255) NOT NULL,
    event_timestamp TIMESTAMP NOT NULL,
    action VARCHAR(50) NOT NULL,
    target_entity VARCHAR(100) NOT NULL,
    target_record_id VARCHAR(100),
    previous_value TEXT,
    new_value TEXT,
    description VARCHAR(500),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for efficient querying
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_entity ON audit_logs(target_entity);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_timestamp ON audit_logs(event_timestamp);
CREATE INDEX idx_audit_user_identifier ON audit_logs(user_identifier);

-- Create composite index for common query patterns
CREATE INDEX idx_audit_entity_record ON audit_logs(target_entity, target_record_id);
