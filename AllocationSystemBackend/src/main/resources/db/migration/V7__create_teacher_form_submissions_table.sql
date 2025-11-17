-- Create TEACHER_FORM_SUBMISSION table
-- Stores raw web/Excel submissions from teachers for updating their preferences
-- and availability per academic year

CREATE TABLE teacher_form_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL,
    year_id BIGINT NOT NULL,
    form_token VARCHAR(255) NOT NULL UNIQUE,
    submitted_at TIMESTAMP NOT NULL,
    submission_data TEXT NOT NULL,
    is_processed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_teacher_form_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id),
    CONSTRAINT fk_teacher_form_year FOREIGN KEY (year_id) REFERENCES academic_years(id)
);

-- Create indexes for better query performance
CREATE INDEX idx_teacher_form_teacher_id ON teacher_form_submissions(teacher_id);
CREATE INDEX idx_teacher_form_year_id ON teacher_form_submissions(year_id);
CREATE INDEX idx_teacher_form_is_processed ON teacher_form_submissions(is_processed);
