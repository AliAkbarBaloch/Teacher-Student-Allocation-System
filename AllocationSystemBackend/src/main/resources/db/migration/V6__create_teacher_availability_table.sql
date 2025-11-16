-- Migration: Create TEACHER_AVAILABILITY table
-- Description: Records teacher availability per academic year and internship type with preference rankings

CREATE TABLE teacher_availability (
    availability_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL,
    year_id BIGINT NOT NULL,
    internship_type_id BIGINT NOT NULL,
    is_available BOOLEAN NOT NULL,
    preference_rank INTEGER,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_teacher_availability_teacher FOREIGN KEY (teacher_id) 
        REFERENCES teachers(id) ON DELETE CASCADE,
    CONSTRAINT fk_teacher_availability_year FOREIGN KEY (year_id) 
        REFERENCES academic_years(id) ON DELETE CASCADE,
    CONSTRAINT fk_teacher_availability_internship_type FOREIGN KEY (internship_type_id) 
        REFERENCES internship_types(id) ON DELETE RESTRICT,
    
    -- Unique constraint: one entry per teacher-year-internship combination
    CONSTRAINT uk_teacher_year_internship UNIQUE (teacher_id, year_id, internship_type_id),
    
    -- Check constraints
    CONSTRAINT chk_preference_rank_positive CHECK (preference_rank IS NULL OR preference_rank > 0)
);

-- Indexes for performance
CREATE INDEX idx_teacher_availability_teacher_id ON teacher_availability(teacher_id);
CREATE INDEX idx_teacher_availability_year_id ON teacher_availability(year_id);
CREATE INDEX idx_teacher_availability_internship_type ON teacher_availability(internship_type_id);
CREATE INDEX idx_teacher_availability_is_available ON teacher_availability(is_available);
CREATE INDEX idx_teacher_availability_composite ON teacher_availability(teacher_id, year_id);
