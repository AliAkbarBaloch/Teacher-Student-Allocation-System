-- SQL script to alter teacher_form_submissions table to use distinct fields
-- Run this AFTER truncating all tables (or on a fresh database)
-- Run in H2 console: http://localhost:8080/h2-console
-- JDBC URL: jdbc:h2:file:./data/allocdb
-- Username: sa
-- Password: (empty)

-- IMPORTANT: If you get "Column already exists" errors, comment out those lines
-- H2 doesn't support IF NOT EXISTS for ADD COLUMN, so run each statement individually
-- and skip any that give "Column already exists" errors

-- Drop the old submission_data column (comment out if column doesn't exist)
-- ALTER TABLE teacher_form_submissions DROP COLUMN submission_data;

-- Add new distinct columns for submission data
-- H2 requires each column to be added in a separate ALTER TABLE statement
ALTER TABLE teacher_form_submissions ADD COLUMN school_id BIGINT;
ALTER TABLE teacher_form_submissions ADD COLUMN employment_status VARCHAR(50);
ALTER TABLE teacher_form_submissions ADD COLUMN notes TEXT;
ALTER TABLE teacher_form_submissions ADD COLUMN subject_ids TEXT;
ALTER TABLE teacher_form_submissions ADD COLUMN internship_type_preference VARCHAR(50);
ALTER TABLE teacher_form_submissions ADD COLUMN internship_combinations TEXT;
ALTER TABLE teacher_form_submissions ADD COLUMN semester_availability TEXT;
ALTER TABLE teacher_form_submissions ADD COLUMN availability_options TEXT;

-- Ensure submitted_at can be NULL (if it's currently NOT NULL)
-- This may fail if column is already nullable, that's okay
ALTER TABLE teacher_form_submissions ALTER COLUMN submitted_at SET NULL;

