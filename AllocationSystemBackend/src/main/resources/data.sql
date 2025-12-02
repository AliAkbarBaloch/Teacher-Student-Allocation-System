INSERT INTO users ( id, email, password, full_name, enabled, account_locked, failed_login_attempts, last_login_date, last_password_reset_date, account_status, role, phone_number, created_at, updated_at)
VALUES
    (1,'admin@example.com','$2a$10$iu6rVkymWojgOcGgrzZmh.Om7y1910hk6aI/wZFVwdlh/wOn/hiB6','Admin User',TRUE,FALSE,0,NULL,NULL,'ACTIVE','ADMIN',NULL,NOW(),NOW());

INSERT INTO ACADEMIC_YEARS (id, year_name, total_credit_hours, elementary_school_hours, middle_school_hours, budget_announcement_date, allocation_deadline, is_locked, created_at, updated_at)
VALUES (1, '2025/2026', 210, 169, 41, '2025-05-15', '2025-06-30', FALSE, NOW(), NOW());

-- 2. INTERNSHIP_TYPE
-- Defining the 4 specific types with their timing and subject strictness.
INSERT INTO INTERNSHIP_TYPES (id, internship_code, full_name, timing, period_type, semester, is_subject_specific, priority_order, created_at, updated_at)
VALUES
    (1, 'PDP1', 'Pedagogical-didactic block internship I', 'Winter', 'Block', 1, FALSE, 3, NOW(), NOW()),
    (2, 'PDP2', 'Pedagogical-didactic block internship II', 'Summer', 'Block', 2, FALSE, 3, NOW(), NOW()),
    (3, 'ZSP', 'Additional study-accompanying internship', 'Winter', 'Wednesday', 1, TRUE, 2, NOW(), NOW()),
    (4, 'SFP', 'Study-accompanying subject-didactic internship', 'Summer', 'Wednesday', 2, TRUE, 1, NOW(), NOW());


-- -- 3. INTERNSHIP_COMBINATION_RULE
-- -- Defining valid pairs for a teacher (e.g., PDP 1 + PDP 2 is valid).
INSERT INTO INTERNSHIP_COMBINATION_RULES (id, internship_type_1_id, internship_type_2_id, is_valid_combination, created_at)
VALUES
    (1, 1, 2, TRUE, NOW()), -- PDP 1 + PDP 2
    (2, 1, 4, TRUE, NOW()), -- PDP 1 + SFP
    (3, 1, 3, TRUE, NOW()), -- PDP 1 + ZSP
    (4, 2, 4, TRUE, NOW()), -- PDP 2 + SFP
    (5, 2, 3, TRUE, NOW()), -- PDP 2 + ZSP
    (6, 4, 3, TRUE, NOW()); -- SFP + ZSP


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
INSERT INTO SUBJECT_CATEGORY (id, category_title, created_at, updated_at) VALUES (2, 'Religion', NOW(), NOW());
INSERT INTO SUBJECT_CATEGORY (id, category_title, created_at, updated_at) VALUES (3, 'Arts', NOW(), NOW());
INSERT INTO SUBJECT_CATEGORY (id, category_title, created_at, updated_at) VALUES (4, 'Social Sciences', NOW(), NOW());
INSERT INTO SUBJECT_CATEGORY (id, category_title, created_at, updated_at) VALUES (5, 'Technical', NOW(), NOW());
INSERT INTO SUBJECT_CATEGORY (id, category_title, created_at, updated_at) VALUES (6, 'Special', NOW(), NOW());

