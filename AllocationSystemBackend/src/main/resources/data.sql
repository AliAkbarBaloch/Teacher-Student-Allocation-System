INSERT INTO ACADEMIC_YEARS (id, year_name, total_credit_hours, elementary_school_hours, middle_school_hours, budget_announcement_date, allocation_deadline, is_locked, created_at, updated_at)
VALUES (1, '2025/2026', 210, 169, 41, '2025-05-15', '2025-06-30', FALSE, NOW(), NOW());

-- 2. INTERNSHIP_TYPE
-- Defining the 4 specific types with their timing and subject strictness.
INSERT INTO INTERNSHIP_TYPES (id, internship_code, full_name, timing, period_type, semester, is_subject_specific, priority_order, created_at, updated_at)
VALUES
    (1, 'PDP 1', 'Pedagogical-didactic block internship I', 'Autumn', 'Block', '1st', FALSE, 3, NOW(), NOW()),
    (2, 'PDP 2', 'Pedagogical-didactic block internship II', 'Spring', 'Block', '2nd', FALSE, 3, NOW(), NOW()),
    (3, 'ZSP', 'Additional study-accompanying internship', 'Winter (Wed)', 'Wednesday', '1st', TRUE, 2, NOW(), NOW()),
    (4, 'SFP', 'Study-accompanying subject-didactic internship', 'Summer (Wed)', 'Wednesday', '2nd', TRUE, 1, NOW(), NOW());


-- -- 3. INTERNSHIP_COMBINATION_RULE
-- -- Defining valid pairs for a teacher (e.g., PDP 1 + PDP 2 is valid).
-- INSERT INTO INTERNSHIP_COMBINATION_RULES (id, internship_type_1_id, internship_type_2_id, is_valid_combination)
-- VALUES
--     (1, 1, 2, TRUE), -- PDP 1 + PDP 2
--     (2, 1, 4, TRUE), -- PDP 1 + SFP
--     (3, 1, 3, TRUE), -- PDP 1 + ZSP
--     (4, 2, 4, TRUE), -- PDP 2 + SFP
--     (5, 2, 3, TRUE), -- PDP 2 + ZSP
--     (6, 4, 3, TRUE); -- SFP + ZSP


-- 4. ZONE_CONSTRAINT
-- Zone 1 = Close (Wednesday ok), Zone 3 = Far (Block only).
INSERT INTO ZONE_CONSTRAINTS (id, zone_number, internship_type_id, is_allowed, description, created_at)
VALUES
    (1, 1, 3, TRUE, 'Zone 1 suitable for ZSP (Wednesday)', NOW()),
    (2, 1, 4, TRUE, 'Zone 1 suitable for SFP (Wednesday)', NOW()),
    (3, 2, 1, TRUE, 'Zone 2 suitable for Blocks', NOW()),
    (4, 2, 3, TRUE, 'Zone 2 suitable for Wednesday', NOW()),
    (5, 3, 1, TRUE, 'Zone 3 suitable for PDP 1 (Block)', NOW()),
    (6, 3, 2, TRUE, 'Zone 3 suitable for PDP 2 (Block)', NOW());


-- 5. SUBJECT_CATEGORY & SUBJECT
-- Mapping abbreviations to full names.
INSERT INTO SUBJECT_CATEGORY (id, category_title, created_at, updated_at) VALUES (1, 'Core Subjects', NOW(), NOW());
INSERT INTO SUBJECT_CATEGORY (id, category_title, created_at, updated_at) VALUES (2, 'Social Sciences', NOW(), NOW());

INSERT INTO SUBJECTS (id, subject_code, subject_title, subject_category_id, school_type, is_active, created_at, updated_at)
VALUES
    (1, 'D', 'German', 1, 'Primary', TRUE, NOW(), NOW()),
    (2, 'MA', 'Mathematics', 1, 'Primary', TRUE, NOW(), NOW()),
    (3, 'E', 'English', 1, 'Middle', TRUE, NOW(), NOW()), -- Bottleneck subject
    (4, 'HSU', 'Home and Subject Matter Lessons', 2, 'Primary', TRUE, NOW(), NOW()),
    (5, 'GSE', 'History/Social Studies/Geography', 2, 'Middle', TRUE, NOW(), NOW());


-- 6. SCHOOL
-- Zone 1 (Passau City), Zone 2 (Suburbs), Zone 3 (Rural).
INSERT INTO SCHOOLS (id, school_name, school_type, zone_number, address, distance_from_center, transport_accessibility, is_active, created_at, updated_at)
VALUES
    (1, 'Grundschule Passau-Innstadt', 'Primary', 1, 'Innstadt kellerweg, Passau', 2.5, '4a', TRUE, NOW(), NOW()),
    (2, 'Mittelschule Salzweg', 'Middle', 2, 'Salzweg Hauptstr', 15.0, '4a', TRUE, NOW(), NOW()),
    (3, 'Grundschule Freyung', 'Primary', 3, 'Freyung Stadtplatz', 45.0, 'None', TRUE, NOW(), NOW());

-- 7. TEACHER
-- Creating supervisors. Note usage_cycle for HSU rotation.
INSERT INTO TEACHERS (id, school_id, first_name, last_name, email, is_part_time, employment_status, usage_cycle, is_active, created_at, updated_at)
VALUES
    (1, 1, 'Hans', 'Müller', 'hans.mueller@gs-passau.de', FALSE, 'FULL_TIME', 'FULL_YEAR', TRUE, NOW(), NOW()),
    (2, 1, 'Anna', 'Schmidt', 'anna.schmidt@gs-passau.de', TRUE, 'FULL_TIME', 'FULL_YEAR', TRUE, NOW(), NOW()), -- Part-time, usually Wednesday only
    (3, 2, 'Peter', 'Weber', 'peter.weber@ms-salzweg.de', FALSE, 'FULL_TIME', 'FULL_YEAR', TRUE, NOW(), NOW()),
    (4, 3, 'Julia', 'Wagner', 'julia.wagner@gs-freyung.de', FALSE, 'FULL_TIME', 'FULL_YEAR', TRUE, NOW(), NOW());

-- 8. TEACHER_SUBJECT
-- Linking teachers to what they are allowed to supervise.
INSERT INTO TEACHER_SUBJECTS (id, year_id, teacher_id, subject_id, availability_status, created_at, updated_at)
VALUES
    (1, 1, 1, 1, 'Available', NOW(), NOW()), -- Hans teaches German
    (2, 1, 1, 2, 'Available', NOW(), NOW()), -- Hans teaches Math
    (3, 1, 2, 4, 'Available', NOW(), NOW()), -- Anna teaches HSU
    (4, 1, 3, 3, 'Available', NOW(), NOW()), -- Peter teaches English (Critical subject)
    (5, 1, 4, 1, 'Available', NOW(), NOW()); -- Julia teaches German

