-- Create teacher_subjects table
CREATE TABLE IF NOT EXISTS teacher_subjects (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    year_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    availability_status VARCHAR(50) NOT NULL,
    grade_level_from INT,
    grade_level_to INT,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

ALTER TABLE teacher_subjects
    ADD CONSTRAINT fk_teacher_subject_year FOREIGN KEY (year_id) REFERENCES academic_years (id);

ALTER TABLE teacher_subjects
    ADD CONSTRAINT fk_teacher_subject_teacher FOREIGN KEY (teacher_id) REFERENCES teachers (id);

ALTER TABLE teacher_subjects
    ADD CONSTRAINT fk_teacher_subject_subject FOREIGN KEY (subject_id) REFERENCES subjects (id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_teacher_subject_year_teacher_subject ON teacher_subjects (year_id, teacher_id, subject_id);

CREATE INDEX IF NOT EXISTS idx_teacher_subject_teacher ON teacher_subjects (teacher_id);
CREATE INDEX IF NOT EXISTS idx_teacher_subject_year ON teacher_subjects (year_id);
CREATE INDEX IF NOT EXISTS idx_teacher_subject_subject ON teacher_subjects (subject_id);
CREATE INDEX IF NOT EXISTS idx_teacher_subject_availability ON teacher_subjects (availability_status);
