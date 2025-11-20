-- Migration: Create SUBJECT table
-- Description: Subjects that can be assigned to teachers for internships

CREATE TABLE subjects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_code VARCHAR(50) NOT NULL UNIQUE,
    subject_title VARCHAR(255) NOT NULL,
    subject_category_id BIGINT NOT NULL,
    school_type VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subject_category FOREIGN KEY (subject_category_id) REFERENCES subject_category(id)
);

-- Indexes for performance
CREATE INDEX idx_subject_code ON subjects(subject_code);
CREATE INDEX idx_subject_category_id ON subjects(subject_category_id);
CREATE INDEX idx_subject_school_type ON subjects(school_type);
CREATE INDEX idx_subject_is_active ON subjects(is_active);

