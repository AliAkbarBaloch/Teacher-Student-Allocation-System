-- Migration: Allow NULL values for submitted_at and submission_data
-- Description: When a form link is generated, these fields are NULL until the teacher submits the form

ALTER TABLE teacher_form_submissions 
    MODIFY COLUMN submitted_at TIMESTAMP NULL,
    MODIFY COLUMN submission_data TEXT NULL;

