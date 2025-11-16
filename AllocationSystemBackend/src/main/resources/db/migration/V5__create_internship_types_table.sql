-- Migration: Create INTERNSHIP_TYPE table
-- Description: Internship types that teachers can be assigned to

CREATE TABLE internship_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    internship_code VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    timing VARCHAR(100),
    period_type VARCHAR(50),
    semester VARCHAR(50),
    is_subject_specific BOOLEAN NOT NULL DEFAULT false,
    priority_order INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_internship_type_code ON internship_types(internship_code);
CREATE INDEX idx_internship_type_priority ON internship_types(priority_order);
CREATE INDEX idx_internship_type_subject_specific ON internship_types(is_subject_specific);

-- Insert sample internship types
INSERT INTO internship_types (internship_code, full_name, timing, period_type, semester, is_subject_specific, priority_order) VALUES
('SFP', 'Subject-Focused Practicum', 'Block', 'Continuous', 'Winter', true, 1),
('ZSP', 'Additional Subject Practicum', 'Block', 'Continuous', 'Summer', true, 2),
('PDP1', 'Professional Development Practicum 1', 'Semester-long', 'Weekly', 'Winter', false, 3),
('PDP2', 'Professional Development Practicum 2', 'Semester-long', 'Weekly', 'Summer', false, 4);
