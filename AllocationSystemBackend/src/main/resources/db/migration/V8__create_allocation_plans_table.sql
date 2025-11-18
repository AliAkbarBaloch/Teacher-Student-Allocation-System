-- Migration to create allocation_plans table
-- V8__create_allocation_plans_table.sql

CREATE TABLE allocation_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    year_id BIGINT NOT NULL,
    plan_name VARCHAR(255) NOT NULL,
    plan_version VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT,
    
    -- Foreign key constraints
    CONSTRAINT fk_allocation_plan_year FOREIGN KEY (year_id) REFERENCES academic_years(id),
    CONSTRAINT fk_allocation_plan_user FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    
    -- Unique constraint for (year_id, plan_version)
    CONSTRAINT uk_allocation_plan_year_version UNIQUE (year_id, plan_version)
);

-- Indexes for efficient querying
CREATE INDEX idx_allocation_plan_year ON allocation_plans(year_id);
CREATE INDEX idx_allocation_plan_status ON allocation_plans(status);
CREATE INDEX idx_allocation_plan_is_current ON allocation_plans(is_current);
CREATE INDEX idx_allocation_plan_created_by ON allocation_plans(created_by_user_id);

-- Composite index for common query patterns
CREATE INDEX idx_allocation_plan_year_status ON allocation_plans(year_id, status);
CREATE INDEX idx_allocation_plan_year_current ON allocation_plans(year_id, is_current);

-- Add comment for table documentation
ALTER TABLE allocation_plans COMMENT = 'Stores allocation plan instances with versioning and status workflow';
