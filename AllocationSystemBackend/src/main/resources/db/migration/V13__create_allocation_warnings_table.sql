-- V13__create_allocation_warnings_table.sql
-- Migration to create allocation_warnings table for tracking unmet demands and constraint violations

CREATE TABLE allocation_warnings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    allocation_plan_id BIGINT NOT NULL,
    internship_type_id BIGINT,
    subject_id BIGINT,
    school_type VARCHAR(50),
    shortage INT,
    warning_type VARCHAR(50),
    message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_allocation_warning_plan 
        FOREIGN KEY (allocation_plan_id) REFERENCES allocation_plans(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_allocation_warning_internship_type 
        FOREIGN KEY (internship_type_id) REFERENCES internship_types(id) 
        ON DELETE SET NULL,
    
    CONSTRAINT fk_allocation_warning_subject 
        FOREIGN KEY (subject_id) REFERENCES subjects(id) 
        ON DELETE SET NULL,
    
    -- Check constraint for non-negative shortage
    CONSTRAINT chk_allocation_warning_shortage_non_negative 
        CHECK (shortage IS NULL OR shortage >= 0)
);

-- Indexes for performance optimization
CREATE INDEX idx_allocation_warning_plan_id ON allocation_warnings(allocation_plan_id);
CREATE INDEX idx_allocation_warning_internship_type_id ON allocation_warnings(internship_type_id);
CREATE INDEX idx_allocation_warning_subject_id ON allocation_warnings(subject_id);
CREATE INDEX idx_allocation_warning_warning_type ON allocation_warnings(warning_type);
CREATE INDEX idx_allocation_warning_created_at ON allocation_warnings(created_at);

-- Composite index for common query patterns (finding warnings for a specific plan and type)
CREATE INDEX idx_allocation_warning_plan_type ON allocation_warnings(allocation_plan_id, warning_type);

-- Add comment for table documentation
ALTER TABLE allocation_warnings COMMENT = 'Stores allocation warnings for unmet demands, constraint violations, and other allocation issues';