-- SUBJECTS INSERTS
INSERT INTO SUBJECTS (id, subject_code, subject_title, subject_category_id, school_type, is_active, created_at, updated_at) VALUES
    (1,  'D',      'German',                          1, 'Primary', TRUE, NOW(), NOW()),
    (2,  'MA',     'Mathematics',                     1, 'Primary', TRUE, NOW(), NOW()),
    (3,  'E',      'English',                         1, 'Middle',  TRUE, NOW(), NOW()),
    (4,  'KRel',   'Catholic Religion',               2, 'Primary', TRUE, NOW(), NOW()),
    (5,  'MU',     'Music',                           3, 'Primary', TRUE, NOW(), NOW()),
    (6,  'KE',     'Art Education',                   3, 'Primary', TRUE, NOW(), NOW()),
    (7,  'SP',     'Sport',                           3, 'Primary', TRUE, NOW(), NOW()),
    (8,  'SK',     'Social Studies',                  4, 'Middle',  TRUE, NOW(), NOW()),
    (9,  'PuG',    'Politics and Citizenship',        4, 'Middle',  TRUE, NOW(), NOW()),
    (10, 'GE',     'History',                         4, 'Middle',  TRUE, NOW(), NOW()),
    (11, 'GEO',    'Geography',                       4, 'Middle',  TRUE, NOW(), NOW()),
    (12, 'HSU',    'Home and Subject Matter Lessons', 4, 'Primary', TRUE, NOW(), NOW()),
    (13, 'AL',     'Work Education',                  5, 'Middle',  TRUE, NOW(), NOW()),
    (14, 'WiB',    'Economic and Vocational Studies', 5, 'Middle',  TRUE, NOW(), NOW()),
    (15, 'PCB',    'Physics-Chemistry-Biology',       5, 'Middle',  TRUE, NOW(), NOW()),
    (16, 'IT',     'Information Technology',          5, 'Middle',  TRUE, NOW(), NOW()),
    (17, 'GSE',    'History/Social Studies/Geography',5, 'Middle',  TRUE, NOW(), NOW()),
    (18, 'GPG',    'Regional History/Social Studies', 5, 'Middle',  TRUE, NOW(), NOW()),
    (19, 'DaZ',    'German as Second Language',       6, 'Primary', TRUE, NOW(), NOW()),
    (20, 'SSE',    'Language Support',                6, 'Primary', TRUE, NOW(), NOW());


-- 6. SCHOOL
-- Zone 1 (Passau City), Zone 2 (Suburbs), Zone 3 (Rural).
INSERT INTO SCHOOLS (id, school_name, school_type, zone_number, address, distance_from_center, transport_accessibility, is_active, created_at, updated_at)
VALUES
    (1, 'Grundschule Passau-Innstadt', 'Primary', 1, 'Innstadt kellerweg, Passau', 2.5, '4a', TRUE, NOW(), NOW()),
    (2, 'Mittelschule Salzweg', 'Middle', 2, 'Salzweg Hauptstr', 15.0, '4a', TRUE, NOW(), NOW()),
    (3, 'Grundschule Freyung', 'Primary', 3, 'Freyung Stadtplatz', 45.0, 'None', TRUE, NOW(), NOW());

