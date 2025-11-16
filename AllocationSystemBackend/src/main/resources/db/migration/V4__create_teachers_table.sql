-- Migration: Create TEACHER table
-- Description: Teachers are linked to schools and will be assigned to subjects

CREATE TABLE teachers (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    is_part_time BOOLEAN NOT NULL DEFAULT false,
    employment_status VARCHAR(50) NOT NULL,
    usage_cycle VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_teacher_school FOREIGN KEY (school_id) 
        REFERENCES schools(id) ON DELETE RESTRICT,
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_phone_format CHECK (phone IS NULL OR phone ~* '^[+]?[(]?[0-9]{1,4}[)]?[-\s./0-9]*$')
);

-- Indexes for performance
CREATE INDEX idx_teacher_school_id ON teachers(school_id);
CREATE INDEX idx_teacher_email ON teachers(email);
CREATE INDEX idx_teacher_employment_status ON teachers(employment_status);
CREATE INDEX idx_teacher_is_active ON teachers(is_active);
CREATE INDEX idx_teacher_name ON teachers(last_name, first_name);

-- Comments for documentation
COMMENT ON TABLE teachers IS 'Teachers linked to schools for subject assignments';
COMMENT ON COLUMN teachers.school_id IS 'Reference to the school where teacher is employed';
COMMENT ON COLUMN teachers.is_part_time IS 'Indicates if teacher works part-time';
COMMENT ON COLUMN teachers.employment_status IS 'Current employment status (FULL_TIME, PART_TIME, ON_LEAVE, etc.)';
COMMENT ON COLUMN teachers.usage_cycle IS 'Indicates when teacher is available for assignments';
COMMENT ON COLUMN teachers.is_active IS 'Soft delete flag - false means deactivated';