-- 7. TEACHER
-- Creating supervisors. Note usage_cycle for HSU rotation.
INSERT INTO teachers (id, school_id, first_name, last_name, email, phone, is_part_time, employment_status, usage_cycle, credit_hour_balance, is_active, created_at, updated_at)
VALUES
    -- Teacher 1: Flexible Primary School Teacher (Zone 1)
    (1, 1, 'Hans', 'Müller', 'hans.mueller@gs-passau.de', '+49 851 123456', FALSE, 'ACTIVE', 'FLEXIBLE', 0, true, NOW(), NOW()),
    -- Teacher 2: Part-time (usually Wednesday constrained)
    (2, 1, 'Anna', 'Schmidt', 'anna.schmidt@gs-passau.de', NULL, TRUE, 'ACTIVE', 'FLEXIBLE', 0, true, NOW(), NOW()),
    -- Teacher 3: Middle School Teacher (Fixed to Grades 5-9 cycle)
    (3, 2, 'Peter', 'Weber', 'peter.weber@ms-salzweg.de', '+49 851 987654', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, true, NOW(), NOW()),
    -- Teacher 4: Primary Teacher (Fixed to Grades 1-2 cycle)
    (4, 3, 'Julia', 'Wagner', 'julia.wagner@gs-freyung.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, true, NOW(), NOW());

-- 8. TEACHER_SUBJECT
-- Linking teachers to what they are allowed to supervise.
INSERT INTO TEACHER_SUBJECTS (id, year_id, teacher_id, subject_id, availability_status, created_at, updated_at)
VALUES
    (1, 1, 1, 1, 'Available', NOW(), NOW()), -- Hans teaches German
    (2, 1, 1, 2, 'Available', NOW(), NOW()), -- Hans teaches Math
    (3, 1, 2, 4, 'Available', NOW(), NOW()), -- Anna teaches HSU
    (4, 1, 3, 3, 'Available', NOW(), NOW()), -- Peter teaches English (Critical subject)
    (5, 1, 4, 1, 'Available', NOW(), NOW()); -- Julia teaches German

-- 9. TEACHER_AVAILABILITY
-- Preferences submitted by teachers or imported via Excel.
INSERT INTO TEACHER_AVAILABILITY (id, teacher_id, academic_year_id, internship_type_id, status, preference_rank, notes, created_at)
VALUES
(1, 1, 1, 3, 'AVAILABLE', 1, 'Prefers Wednesday Winter', NOW()),
(2, 1, 1, 4, 'AVAILABLE', 2, 'Prefers Wednesday Summer', NOW()),
(3, 2, 1, 1, 'AVAILABLE', 0, 'Cannot do block internship due to family', NOW()),
(4, 4, 1, 1, 'AVAILABLE', 1, 'Live in Zone 3, prefers Block', NOW());

-- 10. INTERNSHIP_DEMANDS
-- The "Requirements" received from the internship office.
INSERT INTO internship_demands (id, academic_year_id, internship_type_id, school_type, subject_id, required_teachers, student_count, is_forecasted, created_at, updated_at)
VALUES
    -- High priority: SFP German in Primary schools
    (1, 1, 4, 'Primary', 1, 10, 40, TRUE, NOW(), NOW()),
    -- High priority: SFP English in Middle schools
    (2, 1, 4, 'Middle', 3, 5, 20, TRUE, NOW(), NOW()),
    -- Block internship (subject not relevant) - using subject_id = 1 as a placeholder (cannot be NULL)
    (3, 1, 1, 'Primary', 1, 15, 30, FALSE, NOW(), NOW());

-- 11. ALLOCATION_PLAN
-- A draft plan created by the Admin.
INSERT INTO ALLOCATION_PLANS (id, year_id, plan_name, plan_version, status, created_by_user_id, is_current, created_at, updated_at)
VALUES
(1, 1, 'Allocation Draft V1', '1.0', 'Draft', 1,TRUE, NOW(), NOW());

-- 12. TEACHER_ASSIGNMENT
-- The result of the allocation logic. Hans matches strict subject constraint for ZSP.
INSERT INTO TEACHER_ASSIGNMENTS (id, plan_id, teacher_id, internship_type_id, subject_id, student_group_size, assignment_status, is_manual_override, notes, assigned_at, created_at)
VALUES
-- Hans assigned to SFP (German) - Matches Subject
(1, 1, 1, 4, 1, 4, 'Confirmed', FALSE, 'Auto-matched based on subject D', NOW(), NOW()),
-- Hans assigned to ZSP (Math) - Matches Subject
(2, 1, 1, 3, 2, 3, 'Confirmed', FALSE, 'Auto-matched based on subject MA', NOW(), NOW());

-- 13. CREDIT_HOUR_TRACKING
-- Hans has 2 assignments, so he earns 1.0 credit hour (Reduction hour).
INSERT INTO CREDIT_HOUR_TRACKING (id, teacher_id, academic_year_id, assignments_count, credit_hours_allocated, credit_balance, notes, created_at)
VALUES
(1, 1, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW());