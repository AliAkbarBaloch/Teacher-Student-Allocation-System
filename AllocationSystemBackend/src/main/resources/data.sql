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
-- SUBJECT_CATEGORY INSERTS
INSERT INTO SUBJECT_CATEGORY (id, category_title, created_at, updated_at) VALUES (1, 'Core Subjects', NOW(), NOW());
INSERT INTO SUBJECT_CATEGORY (id, category_title, created_at, updated_at) VALUES (2, 'Religion', NOW(), NOW());
INSERT INTO SUBJECT_CATEGORY (id, category_title, created_at, updated_at) VALUES (3, 'Arts', NOW(), NOW());
INSERT INTO SUBJECT_CATEGORY (id, category_title, created_at, updated_at) VALUES (4, 'Social Sciences', NOW(), NOW());
INSERT INTO SUBJECT_CATEGORY (id, category_title, created_at, updated_at) VALUES (5, 'Technical', NOW(), NOW());
INSERT INTO SUBJECT_CATEGORY (id, category_title, created_at, updated_at) VALUES (6, 'Special', NOW(), NOW());

-- SUBJECTS INSERTS (Corrected and Expanded)

-- 1. Core Subjects (ID 1)
INSERT INTO SUBJECTS (id, subject_code, subject_title, subject_category_id, school_type, is_active, created_at, updated_at) VALUES
    (1,  'D',    'German',                              1, 'Primary', TRUE, NOW(), NOW()),
    (2,  'MA',   'Mathematics',                         1, 'Primary', TRUE, NOW(), NOW()),
    (3,  'E',    'English',                             1, 'Middle',  TRUE, NOW(), NOW()),
    (21, 'D',    'German',                              1, 'Middle',  TRUE, NOW(), NOW()),  -- Added for Middle School
    (22, 'MA',   'Mathematics',                         1, 'Middle',  TRUE, NOW(), NOW()),  -- Added for Middle School
    (23, 'E',    'English',                             1, 'Primary', TRUE, NOW(), NOW());  -- Added for Primary School

-- 2. Religion (ID 2)
INSERT INTO SUBJECTS (id, subject_code, subject_title, subject_category_id, school_type, is_active, created_at, updated_at) VALUES
    (4,  'KRel', 'Catholic Religion',                   2, 'Primary', TRUE, NOW(), NOW());

-- 3. Arts (ID 3)
INSERT INTO SUBJECTS (id, subject_code, subject_title, subject_category_id, school_type, is_active, created_at, updated_at) VALUES
    (5,  'MU',   'Music',                               3, 'Primary', TRUE, NOW(), NOW()),
    (6,  'KE',   'Art Education',                       3, 'Primary', TRUE, NOW(), NOW()),
    (7,  'SP',   'Sport',                               3, 'Primary', TRUE, NOW(), NOW()),
    (25, 'GU',   'Art and Environment (Gestalten u. Umwelt)', 3, 'Primary', TRUE, NOW(), NOW()); -- Added from CSV (GU)

-- 4. Social Sciences (ID 4)
INSERT INTO SUBJECTS (id, subject_code, subject_title, subject_category_id, school_type, is_active, created_at, updated_at) VALUES
    (8,  'SK',   'Social Studies (Sozialkunde)',        4, 'Middle',  TRUE, NOW(), NOW()),
    (9,  'PuG',  'Politics and Citizenship',            4, 'Middle',  TRUE, NOW(), NOW()),
    (10, 'GE',   'History',                             4, 'Middle',  TRUE, NOW(), NOW()),
    (11, 'GEO',  'Geography',                           4, 'Middle',  TRUE, NOW(), NOW()),
    (12, 'HSU',  'Home and Subject Matter Lessons (Sachunterricht)', 4, 'Primary', TRUE, NOW(), NOW()),
    -- CORRECTION: Changed Category from 5 (Technical) to 4 (Social Sciences) for GSE and GPG
    (17, 'GSE',  'History/Social Studies/Geography',    4, 'Middle',  TRUE, NOW(), NOW()),
    (18, 'GPG',  'Regional History/Social Studies',     4, 'Middle',  TRUE, NOW(), NOW());

-- 5. Technical (ID 5)
INSERT INTO SUBJECTS (id, subject_code, subject_title, subject_category_id, school_type, is_active, created_at, updated_at) VALUES
    (13, 'AL',   'Work Education (Arbeitslehre)',       5, 'Middle',  TRUE, NOW(), NOW()),
    (14, 'WiB',  'Economic and Vocational Studies',     5, 'Middle',  TRUE, NOW(), NOW()),
    (15, 'PCB',  'Physics-Chemistry-Biology',           5, 'Middle',  TRUE, NOW(), NOW()),
    (16, 'IT',   'Information Technology',              5, 'Middle',  TRUE, NOW(), NOW());

-- 6. Special (ID 6)
INSERT INTO SUBJECTS (id, subject_code, subject_title, subject_category_id, school_type, is_active, created_at, updated_at) VALUES
    (19, 'DaZ',  'German as Second Language (DaZ)',     6, 'Primary', TRUE, NOW(), NOW()),
    (20, 'SSE',  'Language Support (SSE)',              6, 'Primary', TRUE, NOW(), NOW()),
    (24, 'SPAD', 'School Pedagogy (Schulpädagogik)',    6, 'Both',    TRUE, NOW(), NOW()); -- Added from CSV (Schulpäd.)


-- 6. SCHOOL
INSERT INTO SCHOOLS (id, school_name, school_type, zone_number, address, latitude, longitude, distance_from_center, transport_accessibility, contact_email, contact_phone, is_active, created_at, updated_at)
VALUES
    -- User's Original Schools (IDs 1-3) - Start of List for Continuity
    (1, 'Grundschule Passau-Innstadt', 'PRIMARY', 1, 'Innstadt kellerweg, Passau', 48.5730, 13.4560, 2.5, '4a', 'info@gs-innstadt.de', '+49 851 123456', TRUE, NOW(), NOW()),
    (2, 'Mittelschule Salzweg', 'MIDDLE', 2, 'Salzweg Hauptstr', 48.6050, 13.3730, 15.0, '4a', 'info@ms-salzweg.de', '+49 851 987654', TRUE, NOW(), NOW()),
    (3, 'Grundschule Freyung', 'PRIMARY', 3, 'Freyung Stadtplatz', 48.7330, 13.5500, 45.0, 'None', 'info@gs-freyung.de', NULL, TRUE, NOW(), NOW()),

    -- ZONE 1 SCHOOLS (1-15 km, Wednesday Focus) - 7 Primary, 3 Middle
    (4, 'Grundschule Grubweg', 'PRIMARY', 1, 'Passau-Grubweg Schulstr', 48.5705, 13.4012, 3.8, '4a', 'gs.grubweg@schule.de', '+49 851 20110', TRUE, NOW(), NOW()),
    (5, 'Mittelschule Hutthurm', 'MIDDLE', 1, 'Hutthurm Schulgasse', 48.6601, 13.3855, 10.5, '4a', 'ms.hutthurm@schule.de', '+49 8505 50011', TRUE, NOW(), NOW()),
    (6, 'Grundschule Ruderting', 'PRIMARY', 1, 'Ruderting Kirchweg', 48.6254, 13.3540, 7.5, '4b', 'gs.ruderting@schule.de', '+49 8509 91230', TRUE, NOW(), NOW()),
    (7, 'Grundschule Neukirchen v. W.', 'PRIMARY', 1, 'Neukirchen Hauptstr', 48.5810, 13.2951, 9.2, '4b', 'gs.neukirchen@schule.de', '+49 8502 9180', TRUE, NOW(), NOW()),
    (8, 'Mittelschule Tiefenbach', 'MIDDLE', 1, 'Tiefenbach Schulzentrum', 48.6322, 13.3087, 12.0, '4a', 'ms.tiefenbach@schule.de', '+49 8503 92050', TRUE, NOW(), NOW()),
    (9, 'Grundschule Patriching', 'PRIMARY', 1, 'Passau-Patriching Str. 12', 48.5902, 13.4150, 4.5, '4a', 'gs.patriching@schule.de', '+49 851 88990', TRUE, NOW(), NOW()),
    (10, 'Grundschule Vilshofen-Nord', 'PRIMARY', 1, 'Vilshofen-Nord Weg 1', 48.6410, 13.3000, 14.8, '4a', 'gs.vilshofen.n@schule.de', '+49 8541 7777', TRUE, NOW(), NOW()),
    (11, 'Grundschule Ortenburg', 'PRIMARY', 1, 'Ortenburg Markt 5', 48.5721, 13.3305, 8.8, '4b', 'gs.ortenburg@schule.de', '+49 8542 96110', TRUE, NOW(), NOW()),
    (12, 'Mittelschule Passau-Heining', 'MIDDLE', 1, 'Passau-Heining Str. 3', 48.5600, 13.3800, 4.0, '4a', 'ms.heining@schule.de', '+49 851 55443', TRUE, NOW(), NOW()),
    (13, 'Grundschule Eging am See', 'PRIMARY', 1, 'Eging am See Seestr', 48.7001, 13.3900, 14.5, '4b', 'gs.eging@schule.de', '+49 8544 9110', TRUE, NOW(), NOW()),

    -- ZONE 2 SCHOOLS (15-40 km, Flexible Focus) - 12 Primary, 3 Middle
    (14, 'Grundschule Tittling', 'PRIMARY', 2, 'Tittling Marktplatz 1', 48.7203, 13.3980, 17.5, '4a', 'gs.tittling@schule.de', '+49 8504 90011', TRUE, NOW(), NOW()),
    (15, 'Mittelschule Hauzenberg', 'MIDDLE', 2, 'Hauzenberg Schulring', 48.6515, 13.6210, 20.0, '4b', 'ms.hauzenberg@schule.de', '+49 8586 97880', TRUE, NOW(), NOW()),
    (16, 'Grundschule Vilshofen-Stadt', 'PRIMARY', 2, 'Vilshofen-Stadt Donau 1', 48.6360, 13.1900, 24.5, '4a', 'gs.vilshofen.s@schule.de', '+49 8541 6655', TRUE, NOW(), NOW()),
    (17, 'Grundschule Schöllnach', 'PRIMARY', 2, 'Schöllnach Deggendorfer Str', 48.7600, 13.2800, 25.0, 'None', 'gs.schoellnach@schule.de', '+49 9903 9100', TRUE, NOW(), NOW()),
    (18, 'Mittelschule Grafenau', 'MIDDLE', 2, 'Grafenau Hauptstr 50', 48.8600, 13.3800, 34.0, '4b', 'ms.grafenau@schule.de', '+49 8552 40011', TRUE, NOW(), NOW()),
    (19, 'Grundschule Aidenbach', 'PRIMARY', 2, 'Aidenbach Poststr', 48.4900, 13.1000, 35.5, '4a', 'gs.aidenbach@schule.de', '+49 8543 96960', TRUE, NOW(), NOW()),
    (20, 'Grundschule Osterhofen', 'PRIMARY', 2, 'Osterhofen Schulgasse 4', 48.6500, 12.9800, 39.5, 'None', 'gs.osterhofen@schule.de', '+49 9932 90055', TRUE, NOW(), NOW()),
    (21, 'Grundschule Waldkirchen', 'PRIMARY', 2, 'Waldkirchen Zentrum', 48.7300, 13.6100, 25.0, '4b', 'gs.waldkirchen@schule.de', '+49 8581 9650', TRUE, NOW(), NOW()),
    (22, 'Mittelschule Bogen', 'MIDDLE', 2, 'Bogen Stadtplatz', 48.9100, 12.6800, 38.0, '4a', 'ms.bogen@schule.de', '+49 9422 9660', TRUE, NOW(), NOW()),
    (23, 'Grundschule Deggendorf-Mitte', 'PRIMARY', 2, 'Deggendorf Mitte Str 1', 48.8400, 12.9600, 39.0, '4a', 'gs.deggendorf.m@schule.de', '+49 991 30303', TRUE, NOW(), NOW()),
    (24, 'Grundschule Bad Griesbach', 'PRIMARY', 2, 'Bad Griesbach Schulstr', 48.4200, 13.2000, 23.5, '4b', 'gs.badgriesbach@schule.de', '+49 8532 9600', TRUE, NOW(), NOW()),
    (25, 'Grundschule Fürstenstein', 'PRIMARY', 2, 'Fürstenstein Schlossweg', 48.7000, 13.3100, 18.5, 'None', 'gs.fuerstenstein@schule.de', '+49 8504 9300', TRUE, NOW(), NOW()),
    (26, 'Grundschule Röhrnbach', 'PRIMARY', 2, 'Röhrnbach Schulgasse 1', 48.7500, 13.6700, 32.0, 'None', 'gs.roehrnbach@schule.de', '+49 8582 9100', TRUE, NOW(), NOW()),
    (27, 'Grundschule Kellberg', 'PRIMARY', 2, 'Kellberg Kirchweg', 48.6300, 13.6400, 19.5, '4b', 'gs.kellberg@schule.de', '+49 8503 9200', TRUE, NOW(), NOW()),
    (28, 'Grundschule Witzmannsberg', 'PRIMARY', 2, 'Witzmannsberg Dorfstr', 48.7500, 13.4000, 21.0, 'None', 'gs.witzmannsberg@schule.de', '+49 8504 90090', TRUE, NOW(), NOW()),

    -- ZONE 3 SCHOOLS (40-100 km, Block Focus) - 21 Primary, 4 Middle
    (29, 'Grundschule Cham-West', 'PRIMARY', 3, 'Cham Westend 4', 49.2300, 12.6700, 95.0, 'None', 'gs.cham.w@schule.de', '+49 9971 70010', TRUE, NOW(), NOW()),
    (30, 'Mittelschule Viechtach', 'MIDDLE', 3, 'Viechtach Schulstr 1', 49.0800, 12.9800, 68.0, 'None', 'ms.viechtach@schule.de', '+49 9942 9400', TRUE, NOW(), NOW()),
    (31, 'Grundschule Zwiesel', 'PRIMARY', 3, 'Zwiesel Stadtplatz 1', 48.9800, 13.2300, 50.0, 'None', 'gs.zwiesel@schule.de', '+49 9922 9800', TRUE, NOW(), NOW()),
    (32, 'Grundschule Bad Birnbach', 'PRIMARY', 3, 'Bad Birnbach Kirchstr', 48.3300, 13.1200, 48.0, 'None', 'gs.badbirnbach@schule.de', '+49 8563 9700', TRUE, NOW(), NOW()),
    (33, 'Mittelschule Bodenmais', 'MIDDLE', 3, 'Bodenmais Hauptstr', 49.0600, 13.1000, 60.0, 'None', 'ms.bodenmais@schule.de', '+49 9924 9400', TRUE, NOW(), NOW()),
    (34, 'Grundschule Landau an der Isar', 'PRIMARY', 3, 'Landau Isarstr 10', 48.6800, 12.7200, 52.0, 'None', 'gs.landau@schule.de', '+49 9951 90050', TRUE, NOW(), NOW()),
    (35, 'Grundschule Straubing-Süd', 'PRIMARY', 3, 'Straubing Südring', 48.8600, 12.5600, 61.0, 'None', 'gs.straubing.s@schule.de', '+49 9421 9800', TRUE, NOW(), NOW()),
    (36, 'Grundschule Pocking', 'PRIMARY', 3, 'Pocking Schulstr 5', 48.3800, 13.3100, 40.5, 'None', 'gs.pocking@schule.de', '+49 8531 90011', TRUE, NOW(), NOW()),
    (37, 'Mittelschule Pfarrkirchen', 'MIDDLE', 3, 'Pfarrkirchen Schulzentrum', 48.4200, 13.0900, 42.0, 'None', 'ms.pfarrkirchen@schule.de', '+49 8561 9700', TRUE, NOW(), NOW()),
    (38, 'Grundschule Osterhofen-Altenmarkt', 'PRIMARY', 3, 'Altenmarkt Str 1', 48.6700, 12.9500, 42.0, 'None', 'gs.altenmarkt@schule.de', '+49 9932 90011', TRUE, NOW(), NOW()),
    (39, 'Grundschule Regen', 'PRIMARY', 3, 'Regen Stadtplatz 2', 48.9600, 13.1200, 58.0, 'None', 'gs.regen@schule.de', '+49 9921 9500', TRUE, NOW(), NOW()),
    (40, 'Grundschule Bad Füssing', 'PRIMARY', 3, 'Bad Füssing Kurplatz', 48.3300, 13.3100, 48.0, 'None', 'gs.badfuessing@schule.de', '+49 8531 9600', TRUE, NOW(), NOW()),
    (41, 'Grundschule Simbach am Inn', 'PRIMARY', 3, 'Simbach Innstr', 48.2700, 13.0300, 62.0, 'None', 'gs.simbach.inn@schule.de', '+49 8571 9600', TRUE, NOW(), NOW()),
    (42, 'Mittelschule Plattling', 'MIDDLE', 3, 'Plattling Schulgasse 2', 48.7700, 12.9200, 45.0, 'None', 'ms.plattling@schule.de', '+49 9938 90050', TRUE, NOW(), NOW()),
    (43, 'Grundschule Dingolfing-Nord', 'PRIMARY', 3, 'Dingolfing Nordring', 48.6300, 12.4800, 71.0, 'None', 'gs.dingolfing.n@schule.de', '+49 8732 90011', TRUE, NOW(), NOW()),
    (44, 'Grundschule Deggendorf-Ost', 'PRIMARY', 3, 'Deggendorf Oststr 3', 48.8800, 13.0000, 44.0, 'None', 'gs.deggendorf.o@schule.de', '+49 991 30300', TRUE, NOW(), NOW()),
    (45, 'Grundschule Viechtach-Süd', 'PRIMARY', 3, 'Viechtach Südstr', 49.0500, 12.9200, 69.0, 'None', 'gs.viechtach.s@schule.de', '+49 9942 9400', TRUE, NOW(), NOW()),
    (46, 'Grundschule Mühldorf am Inn', 'PRIMARY', 3, 'Mühldorf Innstr 1', 48.2500, 12.5200, 95.0, 'None', 'gs.muehldorf@schule.de', '+49 8631 9600', TRUE, NOW(), NOW()),
    (47, 'Grundschule Vilshofen-Land', 'PRIMARY', 3, 'Vilshofen Landstr', 48.6800, 13.1500, 43.0, 'None', 'gs.vilshofen.l@schule.de', '+49 8541 6650', TRUE, NOW(), NOW()),
    (48, 'Grundschule Zwiesel-Nord', 'PRIMARY', 3, 'Zwiesel Nordweg 5', 49.0200, 13.2500, 54.0, 'None', 'gs.zwiesel.n@schule.de', '+49 9922 9801', TRUE, NOW(), NOW()),
    (49, 'Grundschule Roding', 'PRIMARY', 3, 'Roding Stadtplatz 3', 49.1900, 12.5200, 85.0, 'None', 'gs.roding@schule.de', '+49 9461 9600', TRUE, NOW(), NOW()),
    (50, 'Grundschule Arnstorf', 'PRIMARY', 3, 'Arnstorf Marktstr 1', 48.5100, 12.8700, 41.0, 'None', 'gs.arnstorf@schule.de', '+49 8723 9600', TRUE, NOW(), NOW()),
    (51, 'Grundschule Mainburg', 'PRIMARY', 3, 'Mainburg Zentrum', 48.6800, 11.8300, 99.0, 'None', 'gs.mainburg@schule.de', '+49 8751 90011', TRUE, NOW(), NOW()),
    (52, 'Grundschule Simbach bei Landau', 'PRIMARY', 3, 'Simbach Hauptstr 1', 48.6300, 12.5800, 60.0, 'None', 'gs.simbach.l@schule.de', '+49 9954 90050', TRUE, NOW(), NOW()),
    (53, 'Grundschule Bad Kötzting', 'PRIMARY', 3, 'Bad Kötzting Kurstr', 49.1700, 12.8700, 80.0, 'None', 'gs.badkoetzting@schule.de', '+49 9941 90011', TRUE, NOW(), NOW());

-- 7. TEACHER
-- Creating supervisors. Note usage_cycle for HSU rotation.
INSERT INTO teachers (id, school_id, first_name, last_name, email, phone, is_part_time, employment_status, usage_cycle, credit_hour_balance, is_active, created_at, updated_at)
VALUES
    -- User's Original Teachers (IDs 1-4)
    (1, 1, 'Hans', 'Müller', 'hans.mueller@gs-passau.de', '+49 851 123456', FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (2, 1, 'Anna', 'Schmidt', 'anna.schmidt@gs-passau.de', NULL, TRUE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (3, 2, 'Peter', 'Weber', 'peter.weber@ms-salzweg.de', '+49 851 987654', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, TRUE, NOW(), NOW()),
    (4, 3, 'Julia', 'Wagner', 'julia.wagner@gs-freyung.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),

    -- Generated Teachers (IDs 5-114)

    -- ZONE 1 & PRIMARY Focused (GS 4, 6, 7, 9, 10, 11, 13, 15)
    (5, 4, 'Michael', 'Schneider', 'm.schneider@gs-grubweg.de', '+49 851 20111', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (6, 6, 'Laura', 'Fischer', 'laura.fischer@gs-ruderting.de', NULL, TRUE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (7, 7, 'Thomas', 'Meyer', 'thomas.meyer@gs-neukirchen.de', '+49 8502 9181', FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (8, 9, 'Sabine', 'Huber', 'sabine.huber@gs-patriching.de', '+49 851 88991', TRUE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (9, 10, 'Tobias', 'Lang', 'tobias.lang@gs-vilshofen-n.de', '+49 8541 7778', FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (10, 11, 'Marie', 'Hoffmann', 'marie.hoffmann@gs-ortenburg.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (11, 13, 'Daniel', 'Schulz', 'daniel.schulz@gs-eging.de', '+49 8544 9111', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (12, 15, 'Katrin', 'Kruse', 'katrin.kruse@gs-neuhaus.de', '+49 851 33344', TRUE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),

    -- ZONE 1 & MIDDLE Focused (MS 5, 12)
    (13, 5, 'Jonas', 'Wagner', 'jonas.wagner@ms-hutthurm.de', '+49 8505 50012', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, TRUE, NOW(), NOW()),
    (14, 12, 'Hannah', 'Bauer', 'hannah.bauer@ms-heining.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),

    -- ZONE 2 & PRIMARY Focused (GS 14, 16, 17, 19, 20, 21, 23, 24, 25, 26, 27, 28)
    (15, 14, 'Leon', 'Schwarz', 'leon.schwarz@gs-tittling.de', '+49 8504 90012', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (16, 16, 'Sophie', 'Weber', 'sophie.weber@gs-vilshofen-s.de', '+49 8541 6656', TRUE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (17, 17, 'Max', 'Krüger', 'max.krueger@gs-schoellnach.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (18, 19, 'Lena', 'Richter', 'lena.richter@gs-aidenbach.de', '+49 8543 96961', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (19, 20, 'Paul', 'Wolf', 'paul.wolf@gs-osterhofen.de', '+49 9932 90056', FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (20, 21, 'Emilia', 'Neumann', 'emilia.neumann@gs-waldkirchen.de', '+49 8581 9651', TRUE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (21, 23, 'Felix', 'Köhler', 'felix.koehler@gs-deggendorf-m.de', '+49 991 30304', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (22, 24, 'Mia', 'Gärtner', 'mia.gaertner@gs-badgriesbach.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (23, 25, 'Elias', 'Hahn', 'elias.hahn@gs-fuerstenstein.de', '+49 8504 9301', TRUE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (24, 26, 'Clara', 'Beck', 'clara.beck@gs-roehrnbach.de', '+49 8582 9101', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (25, 27, 'Leo', 'Herzog', 'leo.herzog@gs-kellberg.de', '+49 8503 9201', FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (26, 28, 'Paula', 'Sauer', 'paula.sauer@gs-witzmannsberg.de', '+49 8504 90091', TRUE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),

    -- ZONE 2 & MIDDLE Focused (MS 2, 8, 15, 18, 22)
    (27, 2, 'Karl', 'Zimmermann', 'karl.zimmermann@ms-salzweg.de', '+49 851 987655', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, TRUE, NOW(), NOW()),
    (28, 8, 'Luisa', 'Möller', 'luisa.moeller@ms-hutthurm.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (29, 15, 'Simon', 'Frank', 'simon.frank@ms-hauzenberg.de', '+49 8586 97881', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, TRUE, NOW(), NOW()),
    (30, 18, 'Nora', 'Vogel', 'nora.vogel@ms-grafenau.de', '+49 8552 40012', TRUE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (31, 22, 'Moritz', 'Jung', 'moritz.jung@ms-bogen.de', '+49 9422 9661', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, TRUE, NOW(), NOW()),

    -- ZONE 3 & PRIMARY Focused (GS 3, 29, 31, 32, 34, 35, 36, 38, 39, 40, 41, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53)
    (32, 3, 'Finn', 'Schuster', 'finn.schuster@gs-freyung.de', '+49 8551 11223', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (33, 29, 'Amelie', 'Peters', 'amelie.peters@gs-cham-w.de', NULL, TRUE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (34, 31, 'Ben', 'Grams', 'ben.grams@gs-zwiesel.de', '+49 9922 9801', FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (35, 32, 'Lilly', 'Koch', 'lilly.koch@gs-badbirnbach.de', '+49 8563 9701', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (36, 34, 'Elias', 'Schröder', 'e.schroeder@gs-landau.de', '+49 9951 90051', TRUE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (37, 35, 'Sarah', 'Maier', 'sarah.maier@gs-straubing-s.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (38, 36, 'Niklas', 'Fuchs', 'niklas.fuchs@gs-pocking.de', '+49 8531 90012', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (39, 38, 'Anna', 'Reiter', 'anna.reiter@gs-altenmarkt.de', '+49 9932 90012', FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (40, 39, 'Julian', 'Gross', 'julian.gross@gs-regen.de', '+49 9921 9501', TRUE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (41, 40, 'Mona', 'Keller', 'mona.keller@gs-badfuessing.de', '+49 8531 9601', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (42, 41, 'Luis', 'Baumgartner', 'luis.baumgartner@gs-simbach-i.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (43, 43, 'Frida', 'Winkler', 'frida.winkler@gs-dingolfing-n.de', '+49 8732 90012', FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (44, 44, 'Chris', 'Kunz', 'chris.kunz@gs-deggendorf-o.de', '+49 991 30301', TRUE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (45, 45, 'Elena', 'Stein', 'elena.stein@gs-viechtach-s.de', '+49 9942 9401', FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (46, 46, 'Robert', 'Vogt', 'robert.vogt@gs-muehldorf.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (47, 47, 'Sophie', 'Engel', 'sophie.engel@gs-vilshofen-l.de', '+49 8541 6651', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (48, 48, 'Anton', 'Moser', 'anton.moser@gs-zwiesel-n.de', '+49 9922 9802', TRUE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (49, 49, 'Lisa', 'König', 'lisa.koenig@gs-roding.de', '+49 9461 9601', FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (50, 50, 'Erik', 'Lehmann', 'erik.lehmann@gs-arnstorf.de', '+49 8723 9601', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (51, 51, 'Maria', 'Sperr', 'maria.sperr@gs-mainburg.de', NULL, TRUE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (52, 52, 'Philipp', 'Kamm', 'philipp.kamm@gs-simbach-l.de', '+49 9954 90051', FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (53, 53, 'Nicole', 'Seidl', 'nicole.seidl@gs-badkoetzting.de', '+49 9941 90012', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),

    -- ZONE 3 & MIDDLE Focused (MS 30, 33, 37, 42)
    (54, 30, 'Wolfgang', 'Haas', 'wolfgang.haas@ms-viechtach.de', '+49 9942 9402', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, TRUE, NOW(), NOW()),
    (55, 33, 'Theresa', 'Meier', 'theresa.meier@ms-bodenmais.de', '+49 9924 9401', TRUE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (56, 37, 'Jürgen', 'Herzog', 'juergen.herzog@ms-pfarrkirchen.de', NULL, FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, TRUE, NOW(), NOW()),
    (57, 42, 'Lena', 'Keller', 'lena.keller@ms-plattling.de', '+49 9938 90051', FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),

    -- Additional Mixed Teachers (IDs 58-114, cycling through schools)
    (58, 1, 'Markus', 'Breu', 'markus.breu@gs-innstadt.de', '+49 851 123457', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (59, 2, 'Petra', 'Riedl', 'petra.riedl@ms-salzweg.de', NULL, TRUE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (60, 4, 'Stefan', 'Eder', 'stefan.eder@gs-grubweg.de', '+49 851 20112', FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (61, 5, 'Christa', 'Preiß', 'christa.preiss@ms-hutthurm.de', '+49 8505 50013', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, TRUE, NOW(), NOW()),
    (62, 7, 'Klaus', 'Lehner', 'klaus.lehner@gs-neukirchen.de', NULL, TRUE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (63, 8, 'Elke', 'Huber', 'elke.huber@ms-tiefenbach.de', '+49 8503 92051', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, TRUE, NOW(), NOW()),
    (64, 10, 'Franz', 'Ziegler', 'franz.ziegler@gs-vilshofen-n.de', '+49 8541 7779', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (65, 12, 'Lisa', 'Bachmann', 'lisa.bachmann@ms-heining.de', '+49 851 55444', TRUE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (66, 14, 'Andreas', 'Holzer', 'andreas.holzer@gs-tittling.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (67, 16, 'Monika', 'Kirchberger', 'monika.kirchberger@gs-vilshofen-s.de', '+49 8541 6657', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (68, 18, 'Robert', 'Dorsch', 'robert.dorsch@ms-grafenau.de', '+49 8552 40013', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, TRUE, NOW(), NOW()),
    (69, 20, 'Bianca', 'Mayer', 'bianca.mayer@gs-osterhofen.de', '+49 9932 90057', TRUE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (70, 22, 'Johann', 'Reindl', 'johann.reindl@ms-bogen.de', NULL, FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, TRUE, NOW(), NOW()),
    (71, 24, 'Maria', 'Sinz', 'maria.sinz@gs-badgriesbach.de', '+49 8532 9601', FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (72, 26, 'Herbert', 'Götz', 'herbert.goetz@gs-roehrnbach.de', '+49 8582 9102', TRUE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (73, 28, 'Manuela', 'Aigner', 'manuela.aigner@gs-witzmannsberg.de', '+49 8504 90092', FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (74, 30, 'Werner', 'Schmid', 'werner.schmid@ms-viechtach.de', NULL, FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, TRUE, NOW(), NOW()),
    (75, 32, 'Silke', 'Heller', 'silke.heller@gs-badbirnbach.de', '+49 8563 9702', FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (76, 34, 'Erich', 'Baumann', 'erich.baumann@gs-landau.de', '+49 9951 90052', TRUE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (77, 36, 'Ute', 'Heindl', 'ute.heindl@gs-pocking.de', '+49 8531 90013', FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (78, 38, 'Gerd', 'Schäfer', 'gerd.schaefer@gs-altenmarkt.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (79, 40, 'Helga', 'Seidl', 'helga.seidl@gs-badfuessing.de', '+49 8531 9602', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (80, 42, 'Rolf', 'Wagner', 'rolf.wagner@ms-plattling.de', '+49 9938 90052', TRUE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (81, 44, 'Inge', 'Huber', 'inge.huber@gs-deggendorf-o.de', '+49 991 30302', FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (82, 46, 'Bernd', 'Kastner', 'bernd.kastner@gs-muehldorf.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (83, 48, 'Uschi', 'Hofmann', 'uschi.hofmann@gs-zwiesel-n.de', '+49 9922 9803', FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (84, 50, 'Otto', 'Berger', 'otto.berger@gs-arnstorf.de', '+49 8723 9602', TRUE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (85, 52, 'Heike', 'Meier', 'heike.meier@gs-simbach-l.de', '+49 9954 90052', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (86, 3, 'Gisela', 'Hartl', 'gisela.hartl@gs-freyung.de', '+49 8551 11224', FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (87, 5, 'Hubert', 'Jahn', 'hubert.jahn@ms-hutthurm.de', '+49 8505 50014', TRUE, 'ACTIVE', 'GRADES_5_TO_9', 0, TRUE, NOW(), NOW()),
    (88, 7, 'Christina', 'Wirth', 'christina.wirth@gs-neukirchen.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (89, 9, 'Rainer', 'Doll', 'rainer.doll@gs-patriching.de', '+49 851 88992', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (90, 11, 'Silvia', 'Brandl', 'silvia.brandl@gs-ortenburg.de', '+49 8542 96111', FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (91, 13, 'Georg', 'Lindinger', 'georg.lindinger@gs-eging.de', '+49 8544 9112', TRUE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (92, 15, 'Eva', 'Straub', 'eva.straub@ms-hauzenberg.de', '+49 8586 97882', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, TRUE, NOW(), NOW()),
    (93, 17, 'Josef', 'Wimmer', 'josef.wimmer@gs-schoellnach.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (94, 19, 'Renate', 'Schick', 'renate.schick@gs-aidenbach.de', '+49 8543 96962', FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (95, 21, 'Lukas', 'Mühlbauer', 'lukas.muehlbauer@gs-waldkirchen.de', '+49 8581 9652', TRUE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (96, 23, 'Kerstin', 'Friedl', 'kerstin.friedl@gs-deggendorf-m.de', '+49 991 30305', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (97, 25, 'Michael', 'Pretzl', 'michael.pretzl@gs-fuerstenstein.de', '+49 8504 9302', FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (98, 27, 'Tanja', 'Maier', 'tanja.maier@gs-kellberg.de', '+49 8503 9202', TRUE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (99, 29, 'Florian', 'Winter', 'florian.winter@gs-cham-w.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (100, 31, 'Susanne', 'Luger', 'susanne.luger@gs-zwiesel.de', '+49 9922 9804', FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (101, 33, 'Harald', 'Vilsmeier', 'harald.vilsmeier@ms-bodenmais.de', '+49 9924 9402', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, TRUE, NOW(), NOW()),
    (102, 35, 'Vanessa', 'Strauss', 'vanessa.strauss@gs-straubing-s.de', NULL, TRUE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (103, 37, 'Jochen', 'Koch', 'jochen.koch@ms-pfarrkirchen.de', '+49 8561 9701', FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (104, 39, 'Tina', 'Baier', 'tina.baier@gs-regen.de', '+49 9921 9502', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (105, 41, 'Max', 'Schmitt', 'max.schmitt@gs-simbach-i.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (106, 43, 'Sabine', 'Hahn', 'sabine.hahn@gs-dingolfing-n.de', '+49 8732 90013', TRUE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (107, 45, 'Christian', 'Meier', 'christian.meier@gs-viechtach-s.de', '+49 9942 9403', FALSE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (108, 47, 'Melanie', 'Schulz', 'melanie.schulz@gs-vilshofen-l.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    (109, 49, 'Thomas', 'Wagner', 'thomas.wagner@gs-roding.de', '+49 9461 9602', FALSE, 'ACTIVE', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (110, 51, 'Verena', 'Zitzelsberger', 'verena.zitzelsberger@gs-mainburg.de', NULL, TRUE, 'ACTIVE', 'GRADES_3_4', 0, TRUE, NOW(), NOW()),
    (111, 53, 'Hubert', 'Müller', 'hubert.mueller@gs-badkoetzting.de', '+49 9941 90013', FALSE, 'ACTIVE', 'GRADES_1_2', 0, TRUE, NOW(), NOW()),
    
    -- Teachers with specific status (INACTIVE) or balance (CREDIT/DEBT)
    (112, 1, 'Hanna', 'Reischl', 'hanna.reischl@gs-innstadt-inactive.de', '+49 851 123458', FALSE, 'INACTIVE_THIS_YEAR', 'FLEXIBLE', 0, TRUE, NOW(), NOW()),
    (113, 22, 'Stefan', 'Kager', 'stefan.kager@ms-bogen-inactive.de', '+49 9422 9662', FALSE, 'INACTIVE_THIS_YEAR', 'GRADES_5_TO_9', 0, TRUE, NOW(), NOW()),
    (114, 30, 'Ulrich', 'Schramm', 'ulrich.schramm@ms-viechtach.de', '+49 9942 9404', TRUE, 'ACTIVE', 'GRADES_5_TO_9', -1, TRUE, NOW(), NOW()); -- Teacher with a debt of 1 credit hour

-- 8. TEACHER_SUBJECT
INSERT INTO TEACHER_SUBJECTS (id, year_id, teacher_id, subject_id, availability_status, created_at, updated_at)
VALUES
    -- User's Original Examples (IDs 1-5)
    (1, 1, 1, 1, 'AVAILABLE', NOW(), NOW()), -- Hans (Primary) teaches German (Primary)
    (2, 1, 1, 2, 'AVAILABLE', NOW(), NOW()), -- Hans (Primary) teaches Math (Primary)
    (3, 1, 2, 4, 'AVAILABLE', NOW(), NOW()), -- Anna (Primary) teaches Catholic Religion (Primary)
    (4, 1, 3, 3, 'AVAILABLE', NOW(), NOW()), -- Peter (Middle) teaches English (Middle)
    (5, 1, 4, 1, 'AVAILABLE', NOW(), NOW()), -- Julia (Primary) teaches German (Primary)

    -- Generated Assignments (IDs 6-425)

    -- Teacher 1 (GS Innstadt, PRIMARY)
    (6, 1, 1, 12, 'AVAILABLE', NOW(), NOW()), -- HSU
    (7, 1, 1, 23, 'AVAILABLE', NOW(), NOW()), -- English (Primary)
    (8, 1, 1, 24, 'AVAILABLE', NOW(), NOW()), -- SPAD (Both)

    -- Teacher 2 (GS Innstadt, PRIMARY)
    (9, 1, 2, 1, 'AVAILABLE', NOW(), NOW()), -- German
    (10, 1, 2, 2, 'AVAILABLE', NOW(), NOW()), -- Math
    (11, 1, 2, 12, 'AVAILABLE', NOW(), NOW()), -- HSU

    -- Teacher 3 (MS Salzweg, MIDDLE)
    (12, 1, 3, 21, 'AVAILABLE', NOW(), NOW()), -- German (Middle)
    (13, 1, 3, 22, 'AVAILABLE', NOW(), NOW()), -- Math (Middle)
    (14, 1, 3, 10, 'AVAILABLE', NOW(), NOW()), -- History

    -- Teacher 4 (GS Freyung, PRIMARY)
    (15, 1, 4, 2, 'AVAILABLE', NOW(), NOW()), -- Math
    (16, 1, 4, 12, 'AVAILABLE', NOW(), NOW()), -- HSU
    (17, 1, 4, 7, 'AVAILABLE', NOW(), NOW()), -- Sport

    -- Teacher 5 (GS Grubweg, PRIMARY)
    (18, 1, 5, 1, 'AVAILABLE', NOW(), NOW()),
    (19, 1, 5, 2, 'AVAILABLE', NOW(), NOW()),
    (20, 1, 5, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 6 (GS Ruderting, PRIMARY)
    (21, 1, 6, 1, 'AVAILABLE', NOW(), NOW()),
    (22, 1, 6, 23, 'AVAILABLE', NOW(), NOW()),
    (23, 1, 6, 12, 'AVAILABLE', NOW(), NOW()),
    (24, 1, 6, 25, 'AVAILABLE', NOW(), NOW()), -- GU

    -- Teacher 7 (GS Neukirchen, PRIMARY)
    (25, 1, 7, 1, 'AVAILABLE', NOW(), NOW()),
    (26, 1, 7, 2, 'AVAILABLE', NOW(), NOW()),
    (27, 1, 7, 4, 'AVAILABLE', NOW(), NOW()),
    (28, 1, 7, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 8 (GS Patriching, PRIMARY)
    (29, 1, 8, 12, 'AVAILABLE', NOW(), NOW()),
    (30, 1, 8, 5, 'AVAILABLE', NOW(), NOW()),
    (31, 1, 8, 6, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 9 (GS Vilshofen-Nord, PRIMARY)
    (32, 1, 9, 1, 'AVAILABLE', NOW(), NOW()),
    (33, 1, 9, 2, 'AVAILABLE', NOW(), NOW()),
    (34, 1, 9, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 10 (GS Ortenburg, PRIMARY)
    (35, 1, 10, 2, 'AVAILABLE', NOW(), NOW()),
    (36, 1, 10, 12, 'AVAILABLE', NOW(), NOW()),
    (37, 1, 10, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 11 (GS Eging, PRIMARY)
    (38, 1, 11, 1, 'AVAILABLE', NOW(), NOW()),
    (39, 1, 11, 2, 'AVAILABLE', NOW(), NOW()),
    (40, 1, 11, 23, 'AVAILABLE', NOW(), NOW()),
    (41, 1, 11, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 12 (GS Neuhaus, PRIMARY)
    (42, 1, 12, 1, 'AVAILABLE', NOW(), NOW()),
    (43, 1, 12, 12, 'AVAILABLE', NOW(), NOW()),
    (44, 1, 12, 20, 'AVAILABLE', NOW(), NOW()), -- SSE

    -- Teacher 13 (MS Hutthurm, MIDDLE)
    (45, 1, 13, 21, 'AVAILABLE', NOW(), NOW()),
    (46, 1, 13, 3, 'AVAILABLE', NOW(), NOW()),
    (47, 1, 13, 8, 'AVAILABLE', NOW(), NOW()), -- Social Studies (SK)
    (48, 1, 13, 15, 'AVAILABLE', NOW(), NOW()), -- PCB

    -- Teacher 14 (MS Heining, MIDDLE)
    (49, 1, 14, 21, 'AVAILABLE', NOW(), NOW()),
    (50, 1, 14, 22, 'AVAILABLE', NOW(), NOW()),
    (51, 1, 14, 3, 'AVAILABLE', NOW(), NOW()),
    (52, 1, 14, 10, 'AVAILABLE', NOW(), NOW()), -- History

    -- Teacher 15 (GS Tittling, PRIMARY)
    (53, 1, 15, 1, 'AVAILABLE', NOW(), NOW()),
    (54, 1, 15, 2, 'AVAILABLE', NOW(), NOW()),
    (55, 1, 15, 12, 'AVAILABLE', NOW(), NOW()),
    (56, 1, 15, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 16 (GS Vilshofen-Stadt, PRIMARY)
    (57, 1, 16, 1, 'AVAILABLE', NOW(), NOW()),
    (58, 1, 16, 23, 'AVAILABLE', NOW(), NOW()),
    (59, 1, 16, 5, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 17 (GS Schöllnach, PRIMARY)
    (60, 1, 17, 2, 'AVAILABLE', NOW(), NOW()),
    (61, 1, 17, 12, 'AVAILABLE', NOW(), NOW()),
    (62, 1, 17, 4, 'AVAILABLE', NOW(), NOW()),
    (63, 1, 17, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 18 (MS Grafenau, MIDDLE)
    (64, 1, 18, 22, 'AVAILABLE', NOW(), NOW()),
    (65, 1, 18, 3, 'AVAILABLE', NOW(), NOW()),
    (66, 1, 18, 15, 'AVAILABLE', NOW(), NOW()),
    (67, 1, 18, 16, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 19 (GS Aidenbach, PRIMARY)
    (68, 1, 19, 1, 'AVAILABLE', NOW(), NOW()),
    (69, 1, 19, 12, 'AVAILABLE', NOW(), NOW()),
    (70, 1, 19, 19, 'AVAILABLE', NOW(), NOW()), -- DaZ

    -- Teacher 20 (GS Osterhofen, PRIMARY)
    (71, 1, 20, 2, 'AVAILABLE', NOW(), NOW()),
    (72, 1, 20, 23, 'AVAILABLE', NOW(), NOW()),
    (73, 1, 20, 12, 'AVAILABLE', NOW(), NOW()),
    (74, 1, 20, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 21 (GS Deggendorf-Mitte, PRIMARY)
    (75, 1, 21, 1, 'AVAILABLE', NOW(), NOW()),
    (76, 1, 21, 2, 'AVAILABLE', NOW(), NOW()),
    (77, 1, 21, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 22 (GS Bad Griesbach, PRIMARY)
    (78, 1, 22, 1, 'AVAILABLE', NOW(), NOW()),
    (79, 1, 22, 23, 'AVAILABLE', NOW(), NOW()),
    (80, 1, 22, 6, 'AVAILABLE', NOW(), NOW()), -- KE
    (81, 1, 22, 25, 'AVAILABLE', NOW(), NOW()), -- GU

    -- Teacher 23 (GS Fürstenstein, PRIMARY)
    (82, 1, 23, 12, 'AVAILABLE', NOW(), NOW()),
    (83, 1, 23, 4, 'AVAILABLE', NOW(), NOW()),
    (84, 1, 23, 7, 'AVAILABLE', NOW(), NOW()),
    (85, 1, 23, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 24 (GS Röhrnbach, PRIMARY)
    (86, 1, 24, 1, 'AVAILABLE', NOW(), NOW()),
    (87, 1, 24, 2, 'AVAILABLE', NOW(), NOW()),
    (88, 1, 24, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 25 (GS Kellberg, PRIMARY)
    (89, 1, 25, 2, 'AVAILABLE', NOW(), NOW()),
    (90, 1, 25, 23, 'AVAILABLE', NOW(), NOW()),
    (91, 1, 25, 12, 'AVAILABLE', NOW(), NOW()),
    (92, 1, 25, 20, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 26 (GS Witzmannsberg, PRIMARY)
    (93, 1, 26, 1, 'AVAILABLE', NOW(), NOW()),
    (94, 1, 26, 2, 'AVAILABLE', NOW(), NOW()),
    (95, 1, 26, 5, 'AVAILABLE', NOW(), NOW()),
    (96, 1, 26, 6, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 27 (MS Salzweg, MIDDLE)
    (97, 1, 27, 21, 'AVAILABLE', NOW(), NOW()),
    (98, 1, 27, 3, 'AVAILABLE', NOW(), NOW()),
    (99, 1, 27, 8, 'AVAILABLE', NOW(), NOW()),
    (100, 1, 27, 11, 'AVAILABLE', NOW(), NOW()), -- Geography (GEO)

    -- Teacher 28 (MS Hutthurm, MIDDLE)
    (101, 1, 28, 22, 'AVAILABLE', NOW(), NOW()),
    (102, 1, 28, 3, 'AVAILABLE', NOW(), NOW()),
    (103, 1, 28, 13, 'AVAILABLE', NOW(), NOW()), -- AL
    (104, 1, 28, 14, 'AVAILABLE', NOW(), NOW()), -- WiB

    -- Teacher 29 (MS Hauzenberg, MIDDLE)
    (105, 1, 29, 21, 'AVAILABLE', NOW(), NOW()),
    (106, 1, 29, 3, 'AVAILABLE', NOW(), NOW()),
    (107, 1, 29, 10, 'AVAILABLE', NOW(), NOW()),
    (108, 1, 29, 17, 'AVAILABLE', NOW(), NOW()), -- GSE

    -- Teacher 30 (MS Grafenau, MIDDLE)
    (109, 1, 30, 22, 'AVAILABLE', NOW(), NOW()),
    (110, 1, 30, 3, 'AVAILABLE', NOW(), NOW()),
    (111, 1, 30, 15, 'AVAILABLE', NOW(), NOW()),
    (112, 1, 30, 16, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 31 (MS Bogen, MIDDLE)
    (113, 1, 31, 21, 'AVAILABLE', NOW(), NOW()),
    (114, 1, 31, 22, 'AVAILABLE', NOW(), NOW()),
    (115, 1, 31, 3, 'AVAILABLE', NOW(), NOW()),
    (116, 1, 31, 9, 'AVAILABLE', NOW(), NOW()), -- PuG

    -- Teacher 32 (GS Freyung, PRIMARY)
    (117, 1, 32, 1, 'AVAILABLE', NOW(), NOW()),
    (118, 1, 32, 2, 'AVAILABLE', NOW(), NOW()),
    (119, 1, 32, 12, 'AVAILABLE', NOW(), NOW()),
    (120, 1, 32, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 33 (GS Cham-West, PRIMARY)
    (121, 1, 33, 12, 'AVAILABLE', NOW(), NOW()),
    (122, 1, 33, 1, 'AVAILABLE', NOW(), NOW()),
    (123, 1, 33, 23, 'AVAILABLE', NOW(), NOW()),
    (124, 1, 33, 19, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 34 (GS Zwiesel, PRIMARY)
    (125, 1, 34, 1, 'AVAILABLE', NOW(), NOW()),
    (126, 1, 34, 2, 'AVAILABLE', NOW(), NOW()),
    (127, 1, 34, 12, 'AVAILABLE', NOW(), NOW()),
    (128, 1, 34, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 35 (GS Bad Birnbach, PRIMARY)
    (129, 1, 35, 2, 'AVAILABLE', NOW(), NOW()),
    (130, 1, 35, 12, 'AVAILABLE', NOW(), NOW()),
    (131, 1, 35, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 36 (GS Landau, PRIMARY)
    (132, 1, 36, 1, 'AVAILABLE', NOW(), NOW()),
    (133, 1, 36, 2, 'AVAILABLE', NOW(), NOW()),
    (134, 1, 36, 23, 'AVAILABLE', NOW(), NOW()),
    (135, 1, 36, 5, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 37 (GS Straubing-Süd, PRIMARY)
    (136, 1, 37, 1, 'AVAILABLE', NOW(), NOW()),
    (137, 1, 37, 2, 'AVAILABLE', NOW(), NOW()),
    (138, 1, 37, 12, 'AVAILABLE', NOW(), NOW()),
    (139, 1, 37, 6, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 38 (GS Pocking, PRIMARY)
    (140, 1, 38, 2, 'AVAILABLE', NOW(), NOW()),
    (141, 1, 38, 12, 'AVAILABLE', NOW(), NOW()),
    (142, 1, 38, 4, 'AVAILABLE', NOW(), NOW()),
    (143, 1, 38, 20, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 39 (GS Altenmarkt, PRIMARY)
    (144, 1, 39, 1, 'AVAILABLE', NOW(), NOW()),
    (145, 1, 39, 12, 'AVAILABLE', NOW(), NOW()),
    (146, 1, 39, 23, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 40 (GS Regen, PRIMARY)
    (147, 1, 40, 1, 'AVAILABLE', NOW(), NOW()),
    (148, 1, 40, 2, 'AVAILABLE', NOW(), NOW()),
    (149, 1, 40, 12, 'AVAILABLE', NOW(), NOW()),
    (150, 1, 40, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 41 (GS Bad Füssing, PRIMARY)
    (151, 1, 41, 1, 'AVAILABLE', NOW(), NOW()),
    (152, 1, 41, 2, 'AVAILABLE', NOW(), NOW()),
    (153, 1, 41, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 42 (GS Simbach am Inn, PRIMARY)
    (154, 1, 42, 2, 'AVAILABLE', NOW(), NOW()),
    (155, 1, 42, 23, 'AVAILABLE', NOW(), NOW()),
    (156, 1, 42, 12, 'AVAILABLE', NOW(), NOW()),
    (157, 1, 42, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 43 (GS Dingolfing-Nord, PRIMARY)
    (158, 1, 43, 1, 'AVAILABLE', NOW(), NOW()),
    (159, 1, 43, 12, 'AVAILABLE', NOW(), NOW()),
    (160, 1, 43, 19, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 44 (GS Deggendorf-Ost, PRIMARY)
    (161, 1, 44, 1, 'AVAILABLE', NOW(), NOW()),
    (162, 1, 44, 2, 'AVAILABLE', NOW(), NOW()),
    (163, 1, 44, 12, 'AVAILABLE', NOW(), NOW()),
    (164, 1, 44, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 45 (GS Viechtach-Süd, PRIMARY)
    (165, 1, 45, 2, 'AVAILABLE', NOW(), NOW()),
    (166, 1, 45, 12, 'AVAILABLE', NOW(), NOW()),
    (167, 1, 45, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 46 (GS Mühldorf, PRIMARY)
    (168, 1, 46, 1, 'AVAILABLE', NOW(), NOW()),
    (169, 1, 46, 2, 'AVAILABLE', NOW(), NOW()),
    (170, 1, 46, 12, 'AVAILABLE', NOW(), NOW()),
    (171, 1, 46, 5, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 47 (GS Vilshofen-Land, PRIMARY)
    (172, 1, 47, 1, 'AVAILABLE', NOW(), NOW()),
    (173, 1, 47, 23, 'AVAILABLE', NOW(), NOW()),
    (174, 1, 47, 6, 'AVAILABLE', NOW(), NOW()),
    (175, 1, 47, 25, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 48 (GS Zwiesel-Nord, PRIMARY)
    (176, 1, 48, 2, 'AVAILABLE', NOW(), NOW()),
    (177, 1, 48, 12, 'AVAILABLE', NOW(), NOW()),
    (178, 1, 48, 4, 'AVAILABLE', NOW(), NOW()),
    (179, 1, 48, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 49 (GS Roding, PRIMARY)
    (180, 1, 49, 1, 'AVAILABLE', NOW(), NOW()),
    (181, 1, 49, 12, 'AVAILABLE', NOW(), NOW()),
    (182, 1, 49, 20, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 50 (GS Arnstorf, PRIMARY)
    (183, 1, 50, 1, 'AVAILABLE', NOW(), NOW()),
    (184, 1, 50, 2, 'AVAILABLE', NOW(), NOW()),
    (185, 1, 50, 12, 'AVAILABLE', NOW(), NOW()),
    (186, 1, 50, 23, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 51 (GS Mainburg, PRIMARY)
    (187, 1, 51, 1, 'AVAILABLE', NOW(), NOW()),
    (188, 1, 51, 2, 'AVAILABLE', NOW(), NOW()),
    (189, 1, 51, 12, 'AVAILABLE', NOW(), NOW()),
    (190, 1, 51, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 52 (GS Simbach bei Landau, PRIMARY)
    (191, 1, 52, 2, 'AVAILABLE', NOW(), NOW()),
    (192, 1, 52, 12, 'AVAILABLE', NOW(), NOW()),
    (193, 1, 52, 4, 'AVAILABLE', NOW(), NOW()),
    (194, 1, 52, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 53 (GS Bad Kötzting, PRIMARY)
    (195, 1, 53, 1, 'AVAILABLE', NOW(), NOW()),
    (196, 1, 53, 23, 'AVAILABLE', NOW(), NOW()),
    (197, 1, 53, 12, 'AVAILABLE', NOW(), NOW()),
    (198, 1, 53, 5, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 54 (MS Viechtach, MIDDLE)
    (199, 1, 54, 21, 'AVAILABLE', NOW(), NOW()),
    (200, 1, 54, 3, 'AVAILABLE', NOW(), NOW()),
    (201, 1, 54, 10, 'AVAILABLE', NOW(), NOW()),
    (202, 1, 54, 17, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 55 (MS Bodenmais, MIDDLE)
    (203, 1, 55, 22, 'AVAILABLE', NOW(), NOW()),
    (204, 1, 55, 3, 'AVAILABLE', NOW(), NOW()),
    (205, 1, 55, 15, 'AVAILABLE', NOW(), NOW()),
    (206, 1, 55, 16, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 56 (MS Pfarrkirchen, MIDDLE)
    (207, 1, 56, 21, 'AVAILABLE', NOW(), NOW()),
    (208, 1, 56, 22, 'AVAILABLE', NOW(), NOW()),
    (209, 1, 56, 8, 'AVAILABLE', NOW(), NOW()),
    (210, 1, 56, 11, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 57 (MS Plattling, MIDDLE)
    (211, 1, 57, 21, 'AVAILABLE', NOW(), NOW()),
    (212, 1, 57, 3, 'AVAILABLE', NOW(), NOW()),
    (213, 1, 57, 13, 'AVAILABLE', NOW(), NOW()),
    (214, 1, 57, 14, 'AVAILABLE', NOW(), NOW()),
    
    -- Teacher 58 (GS Innstadt, PRIMARY)
    (215, 1, 58, 1, 'AVAILABLE', NOW(), NOW()),
    (216, 1, 58, 2, 'AVAILABLE', NOW(), NOW()),
    (217, 1, 58, 12, 'AVAILABLE', NOW(), NOW()),
    
    -- Teacher 59 (MS Salzweg, MIDDLE)
    (218, 1, 59, 21, 'AVAILABLE', NOW(), NOW()),
    (219, 1, 59, 3, 'AVAILABLE', NOW(), NOW()),
    (220, 1, 59, 8, 'AVAILABLE', NOW(), NOW()),
    (221, 1, 59, 10, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 60 (GS Grubweg, PRIMARY)
    (222, 1, 60, 1, 'AVAILABLE', NOW(), NOW()),
    (223, 1, 60, 23, 'AVAILABLE', NOW(), NOW()),
    (224, 1, 60, 12, 'AVAILABLE', NOW(), NOW()),
    (225, 1, 60, 19, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 61 (MS Hutthurm, MIDDLE)
    (226, 1, 61, 22, 'AVAILABLE', NOW(), NOW()),
    (227, 1, 61, 3, 'AVAILABLE', NOW(), NOW()),
    (228, 1, 61, 15, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 62 (GS Neukirchen, PRIMARY)
    (229, 1, 62, 12, 'AVAILABLE', NOW(), NOW()),
    (230, 1, 62, 5, 'AVAILABLE', NOW(), NOW()),
    (231, 1, 62, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 63 (MS Hutthurm, MIDDLE)
    (232, 1, 63, 21, 'AVAILABLE', NOW(), NOW()),
    (233, 1, 63, 22, 'AVAILABLE', NOW(), NOW()),
    (234, 1, 63, 17, 'AVAILABLE', NOW(), NOW()),
    (235, 1, 63, 18, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 64 (GS Vilshofen-Nord, PRIMARY)
    (236, 1, 64, 2, 'AVAILABLE', NOW(), NOW()),
    (237, 1, 64, 12, 'AVAILABLE', NOW(), NOW()),
    (238, 1, 64, 4, 'AVAILABLE', NOW(), NOW()),
    (239, 1, 64, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 65 (MS Heining, MIDDLE)
    (240, 1, 65, 3, 'AVAILABLE', NOW(), NOW()),
    (241, 1, 65, 22, 'AVAILABLE', NOW(), NOW()),
    (242, 1, 65, 13, 'AVAILABLE', NOW(), NOW()),
    (243, 1, 65, 16, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 66 (GS Tittling, PRIMARY)
    (244, 1, 66, 1, 'AVAILABLE', NOW(), NOW()),
    (245, 1, 66, 2, 'AVAILABLE', NOW(), NOW()),
    (246, 1, 66, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 67 (GS Vilshofen-Stadt, PRIMARY)
    (247, 1, 67, 1, 'AVAILABLE', NOW(), NOW()),
    (248, 1, 67, 23, 'AVAILABLE', NOW(), NOW()),
    (249, 1, 67, 6, 'AVAILABLE', NOW(), NOW()),
    (250, 1, 67, 25, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 68 (MS Grafenau, MIDDLE)
    (251, 1, 68, 21, 'AVAILABLE', NOW(), NOW()),
    (252, 1, 68, 3, 'AVAILABLE', NOW(), NOW()),
    (253, 1, 68, 8, 'AVAILABLE', NOW(), NOW()),
    (254, 1, 68, 10, 'AVAILABLE', NOW(), NOW()),
    (255, 1, 68, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 69 (GS Osterhofen, PRIMARY)
    (256, 1, 69, 2, 'AVAILABLE', NOW(), NOW()),
    (257, 1, 69, 12, 'AVAILABLE', NOW(), NOW()),
    (258, 1, 69, 4, 'AVAILABLE', NOW(), NOW()),
    (259, 1, 69, 23, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 70 (MS Bogen, MIDDLE)
    (260, 1, 70, 22, 'AVAILABLE', NOW(), NOW()),
    (261, 1, 70, 3, 'AVAILABLE', NOW(), NOW()),
    (262, 1, 70, 15, 'AVAILABLE', NOW(), NOW()),
    (263, 1, 70, 16, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 71 (GS Bad Griesbach, PRIMARY)
    (264, 1, 71, 1, 'AVAILABLE', NOW(), NOW()),
    (265, 1, 71, 12, 'AVAILABLE', NOW(), NOW()),
    (266, 1, 71, 20, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 72 (GS Röhrnbach, PRIMARY)
    (267, 1, 72, 1, 'AVAILABLE', NOW(), NOW()),
    (268, 1, 72, 2, 'AVAILABLE', NOW(), NOW()),
    (269, 1, 72, 12, 'AVAILABLE', NOW(), NOW()),
    (270, 1, 72, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 73 (GS Witzmannsberg, PRIMARY)
    (271, 1, 73, 1, 'AVAILABLE', NOW(), NOW()),
    (272, 1, 73, 23, 'AVAILABLE', NOW(), NOW()),
    (273, 1, 73, 6, 'AVAILABLE', NOW(), NOW()),
    (274, 1, 73, 25, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 74 (MS Viechtach, MIDDLE)
    (275, 1, 74, 21, 'AVAILABLE', NOW(), NOW()),
    (276, 1, 74, 22, 'AVAILABLE', NOW(), NOW()),
    (277, 1, 74, 17, 'AVAILABLE', NOW(), NOW()),
    (278, 1, 74, 18, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 75 (GS Bad Birnbach, PRIMARY)
    (279, 1, 75, 1, 'AVAILABLE', NOW(), NOW()),
    (280, 1, 75, 23, 'AVAILABLE', NOW(), NOW()),
    (281, 1, 75, 12, 'AVAILABLE', NOW(), NOW()),
    (282, 1, 75, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 76 (GS Landau, PRIMARY)
    (283, 1, 76, 2, 'AVAILABLE', NOW(), NOW()),
    (284, 1, 76, 12, 'AVAILABLE', NOW(), NOW()),
    (285, 1, 76, 20, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 77 (GS Pocking, PRIMARY)
    (286, 1, 77, 1, 'AVAILABLE', NOW(), NOW()),
    (287, 1, 77, 2, 'AVAILABLE', NOW(), NOW()),
    (288, 1, 77, 12, 'AVAILABLE', NOW(), NOW()),
    (289, 1, 77, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 78 (GS Altenmarkt, PRIMARY)
    (290, 1, 78, 1, 'AVAILABLE', NOW(), NOW()),
    (291, 1, 78, 23, 'AVAILABLE', NOW(), NOW()),
    (292, 1, 78, 5, 'AVAILABLE', NOW(), NOW()),
    (293, 1, 78, 6, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 79 (GS Bad Füssing, PRIMARY)
    (294, 1, 79, 2, 'AVAILABLE', NOW(), NOW()),
    (295, 1, 79, 12, 'AVAILABLE', NOW(), NOW()),
    (296, 1, 79, 7, 'AVAILABLE', NOW(), NOW()),
    (297, 1, 79, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 80 (MS Plattling, MIDDLE)
    (298, 1, 80, 21, 'AVAILABLE', NOW(), NOW()),
    (299, 1, 80, 3, 'AVAILABLE', NOW(), NOW()),
    (300, 1, 80, 13, 'AVAILABLE', NOW(), NOW()),
    (301, 1, 80, 14, 'AVAILABLE', NOW(), NOW()),
    (302, 1, 80, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 81 (GS Deggendorf-Ost, PRIMARY)
    (303, 1, 81, 12, 'AVAILABLE', NOW(), NOW()),
    (304, 1, 81, 1, 'AVAILABLE', NOW(), NOW()),
    (305, 1, 81, 19, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 82 (GS Mühldorf, PRIMARY)
    (306, 1, 82, 23, 'AVAILABLE', NOW(), NOW()),
    (307, 1, 82, 12, 'AVAILABLE', NOW(), NOW()),
    (308, 1, 82, 5, 'AVAILABLE', NOW(), NOW()),
    (309, 1, 82, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 83 (GS Zwiesel-Nord, PRIMARY)
    (310, 1, 83, 2, 'AVAILABLE', NOW(), NOW()),
    (311, 1, 83, 12, 'AVAILABLE', NOW(), NOW()),
    (312, 1, 83, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 84 (GS Arnstorf, PRIMARY)
    (313, 1, 84, 1, 'AVAILABLE', NOW(), NOW()),
    (314, 1, 84, 2, 'AVAILABLE', NOW(), NOW()),
    (315, 1, 84, 12, 'AVAILABLE', NOW(), NOW()),
    (316, 1, 84, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 85 (GS Simbach bei Landau, PRIMARY)
    (317, 1, 85, 1, 'AVAILABLE', NOW(), NOW()),
    (318, 1, 85, 23, 'AVAILABLE', NOW(), NOW()),
    (319, 1, 85, 6, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 86 (GS Freyung, PRIMARY)
    (320, 1, 86, 2, 'AVAILABLE', NOW(), NOW()),
    (321, 1, 86, 12, 'AVAILABLE', NOW(), NOW()),
    (322, 1, 86, 4, 'AVAILABLE', NOW(), NOW()),
    (323, 1, 86, 23, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 87 (MS Hutthurm, MIDDLE)
    (324, 1, 87, 21, 'AVAILABLE', NOW(), NOW()),
    (325, 1, 87, 3, 'AVAILABLE', NOW(), NOW()),
    (326, 1, 87, 8, 'AVAILABLE', NOW(), NOW()),
    (327, 1, 87, 10, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 88 (GS Neukirchen, PRIMARY)
    (328, 1, 88, 1, 'AVAILABLE', NOW(), NOW()),
    (329, 1, 88, 12, 'AVAILABLE', NOW(), NOW()),
    (330, 1, 88, 20, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 89 (GS Patriching, PRIMARY)
    (331, 1, 89, 1, 'AVAILABLE', NOW(), NOW()),
    (332, 1, 89, 2, 'AVAILABLE', NOW(), NOW()),
    (333, 1, 89, 12, 'AVAILABLE', NOW(), NOW()),
    (334, 1, 89, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 90 (GS Ortenburg, PRIMARY)
    (335, 1, 90, 1, 'AVAILABLE', NOW(), NOW()),
    (336, 1, 90, 23, 'AVAILABLE', NOW(), NOW()),
    (337, 1, 90, 5, 'AVAILABLE', NOW(), NOW()),
    (338, 1, 90, 6, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 91 (GS Eging, PRIMARY)
    (339, 1, 91, 2, 'AVAILABLE', NOW(), NOW()),
    (340, 1, 91, 12, 'AVAILABLE', NOW(), NOW()),
    (341, 1, 91, 7, 'AVAILABLE', NOW(), NOW()),
    (342, 1, 91, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 92 (MS Hauzenberg, MIDDLE)
    (343, 1, 92, 22, 'AVAILABLE', NOW(), NOW()),
    (344, 1, 92, 3, 'AVAILABLE', NOW(), NOW()),
    (345, 1, 92, 15, 'AVAILABLE', NOW(), NOW()),
    (346, 1, 92, 16, 'AVAILABLE', NOW(), NOW()),
    (347, 1, 92, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 93 (GS Schöllnach, PRIMARY)
    (348, 1, 93, 12, 'AVAILABLE', NOW(), NOW()),
    (349, 1, 93, 1, 'AVAILABLE', NOW(), NOW()),
    (350, 1, 93, 19, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 94 (GS Aidenbach, PRIMARY)
    (351, 1, 94, 23, 'AVAILABLE', NOW(), NOW()),
    (352, 1, 94, 12, 'AVAILABLE', NOW(), NOW()),
    (353, 1, 94, 5, 'AVAILABLE', NOW(), NOW()),
    (354, 1, 94, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 95 (GS Waldkirchen, PRIMARY)
    (355, 1, 95, 2, 'AVAILABLE', NOW(), NOW()),
    (356, 1, 95, 12, 'AVAILABLE', NOW(), NOW()),
    (357, 1, 95, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 96 (GS Deggendorf-Mitte, PRIMARY)
    (358, 1, 96, 1, 'AVAILABLE', NOW(), NOW()),
    (359, 1, 96, 2, 'AVAILABLE', NOW(), NOW()),
    (360, 1, 96, 12, 'AVAILABLE', NOW(), NOW()),
    (361, 1, 96, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 97 (GS Fürstenstein, PRIMARY)
    (362, 1, 97, 1, 'AVAILABLE', NOW(), NOW()),
    (363, 1, 97, 23, 'AVAILABLE', NOW(), NOW()),
    (364, 1, 97, 6, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 98 (GS Kellberg, PRIMARY)
    (365, 1, 98, 2, 'AVAILABLE', NOW(), NOW()),
    (366, 1, 98, 12, 'AVAILABLE', NOW(), NOW()),
    (367, 1, 98, 4, 'AVAILABLE', NOW(), NOW()),
    (368, 1, 98, 23, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 99 (GS Cham-West, PRIMARY)
    (369, 1, 99, 1, 'AVAILABLE', NOW(), NOW()),
    (370, 1, 99, 12, 'AVAILABLE', NOW(), NOW()),
    (371, 1, 99, 20, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 100 (GS Zwiesel, PRIMARY)
    (372, 1, 100, 1, 'AVAILABLE', NOW(), NOW()),
    (373, 1, 100, 2, 'AVAILABLE', NOW(), NOW()),
    (374, 1, 100, 12, 'AVAILABLE', NOW(), NOW()),
    (375, 1, 100, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 101 (MS Bodenmais, MIDDLE)
    (376, 1, 101, 22, 'AVAILABLE', NOW(), NOW()),
    (377, 1, 101, 3, 'AVAILABLE', NOW(), NOW()),
    (378, 1, 101, 17, 'AVAILABLE', NOW(), NOW()),
    (379, 1, 101, 18, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 102 (GS Straubing-Süd, PRIMARY)
    (380, 1, 102, 1, 'AVAILABLE', NOW(), NOW()),
    (381, 1, 102, 23, 'AVAILABLE', NOW(), NOW()),
    (382, 1, 102, 6, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 103 (MS Pfarrkirchen, MIDDLE)
    (383, 1, 103, 21, 'AVAILABLE', NOW(), NOW()),
    (384, 1, 103, 3, 'AVAILABLE', NOW(), NOW()),
    (385, 1, 103, 13, 'AVAILABLE', NOW(), NOW()),
    (386, 1, 103, 14, 'AVAILABLE', NOW(), NOW()),
    (387, 1, 103, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 104 (GS Regen, PRIMARY)
    (388, 1, 104, 2, 'AVAILABLE', NOW(), NOW()),
    (389, 1, 104, 12, 'AVAILABLE', NOW(), NOW()),
    (390, 1, 104, 4, 'AVAILABLE', NOW(), NOW()),
    (391, 1, 104, 23, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 105 (GS Simbach am Inn, PRIMARY)
    (392, 1, 105, 1, 'AVAILABLE', NOW(), NOW()),
    (393, 1, 105, 12, 'AVAILABLE', NOW(), NOW()),
    (394, 1, 105, 20, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 106 (GS Dingolfing-Nord, PRIMARY)
    (395, 1, 106, 1, 'AVAILABLE', NOW(), NOW()),
    (396, 1, 106, 2, 'AVAILABLE', NOW(), NOW()),
    (397, 1, 106, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 107 (GS Viechtach-Süd, PRIMARY)
    (398, 1, 107, 1, 'AVAILABLE', NOW(), NOW()),
    (399, 1, 107, 23, 'AVAILABLE', NOW(), NOW()),
    (400, 1, 107, 6, 'AVAILABLE', NOW(), NOW()),
    (401, 1, 107, 25, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 108 (GS Vilshofen-Land, PRIMARY)
    (402, 1, 108, 2, 'AVAILABLE', NOW(), NOW()),
    (403, 1, 108, 12, 'AVAILABLE', NOW(), NOW()),
    (404, 1, 108, 4, 'AVAILABLE', NOW(), NOW()),
    (405, 1, 108, 23, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 109 (GS Roding, PRIMARY)
    (406, 1, 109, 1, 'AVAILABLE', NOW(), NOW()),
    (407, 1, 109, 12, 'AVAILABLE', NOW(), NOW()),
    (408, 1, 109, 20, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 110 (GS Mainburg, PRIMARY)
    (409, 1, 110, 1, 'AVAILABLE', NOW(), NOW()),
    (410, 1, 110, 2, 'AVAILABLE', NOW(), NOW()),
    (411, 1, 110, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 111 (GS Bad Kötzting, PRIMARY)
    (412, 1, 111, 1, 'AVAILABLE', NOW(), NOW()),
    (413, 1, 111, 23, 'AVAILABLE', NOW(), NOW()),
    (414, 1, 111, 6, 'AVAILABLE', NOW(), NOW()),
    (415, 1, 111, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 112 (Hanna Reischl, GS Innstadt, PRIMARY) - INACTIVE_THIS_YEAR
    (416, 1, 112, 1, 'UNAVAILABLE_THIS_YEAR', NOW(), NOW()),
    (417, 1, 112, 2, 'UNAVAILABLE_THIS_YEAR', NOW(), NOW()),
    (418, 1, 112, 12, 'UNAVAILABLE_THIS_YEAR', NOW(), NOW()),
    (419, 1, 112, 23, 'UNAVAILABLE_THIS_YEAR', NOW(), NOW()),

    -- Teacher 113 (Stefan Kager, MS Bogen, MIDDLE) - INACTIVE_THIS_YEAR
    (420, 1, 113, 21, 'UNAVAILABLE_THIS_YEAR', NOW(), NOW()),
    (421, 1, 113, 3, 'UNAVAILABLE_THIS_YEAR', NOW(), NOW()),
    (422, 1, 113, 10, 'UNAVAILABLE_THIS_YEAR', NOW(), NOW()),
    (423, 1, 113, 17, 'UNAVAILABLE_THIS_YEAR', NOW(), NOW()),

    -- Teacher 114 (Ulrich Schramm, MS Viechtach, MIDDLE) - ACTIVE, with Debt
    (424, 1, 114, 21, 'AVAILABLE', NOW(), NOW()),
    (425, 1, 114, 3, 'AVAILABLE', NOW(), NOW()),
    (426, 1, 114, 8, 'AVAILABLE', NOW(), NOW());

-- 9. TEACHER_AVAILABILITY
INSERT INTO TEACHER_AVAILABILITY (id, teacher_id, academic_year_id, internship_type_id, status, is_available, preference_rank, notes, created_at)
VALUES
    -- T1 (Zone 1, Full-Time, GS Passau-Innstadt)
    (1, 1, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 limit to Wednesday', NOW()),
    (2, 1, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 limit to Wednesday', NOW()),
    (3, 1, 1, 3, 'PREFERRED', TRUE, 1, 'Prefers Wednesday Winter', NOW()),
    (4, 1, 1, 4, 'AVAILABLE', TRUE, 2, 'Prefers Wednesday Summer', NOW()),

    -- T2 (Zone 1, Part-Time, GS Passau-Innstadt)
    (5, 2, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Cannot do block internship due to part-time/zone 1', NOW()),
    (6, 2, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Cannot do block internship due to part-time/zone 1', NOW()),
    (7, 2, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time constraint aligns with ZSP/SFP', NOW()),
    (8, 2, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time constraint aligns with ZSP/SFP', NOW()),

    -- T3 (Zone 2, Full-Time, MS Salzweg)
    (9, 3, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible. Prefers winter block', NOW()),
    (10, 3, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (11, 3, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (12, 3, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T4 (Zone 3, Full-Time, GS Freyung)
    (13, 4, 1, 1, 'PREFERRED', TRUE, 1, 'Live in Zone 3, prefers Block', NOW()),
    (14, 4, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (15, 4, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 limit to Block', NOW()),
    (16, 4, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 limit to Block', NOW()),

    -- T5 (Zone 1, Full-Time, GS Grubweg)
    (17, 5, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (18, 5, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (19, 5, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (20, 5, 1, 4, 'AVAILABLE', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T6 (Zone 1, Part-Time, GS Ruderting)
    (21, 6, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1/Part-time constraint', NOW()),
    (22, 6, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1/Part-time constraint', NOW()),
    (23, 6, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (24, 6, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T7 (Zone 1, Full-Time, GS Neukirchen v. W.)
    (25, 7, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (26, 7, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (27, 7, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (28, 7, 1, 4, 'AVAILABLE', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T8 (Zone 1, Part-Time, GS Patriching)
    (29, 8, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1/Part-time constraint', NOW()),
    (30, 8, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1/Part-time constraint', NOW()),
    (31, 8, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (32, 8, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T9 (Zone 1, Full-Time, GS Vilshofen-Nord)
    (33, 9, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (34, 9, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (35, 9, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (36, 9, 1, 4, 'PREFERRED', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T10 (Zone 1, Full-Time, GS Ortenburg)
    (37, 10, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (38, 10, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (39, 10, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (40, 10, 1, 4, 'AVAILABLE', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T11 (Zone 1, Full-Time, GS Eging am See)
    (41, 11, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (42, 11, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (43, 11, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (44, 11, 1, 4, 'PREFERRED', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T12 (Zone 1, Part-Time, GS Neuhaus am Inn)
    (45, 12, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1/Part-time constraint', NOW()),
    (46, 12, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1/Part-time constraint', NOW()),
    (47, 12, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (48, 12, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T13 (Zone 1, Full-Time, MS Hutthurm)
    (49, 13, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (50, 13, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (51, 13, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (52, 13, 1, 4, 'AVAILABLE', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T14 (Zone 2, Full-Time, MS Heining)
    (53, 14, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (54, 14, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (55, 14, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (56, 14, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T15 (Zone 2, Full-Time, MS Hauzenberg)
    (57, 15, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (58, 15, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (59, 15, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (60, 15, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T16 (Zone 2, Part-Time, GS Vilshofen-Stadt)
    (61, 16, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (62, 16, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (63, 16, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (64, 16, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T17 (Zone 2, Full-Time, GS Schöllnach)
    (65, 17, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (66, 17, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (67, 17, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (68, 17, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T18 (Zone 2, Full-Time, MS Grafenau)
    (69, 18, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (70, 18, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (71, 18, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (72, 18, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T19 (Zone 2, Full-Time, GS Aidenbach)
    (73, 19, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (74, 19, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (75, 19, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (76, 19, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T20 (Zone 2, Full-Time, GS Osterhofen)
    (77, 20, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (78, 20, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (79, 20, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (80, 20, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T21 (Zone 2, Full-Time, GS Waldkirchen)
    (81, 21, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (82, 21, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (83, 21, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (84, 21, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T22 (Zone 2, Full-Time, GS Bad Griesbach)
    (85, 22, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (86, 22, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (87, 22, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (88, 22, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T23 (Zone 2, Part-Time, GS Fürstenstein)
    (89, 23, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (90, 23, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (91, 23, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (92, 23, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T24 (Zone 2, Full-Time, GS Röhrnbach)
    (93, 24, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (94, 24, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (95, 24, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (96, 24, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T25 (Zone 2, Full-Time, GS Kellberg)
    (97, 25, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (98, 25, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (99, 25, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (100, 25, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T26 (Zone 2, Part-Time, GS Witzmannsberg)
    (101, 26, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (102, 26, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (103, 26, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (104, 26, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T27 (Zone 2, Full-Time, MS Salzweg)
    (105, 27, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (106, 27, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (107, 27, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (108, 27, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T28 (Zone 2, Full-Time, MS Hutthurm)
    (109, 28, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (110, 28, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (111, 28, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (112, 28, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T29 (Zone 2, Full-Time, MS Hauzenberg)
    (113, 29, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (114, 29, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (115, 29, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (116, 29, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T30 (Zone 2, Part-Time, MS Grafenau)
    (117, 30, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (118, 30, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (119, 30, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (120, 30, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T31 (Zone 2, Full-Time, MS Bogen)
    (121, 31, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (122, 31, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (123, 31, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (124, 31, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T32 (Zone 3, Full-Time, GS Freyung)
    (125, 32, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (126, 32, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (127, 32, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (128, 32, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T33 (Zone 3, Part-Time, GS Cham-West)
    (129, 33, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred (Part-time constraint overruled by Zone)', NOW()),
    (130, 33, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (131, 33, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (132, 33, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T34 (Zone 3, Full-Time, GS Zwiesel)
    (133, 34, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (134, 34, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (135, 34, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (136, 34, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T35 (Zone 3, Full-Time, GS Bad Birnbach)
    (137, 35, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (138, 35, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (139, 35, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (140, 35, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T36 (Zone 3, Part-Time, GS Landau an der Isar)
    (141, 36, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred (Part-time constraint overruled by Zone)', NOW()),
    (142, 36, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (143, 36, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (144, 36, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T37 (Zone 3, Full-Time, MS Pfarrkirchen)
    (145, 37, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (146, 37, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (147, 37, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (148, 37, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T38 (Zone 3, Full-Time, GS Osterhofen-Altenmarkt)
    (149, 38, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (150, 38, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (151, 38, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (152, 38, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T39 (Zone 3, Full-Time, GS Regen)
    (153, 39, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (154, 39, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (155, 39, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (156, 39, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T40 (Zone 3, Part-Time, GS Bad Füssing)
    (157, 40, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred (Part-time constraint overruled by Zone)', NOW()),
    (158, 40, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (159, 40, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (160, 40, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T41 (Zone 3, Full-Time, GS Simbach am Inn)
    (161, 41, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (162, 41, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (163, 41, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (164, 41, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T42 (Zone 3, Full-Time, MS Plattling)
    (165, 42, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (166, 42, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (167, 42, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (168, 42, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T43 (Zone 3, Full-Time, GS Dingolfing-Nord)
    (169, 43, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (170, 43, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (171, 43, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (172, 43, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T44 (Zone 3, Part-Time, GS Deggendorf-Ost)
    (173, 44, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (174, 44, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (175, 44, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (176, 44, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T45 (Zone 3, Full-Time, GS Viechtach-Süd)
    (177, 45, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (178, 45, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (179, 45, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (180, 45, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T46 (Zone 3, Full-Time, GS Mühldorf am Inn)
    (181, 46, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (182, 46, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (183, 46, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (184, 46, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T47 (Zone 3, Full-Time, GS Vilshofen-Land)
    (185, 47, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (186, 47, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (187, 47, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (188, 47, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T48 (Zone 3, Part-Time, GS Zwiesel-Nord)
    (189, 48, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (190, 48, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (191, 48, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (192, 48, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T49 (Zone 3, Full-Time, GS Roding)
    (193, 49, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (194, 49, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (195, 49, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (196, 49, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T50 (Zone 3, Full-Time, GS Arnstorf)
    (197, 50, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (198, 50, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (199, 50, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (200, 50, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T51 (Zone 3, Part-Time, GS Mainburg)
    (201, 51, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (202, 51, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (203, 51, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (204, 51, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T52 (Zone 3, Full-Time, GS Simbach bei Landau)
    (205, 52, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (206, 52, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (207, 52, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (208, 52, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T53 (Zone 3, Full-Time, GS Bad Kötzting)
    (209, 53, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (210, 53, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (211, 53, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (212, 53, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T54 (Zone 3, Full-Time, MS Viechtach)
    (213, 54, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (214, 54, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (215, 54, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (216, 54, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T55 (Zone 3, Part-Time, MS Bodenmais)
    (217, 55, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (218, 55, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (219, 55, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (220, 55, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T56 (Zone 3, Full-Time, MS Pfarrkirchen)
    (221, 56, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (222, 56, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (223, 56, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (224, 56, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T57 (Zone 3, Full-Time, MS Plattling)
    (225, 57, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (226, 57, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (227, 57, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (228, 57, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T58 (Zone 1, Full-Time, GS Passau-Innstadt)
    (229, 58, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (230, 58, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (231, 58, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (232, 58, 1, 4, 'PREFERRED', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T59 (Zone 2, Part-Time, MS Salzweg)
    (233, 59, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (234, 59, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (235, 59, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (236, 59, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T60 (Zone 1, Full-Time, GS Grubweg)
    (237, 60, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (238, 60, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (239, 60, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (240, 60, 1, 4, 'AVAILABLE', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T61 (Zone 1, Full-Time, MS Hutthurm)
    (241, 61, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (242, 61, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (243, 61, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (244, 61, 1, 4, 'PREFERRED', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T62 (Zone 1, Part-Time, GS Neukirchen v. W.)
    (245, 62, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1/Part-time constraint', NOW()),
    (246, 62, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1/Part-time constraint', NOW()),
    (247, 62, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (248, 62, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T63 (Zone 1, Full-Time, MS Hutthurm)
    (249, 63, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (250, 63, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (251, 63, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (252, 63, 1, 4, 'AVAILABLE', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T64 (Zone 1, Full-Time, GS Vilshofen-Nord)
    (253, 64, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (254, 64, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (255, 64, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (256, 64, 1, 4, 'PREFERRED', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T65 (Zone 2, Part-Time, MS Heining)
    (257, 65, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (258, 65, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (259, 65, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (260, 65, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T66 (Zone 2, Full-Time, GS Tittling)
    (261, 66, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (262, 66, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (263, 66, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (264, 66, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T67 (Zone 2, Full-Time, GS Vilshofen-Stadt)
    (265, 67, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (266, 67, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (267, 67, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (268, 67, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T68 (Zone 2, Full-Time, MS Grafenau)
    (269, 68, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (270, 68, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (271, 68, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (272, 68, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T69 (Zone 2, Part-Time, GS Osterhofen)
    (273, 69, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (274, 69, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (275, 69, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (276, 69, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T70 (Zone 2, Full-Time, MS Bogen)
    (277, 70, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (278, 70, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (279, 70, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (280, 70, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T71 (Zone 2, Full-Time, GS Bad Griesbach)
    (281, 71, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (282, 71, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (283, 71, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (284, 71, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T72 (Zone 2, Part-Time, GS Röhrnbach)
    (285, 72, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (286, 72, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (287, 72, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (288, 72, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T73 (Zone 2, Full-Time, GS Witzmannsberg)
    (289, 73, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (290, 73, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (291, 73, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (292, 73, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T74 (Zone 3, Full-Time, MS Viechtach)
    (293, 74, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (294, 74, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (295, 74, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (296, 74, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T75 (Zone 3, Full-Time, GS Bad Birnbach)
    (297, 75, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (298, 75, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (299, 75, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (300, 75, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T76 (Zone 3, Part-Time, GS Landau an der Isar)
    (301, 76, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (302, 76, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (303, 76, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (304, 76, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T77 (Zone 3, Full-Time, GS Pocking)
    (305, 77, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (306, 77, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (307, 77, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (308, 77, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T78 (Zone 3, Full-Time, GS Osterhofen-Altenmarkt)
    (309, 78, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (310, 78, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (311, 78, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (312, 78, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T79 (Zone 3, Full-Time, GS Bad Füssing)
    (313, 79, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (314, 79, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (315, 79, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (316, 79, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T80 (Zone 3, Part-Time, MS Plattling)
    (317, 80, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (318, 80, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (319, 80, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (320, 80, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T81 (Zone 3, Full-Time, GS Deggendorf-Ost)
    (321, 81, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (322, 81, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (323, 81, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (324, 81, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T82 (Zone 3, Full-Time, GS Mühldorf am Inn)
    (325, 82, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (326, 82, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (327, 82, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (328, 82, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T83 (Zone 3, Full-Time, GS Zwiesel-Nord)
    (329, 83, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (330, 83, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (331, 83, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (332, 83, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T84 (Zone 3, Part-Time, GS Arnstorf)
    (333, 84, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (334, 84, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (335, 84, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (336, 84, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T85 (Zone 3, Full-Time, GS Simbach bei Landau)
    (337, 85, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (338, 85, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (339, 85, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (340, 85, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T86 (Zone 3, Full-Time, GS Freyung)
    (341, 86, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (342, 86, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (343, 86, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (344, 86, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T87 (Zone 1, Part-Time, MS Hutthurm)
    (345, 87, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1/Part-time constraint', NOW()),
    (346, 87, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1/Part-time constraint', NOW()),
    (347, 87, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (348, 87, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T88 (Zone 1, Full-Time, GS Neukirchen v. W.)
    (349, 88, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (350, 88, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (351, 88, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (352, 88, 1, 4, 'AVAILABLE', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T89 (Zone 1, Full-Time, GS Patriching)
    (353, 89, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (354, 89, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (355, 89, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (356, 89, 1, 4, 'PREFERRED', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T90 (Zone 1, Full-Time, GS Ortenburg)
    (357, 90, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (358, 90, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (359, 90, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (360, 90, 1, 4, 'AVAILABLE', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T91 (Zone 1, Part-Time, GS Eging am See)
    (361, 91, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1/Part-time constraint', NOW()),
    (362, 91, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1/Part-time constraint', NOW()),
    (363, 91, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (364, 91, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T92 (Zone 2, Full-Time, MS Hauzenberg)
    (365, 92, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (366, 92, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (367, 92, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (368, 92, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T93 (Zone 2, Full-Time, GS Schöllnach)
    (369, 93, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (370, 93, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (371, 93, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (372, 93, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T94 (Zone 2, Full-Time, GS Aidenbach)
    (373, 94, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (374, 94, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (375, 94, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (376, 94, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T95 (Zone 2, Part-Time, GS Waldkirchen)
    (377, 95, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (378, 95, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (379, 95, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (380, 95, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T96 (Zone 2, Full-Time, GS Deggendorf-Mitte)
    (381, 96, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (382, 96, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (383, 96, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (384, 96, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T97 (Zone 2, Full-Time, GS Fürstenstein)
    (385, 97, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (386, 97, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (387, 97, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (388, 97, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T98 (Zone 2, Part-Time, GS Kellberg)
    (389, 98, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (390, 98, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Part-time constraint', NOW()),
    (391, 98, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (392, 98, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T99 (Zone 3, Full-Time, GS Cham-West)
    (393, 99, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (394, 99, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (395, 99, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (396, 99, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T100 (Zone 3, Full-Time, GS Zwiesel)
    (397, 100, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (398, 100, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (399, 100, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (400, 100, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T101 (Zone 3, Full-Time, MS Bodenmais)
    (401, 101, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (402, 101, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (403, 101, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (404, 101, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T102 (Zone 3, Part-Time, GS Straubing-Süd)
    (405, 102, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (406, 102, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (407, 102, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (408, 102, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T103 (Zone 3, Full-Time, MS Pfarrkirchen)
    (409, 103, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (410, 103, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (411, 103, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (412, 103, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T104 (Zone 3, Full-Time, GS Regen)
    (413, 104, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (414, 104, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (415, 104, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (416, 104, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T105 (Zone 3, Full-Time, GS Simbach am Inn)
    (417, 105, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (418, 105, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (419, 105, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (420, 105, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T106 (Zone 3, Part-Time, GS Dingolfing-Nord)
    (421, 106, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (422, 106, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (423, 106, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (424, 106, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T107 (Zone 3, Full-Time, GS Viechtach-Süd)
    (425, 107, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (426, 107, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (427, 107, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (428, 107, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T108 (Zone 3, Full-Time, GS Vilshofen-Land)
    (429, 108, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (430, 108, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (431, 108, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (432, 108, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T109 (Zone 3, Full-Time, GS Roding)
    (433, 109, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (434, 109, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (435, 109, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (436, 109, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T110 (Zone 3, Part-Time, GS Mainburg)
    (437, 110, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (438, 110, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (439, 110, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (440, 110, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T111 (Zone 3, Full-Time, GS Bad Kötzting)
    (441, 111, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (442, 111, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (443, 111, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (444, 111, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T112 (Zone 1, Inactive)
    (445, 112, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Teacher is INACTIVE_THIS_YEAR', NOW()),
    (446, 112, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Teacher is INACTIVE_THIS_YEAR', NOW()),
    (447, 112, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Teacher is INACTIVE_THIS_YEAR', NOW()),
    (448, 112, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Teacher is INACTIVE_THIS_YEAR', NOW()),

    -- T113 (Zone 2, Inactive)
    (449, 113, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Teacher is INACTIVE_THIS_YEAR', NOW()),
    (450, 113, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Teacher is INACTIVE_THIS_YEAR', NOW()),
    (451, 113, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Teacher is INACTIVE_THIS_YEAR', NOW()),
    (452, 113, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Teacher is INACTIVE_THIS_YEAR', NOW()),

    -- T114 (Zone 3, Part-Time, Active)
    (453, 114, 1, 1, 'PREFERRED', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (454, 114, 1, 2, 'PREFERRED', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (455, 114, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (456, 114, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW());

-- 10. INTERNSHIP_DEMANDS
-- High priority: SFP German in Primary schools
INSERT INTO internship_demands (id, academic_year_id, internship_type_id, school_type, subject_id, required_teachers, student_count, is_forecasted, created_at, updated_at)
VALUES
    (1, 1, 4, 'Primary', 1, 10, 40, TRUE, NOW(), NOW()),    -- D (German Primary, SFP, Forecasted)
    (2, 1, 4, 'Middle', 3, 5, 20, TRUE, NOW(), NOW()),      -- E (English Middle, SFP, Forecasted)
    (3, 1, 1, 'Primary', 1, 15, 30, FALSE, NOW(), NOW()),   -- D (German Primary, PDP1, Not Forecasted)
    (4, 1, 4, 'Primary', 23, 5, 20, TRUE, NOW(), NOW()),    -- E (English Primary, SFP, Forecasted)
    (5, 1, 4, 'Primary', 12, 7, 28, TRUE, NOW(), NOW()),    -- HSU (Home and Subject Matter Lessons, SFP, Forecasted)
    (6, 1, 4, 'Primary', 2, 6, 24, TRUE, NOW(), NOW()),     -- MA (Mathematics Primary, SFP, Forecasted)
    (7, 1, 4, 'Primary', 4, 4, 16, TRUE, NOW(), NOW()),     -- KRel (Catholic Religion, SFP, Forecasted)
    (8, 1, 4, 'Middle', 10, 2, 8, TRUE, NOW(), NOW()),      -- GE (History Middle, SFP, Forecasted)
    (9, 1, 4, 'Middle', 22, 2, 8, TRUE, NOW(), NOW()),      -- MA (Mathematics Middle, SFP, Forecasted)
    (10, 1, 4, 'Middle', 21, 1, 4, TRUE, NOW(), NOW()),     -- D (German Middle, SFP, Forecasted)

    -- ZSP Demands (Internship Type ID 3, Fixed/Known = FALSE) - Priority 2 (40 Primary / 10 Middle)
    (11, 1, 3, 'Primary', 1, 8, 32, FALSE, NOW(), NOW()),   -- D (German Primary, ZSP, Not Forecasted)
    (12, 1, 3, 'Middle', 3, 3, 12, FALSE, NOW(), NOW()),    -- E (English Middle, ZSP, Not Forecasted)
    (13, 1, 3, 'Primary', 23, 4, 16, FALSE, NOW(), NOW()),  -- E (English Primary, ZSP, Not Forecasted)
    (14, 1, 3, 'Primary', 12, 8, 32, FALSE, NOW(), NOW()),  -- HSU (Home and Subject Matter Lessons, ZSP, Not Forecasted)
    (15, 1, 3, 'Primary', 2, 7, 28, FALSE, NOW(), NOW()),   -- MA (Mathematics Primary, ZSP, Not Forecasted)
    (16, 1, 3, 'Primary', 7, 4, 16, FALSE, NOW(), NOW()),   -- SP (Sport Primary, ZSP, Not Forecasted)
    (17, 1, 3, 'Primary', 4, 3, 12, FALSE, NOW(), NOW()),   -- KRel (Catholic Religion, ZSP, Not Forecasted)
    (18, 1, 3, 'Middle', 8, 2, 8, FALSE, NOW(), NOW()),     -- SK (Social Studies Middle, ZSP, Not Forecasted)
    (19, 1, 3, 'Middle', 15, 2, 8, FALSE, NOW(), NOW()),    -- PCB (Physics-Chemistry-Biology, ZSP, Not Forecasted)
    (20, 1, 3, 'Middle', 21, 2, 8, FALSE, NOW(), NOW()),    -- D (German Middle, ZSP, Not Forecasted)

    -- PDP 1 Demands (Internship Type ID 1, Fixed/Known = FALSE) - Priority 3 (40 Primary / 10 Middle)
    (21, 1, 1, 'Primary', 1, 40, 80, FALSE, NOW(), NOW()),  -- D (German Primary, PDP1, Not Forecasted)
    (22, 1, 1, 'Middle', 3, 10, 20, FALSE, NOW(), NOW()),   -- E (English Middle, PDP1, Not Forecasted)

    -- PDP 2 Demands (Internship Type ID 2, Forecasted = TRUE) - Priority 3 (49 Primary / 11 Middle)
    (23, 1, 2, 'Primary', 1, 49, 98, TRUE, NOW(), NOW()),   -- D (German Primary, PDP2, Forecasted)
    (24, 1, 2, 'Middle', 3, 11, 22, TRUE, NOW(), NOW());    -- E (English Middle, PDP2, Forecasted)

-- 11. ALLOCATION_PLAN
-- A draft plan created by the Admin.
INSERT INTO ALLOCATION_PLANS (id, year_id, plan_name, plan_version, status, is_current, created_at, updated_at)
VALUES
(1, 1, 'Allocation Draft V1', '1.0', 'Draft', TRUE, NOW(), NOW());

-- 12. TEACHER_ASSIGNMENT
-- The result of the allocation logic. Hans matches strict subject constraint for ZSP.
INSERT INTO TEACHER_ASSIGNMENTS (id, plan_id, teacher_id, internship_type_id, subject_id, student_group_size, assignment_status, is_manual_override, notes, assigned_at, created_at)
VALUES
    -- User's Original Examples
    (1, 1, 1, 4, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched based on subject D (SFP)', NOW(), NOW()),
    (2, 1, 1, 3, 2, 4, 'CONFIRMED', FALSE, 'Auto-matched based on subject MA (ZSP)', NOW(), NOW()), -- T1: D+MA (Complete)

    -- T2 (Zone 1, Part-Time, PRIMARY): ZSP/SFP only
    (3, 1, 2, 3, 4, 4, 'CONFIRMED', FALSE, 'Auto-matched based on subject KRel (ZSP)', NOW(), NOW()),
    (4, 1, 2, 4, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched based on subject HSU (SFP)', NOW(), NOW()), -- T2: KRel+HSU (Complete)

    -- T3 (Zone 2, Full-Time, MIDDLE): E + PDP
    (5, 1, 3, 4, 3, 4, 'CONFIRMED', FALSE, 'Auto-matched based on subject E (SFP)', NOW(), NOW()),
    (6, 1, 3, 1, 21, 2, 'CONFIRMED', FALSE, 'Filler PDP 1 assignment', NOW(), NOW()), -- T3: E+PDP1 (Complete)

    -- T4 (Zone 3, Full-Time, PRIMARY): PDPs only
    (7, 1, 4, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1 assignment', NOW(), NOW()),
    (8, 1, 4, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2 assignment', NOW(), NOW()), -- T4: PDP1+PDP2 (Complete)

    -- T5 (Zone 1, Full-Time, PRIMARY)
    (9, 1, 5, 3, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (ZSP)', NOW(), NOW()),
    (10, 1, 5, 4, 2, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (SFP)', NOW(), NOW()), -- T5: D+MA (Complete)

    -- T6 (Zone 1, Part-Time, PRIMARY)
    (11, 1, 6, 3, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (ZSP)', NOW(), NOW()),
    (12, 1, 6, 4, 23, 4, 'CONFIRMED', FALSE, 'Auto-matched E (SFP)', NOW(), NOW()), -- T6: HSU+E (Complete)

    -- T7 (Zone 1, Full-Time, PRIMARY)
    (13, 1, 7, 3, 4, 4, 'CONFIRMED', FALSE, 'Auto-matched KRel (ZSP)', NOW(), NOW()),
    (14, 1, 7, 4, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (SFP)', NOW(), NOW()), -- T7: KRel+D (Complete)

    -- T8 (Zone 1, Part-Time, PRIMARY)
    (15, 1, 8, 3, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (ZSP)', NOW(), NOW()),
    (16, 1, 8, 4, 5, 4, 'CONFIRMED', FALSE, 'Auto-matched MU (SFP)', NOW(), NOW()), -- T8: HSU+MU (Complete)

    -- T9 (Zone 1, Full-Time, PRIMARY)
    (17, 1, 9, 3, 2, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (ZSP)', NOW(), NOW()),
    (18, 1, 9, 4, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (SFP)', NOW(), NOW()), -- T9: MA+D (Complete)

    -- T10 (Zone 1, Full-Time, PRIMARY)
    (19, 1, 10, 3, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (ZSP)', NOW(), NOW()),
    (20, 1, 10, 4, 4, 4, 'CONFIRMED', FALSE, 'Auto-matched KRel (SFP)', NOW(), NOW()), -- T10: HSU+KRel (Complete)

    -- T11 (Zone 1, Full-Time, PRIMARY)
    (21, 1, 11, 3, 23, 4, 'CONFIRMED', FALSE, 'Auto-matched E (ZSP)', NOW(), NOW()),
    (22, 1, 11, 4, 7, 4, 'CONFIRMED', FALSE, 'Auto-matched SP (SFP)', NOW(), NOW()), -- T11: E+SP (Complete)

    -- T12 (Zone 1, Part-Time, PRIMARY)
    (23, 1, 12, 3, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (ZSP)', NOW(), NOW()),
    (24, 1, 12, 4, 20, 4, 'CONFIRMED', FALSE, 'Auto-matched SSE (SFP)', NOW(), NOW()), -- T12: D+SSE (Complete)

    -- T13 (Zone 1, Full-Time, MIDDLE)
    (25, 1, 13, 3, 3, 4, 'CONFIRMED', FALSE, 'Auto-matched E (ZSP)', NOW(), NOW()),
    (26, 1, 13, 4, 8, 4, 'CONFIRMED', FALSE, 'Auto-matched SK (SFP)', NOW(), NOW()), -- T13: E+SK (Complete)

    -- T14 (Zone 2, Full-Time, MIDDLE)
    (27, 1, 14, 4, 3, 4, 'CONFIRMED', FALSE, 'Auto-matched E (SFP)', NOW(), NOW()),
    (28, 1, 14, 3, 21, 4, 'CONFIRMED', FALSE, 'Auto-matched D (ZSP)', NOW(), NOW()), -- T14: E+D (Complete)

    -- T15 (Zone 2, Full-Time, PRIMARY)
    (29, 1, 15, 3, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (ZSP)', NOW(), NOW()),
    (30, 1, 15, 4, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (SFP)', NOW(), NOW()), -- T15: HSU+D (Complete)

    -- T16 (Zone 2, Part-Time, PRIMARY)
    (31, 1, 16, 3, 23, 4, 'CONFIRMED', FALSE, 'Auto-matched E (ZSP)', NOW(), NOW()),
    (32, 1, 16, 4, 5, 4, 'CONFIRMED', FALSE, 'Auto-matched MU (SFP)', NOW(), NOW()), -- T16: E+MU (Complete)

    -- T17 (Zone 2, Full-Time, PRIMARY)
    (33, 1, 17, 3, 2, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (ZSP)', NOW(), NOW()),
    (34, 1, 17, 4, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (SFP)', NOW(), NOW()), -- T17: MA+HSU (Complete)

    -- T18 (Zone 2, Full-Time, MIDDLE)
    (35, 1, 18, 4, 3, 4, 'CONFIRMED', FALSE, 'Auto-matched E (SFP)', NOW(), NOW()),
    (36, 1, 18, 3, 16, 4, 'CONFIRMED', FALSE, 'Auto-matched IT (ZSP)', NOW(), NOW()), -- T18: E+IT (Complete)

    -- T19 (Zone 2, Full-Time, PRIMARY)
    (37, 1, 19, 4, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (SFP)', NOW(), NOW()),
    (38, 1, 19, 3, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (ZSP)', NOW(), NOW()), -- T19: D+HSU (Complete)

    -- T20 (Zone 2, Full-Time, PRIMARY)
    (39, 1, 20, 3, 2, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (ZSP)', NOW(), NOW()),
    (40, 1, 20, 4, 23, 4, 'CONFIRMED', FALSE, 'Auto-matched E (SFP)', NOW(), NOW()), -- T20: MA+E (Complete)

    -- T21 (Zone 2, Full-Time, PRIMARY)
    (41, 1, 21, 3, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (ZSP)', NOW(), NOW()),
    (42, 1, 21, 4, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (SFP)', NOW(), NOW()), -- T21: HSU+D (Complete)

    -- T22 (Zone 2, Full-Time, PRIMARY)
    (43, 1, 22, 3, 23, 4, 'CONFIRMED', FALSE, 'Auto-matched E (ZSP)', NOW(), NOW()),
    (44, 1, 22, 4, 6, 4, 'CONFIRMED', FALSE, 'Auto-matched KE (SFP)', NOW(), NOW()), -- T22: E+KE (Complete)

    -- T23 (Zone 2, Part-Time, PRIMARY)
    (45, 1, 23, 3, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (ZSP)', NOW(), NOW()),
    (46, 1, 23, 4, 4, 4, 'CONFIRMED', FALSE, 'Auto-matched KRel (SFP)', NOW(), NOW()), -- T23: HSU+KRel (Complete)

    -- T24 (Zone 2, Full-Time, PRIMARY)
    (47, 1, 24, 3, 2, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (ZSP)', NOW(), NOW()),
    (48, 1, 24, 4, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (SFP)', NOW(), NOW()), -- T24: MA+D (Complete)

    -- T25 (Zone 2, Full-Time, PRIMARY)
    (49, 1, 25, 3, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (ZSP)', NOW(), NOW()),
    (50, 1, 25, 4, 23, 4, 'CONFIRMED', FALSE, 'Auto-matched E (SFP)', NOW(), NOW()), -- T25: HSU+E (Complete)

    -- T26 (Zone 2, Part-Time, PRIMARY)
    (51, 1, 26, 3, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (ZSP)', NOW(), NOW()),
    (52, 1, 26, 4, 2, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (SFP)', NOW(), NOW()), -- T26: D+MA (Complete)

    -- T27 (Zone 2, Full-Time, MIDDLE)
    (53, 1, 27, 4, 3, 4, 'CONFIRMED', FALSE, 'Auto-matched E (SFP)', NOW(), NOW()),
    (54, 1, 27, 3, 21, 4, 'CONFIRMED', FALSE, 'Auto-matched D (ZSP)', NOW(), NOW()), -- T27: E+D (Complete)

    -- T28 (Zone 2, Full-Time, MIDDLE)
    (55, 1, 28, 4, 22, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (SFP)', NOW(), NOW()),
    (56, 1, 28, 3, 13, 4, 'CONFIRMED', FALSE, 'Auto-matched AL (ZSP)', NOW(), NOW()), -- T28: MA+AL (Complete)

    -- T29 (Zone 2, Full-Time, MIDDLE)
    (57, 1, 29, 3, 3, 4, 'CONFIRMED', FALSE, 'Auto-matched E (ZSP)', NOW(), NOW()),
    (58, 1, 29, 4, 10, 4, 'CONFIRMED', FALSE, 'Auto-matched GE (SFP)', NOW(), NOW()), -- T29: E+GE (Complete)

    -- T30 (Zone 2, Part-Time, MIDDLE)
    (59, 1, 30, 3, 22, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (ZSP)', NOW(), NOW()),
    (60, 1, 30, 4, 3, 4, 'CONFIRMED', FALSE, 'Auto-matched E (SFP)', NOW(), NOW()), -- T30: MA+E (Complete)

    -- T31 (Zone 2, Full-Time, MIDDLE)
    (61, 1, 31, 3, 21, 4, 'CONFIRMED', FALSE, 'Auto-matched D (ZSP)', NOW(), NOW()),
    (62, 1, 31, 4, 22, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (SFP)', NOW(), NOW()), -- T31: D+MA (Complete)

    -- T32-T53, T54-T57, T74-T86, T99-T111, T114 (Zone 3, PDPs only) - 34 teachers x 2 slots = 68 assignments
    (63, 1, 32, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (64, 1, 32, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T32 Complete
    (65, 1, 33, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (66, 1, 33, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T33 Complete
    (67, 1, 34, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (68, 1, 34, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T34 Complete
    (69, 1, 35, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (70, 1, 35, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T35 Complete
    (71, 1, 36, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (72, 1, 36, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T36 Complete
    (73, 1, 37, 1, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (74, 1, 37, 2, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T37 Complete
    (75, 1, 38, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (76, 1, 38, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T38 Complete
    (77, 1, 39, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (78, 1, 39, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T39 Complete
    (79, 1, 40, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (80, 1, 40, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T40 Complete
    (81, 1, 41, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (82, 1, 41, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T41 Complete
    (83, 1, 42, 1, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (84, 1, 42, 2, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T42 Complete
    (85, 1, 43, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (86, 1, 43, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T43 Complete
    (87, 1, 44, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (88, 1, 44, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T44 Complete
    (89, 1, 45, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (90, 1, 45, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T45 Complete
    (91, 1, 46, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (92, 1, 46, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T46 Complete
    (93, 1, 47, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (94, 1, 47, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T47 Complete
    (95, 1, 48, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (96, 1, 48, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T48 Complete
    (97, 1, 49, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (98, 1, 49, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T49 Complete
    (99, 1, 50, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (100, 1, 50, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T50 Complete
    (101, 1, 51, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (102, 1, 51, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T51 Complete
    (103, 1, 52, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (104, 1, 52, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T52 Complete
    (105, 1, 53, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (106, 1, 53, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T53 Complete
    (107, 1, 54, 1, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (108, 1, 54, 2, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T54 Complete
    (109, 1, 55, 1, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (110, 1, 55, 2, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T55 Complete
    (111, 1, 56, 1, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (112, 1, 56, 2, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T56 Complete
    (113, 1, 57, 1, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (114, 1, 57, 2, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T57 Complete

    -- T58-T64 (Zone 1, Full-Time, PRIMARY/MIDDLE)
    (115, 1, 58, 3, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (ZSP)', NOW(), NOW()),
    (116, 1, 58, 4, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (SFP)', NOW(), NOW()), -- T58 Complete
    (117, 1, 59, 3, 8, 4, 'CONFIRMED', FALSE, 'Auto-matched SK (ZSP)', NOW(), NOW()),
    (118, 1, 59, 4, 3, 4, 'CONFIRMED', FALSE, 'Auto-matched E (SFP)', NOW(), NOW()), -- T59 Complete
    (119, 1, 60, 3, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (ZSP)', NOW(), NOW()),
    (120, 1, 60, 4, 2, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (SFP)', NOW(), NOW()), -- T60 Complete
    (121, 1, 61, 3, 22, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (ZSP)', NOW(), NOW()),
    (122, 1, 61, 4, 3, 4, 'CONFIRMED', FALSE, 'Auto-matched E (SFP)', NOW(), NOW()), -- T61 Complete
    (123, 1, 62, 3, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (ZSP)', NOW(), NOW()),
    (124, 1, 62, 4, 5, 4, 'CONFIRMED', FALSE, 'Auto-matched MU (SFP)', NOW(), NOW()), -- T62 Complete
    (125, 1, 63, 3, 17, 4, 'CONFIRMED', FALSE, 'Auto-matched GSE (ZSP)', NOW(), NOW()),
    (126, 1, 63, 4, 21, 4, 'CONFIRMED', FALSE, 'Auto-matched D (SFP)', NOW(), NOW()), -- T63 Complete
    (127, 1, 64, 3, 2, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (ZSP)', NOW(), NOW()),
    (128, 1, 64, 4, 4, 4, 'CONFIRMED', FALSE, 'Auto-matched KRel (SFP)', NOW(), NOW()), -- T64 Complete

    -- T65-T73 (Zone 2, Full/Part-Time, PRIMARY/MIDDLE)
    (129, 1, 65, 3, 22, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (ZSP)', NOW(), NOW()),
    (130, 1, 65, 4, 3, 4, 'CONFIRMED', FALSE, 'Auto-matched E (SFP)', NOW(), NOW()), -- T65 Complete
    (131, 1, 66, 3, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (ZSP)', NOW(), NOW()),
    (132, 1, 66, 4, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (SFP)', NOW(), NOW()), -- T66 Complete
    (133, 1, 67, 3, 23, 4, 'CONFIRMED', FALSE, 'Auto-matched E (ZSP)', NOW(), NOW()),
    (134, 1, 67, 4, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (SFP)', NOW(), NOW()), -- T67 Complete
    (135, 1, 68, 3, 21, 4, 'CONFIRMED', FALSE, 'Auto-matched D (ZSP)', NOW(), NOW()),
    (136, 1, 68, 4, 3, 4, 'CONFIRMED', FALSE, 'Auto-matched E (SFP)', NOW(), NOW()), -- T68 Complete
    (137, 1, 69, 3, 2, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (ZSP)', NOW(), NOW()),
    (138, 1, 69, 4, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (SFP)', NOW(), NOW()), -- T69 Complete
    (139, 1, 70, 3, 15, 4, 'CONFIRMED', FALSE, 'Auto-matched PCB (ZSP)', NOW(), NOW()),
    (140, 1, 70, 4, 3, 4, 'CONFIRMED', FALSE, 'Auto-matched E (SFP)', NOW(), NOW()), -- T70 Complete
    (141, 1, 71, 3, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (ZSP)', NOW(), NOW()),
    (142, 1, 71, 4, 20, 4, 'CONFIRMED', FALSE, 'Auto-matched SSE (SFP)', NOW(), NOW()), -- T71 Complete
    (143, 1, 72, 3, 7, 4, 'CONFIRMED', FALSE, 'Auto-matched SP (ZSP)', NOW(), NOW()),
    (144, 1, 72, 4, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (SFP)', NOW(), NOW()), -- T72 Complete
    (145, 1, 73, 3, 23, 4, 'CONFIRMED', FALSE, 'Auto-matched E (ZSP)', NOW(), NOW()),
    (146, 1, 73, 4, 6, 4, 'CONFIRMED', FALSE, 'Auto-matched KE (SFP)', NOW(), NOW()), -- T73 Complete

    -- T74-T86, T99-T111, T114 (Zone 3, PDPs only)
    (147, 1, 74, 1, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (148, 1, 74, 2, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T74 Complete
    (149, 1, 75, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (150, 1, 75, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T75 Complete
    (151, 1, 76, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (152, 1, 76, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T76 Complete
    (153, 1, 77, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (154, 1, 77, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T77 Complete
    (155, 1, 78, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (156, 1, 78, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T78 Complete
    (157, 1, 79, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (158, 1, 79, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T79 Complete
    (159, 1, 80, 1, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (160, 1, 80, 2, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T80 Complete
    (161, 1, 81, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (162, 1, 81, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T81 Complete
    (163, 1, 82, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (164, 1, 82, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T82 Complete
    (165, 1, 83, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (166, 1, 83, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T83 Complete
    (167, 1, 84, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (168, 1, 84, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T84 Complete
    (169, 1, 85, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (170, 1, 85, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T85 Complete
    (171, 1, 86, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (172, 1, 86, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T86 Complete
    (173, 1, 99, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (174, 1, 99, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T99 Complete
    (175, 1, 100, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (176, 1, 100, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T100 Complete
    (177, 1, 101, 1, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (178, 1, 101, 2, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T101 Complete
    (179, 1, 102, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (180, 1, 102, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T102 Complete
    (181, 1, 103, 1, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (182, 1, 103, 2, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T103 Complete
    (183, 1, 104, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (184, 1, 104, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T104 Complete
    (185, 1, 105, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (186, 1, 105, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T105 Complete
    (187, 1, 106, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (188, 1, 106, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T106 Complete
    (189, 1, 107, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (190, 1, 107, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T107 Complete
    (191, 1, 108, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (192, 1, 108, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T108 Complete
    (193, 1, 109, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (194, 1, 109, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T109 Complete
    (195, 1, 110, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (196, 1, 110, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T110 Complete
    (197, 1, 111, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()),
    (198, 1, 111, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T111 Complete
    (199, 1, 114, 1, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1 (Teacher with -1 balance)', NOW(), NOW()),
    (200, 1, 114, 2, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()), -- T114 Complete

    -- T87-T98 (Zone 1/2, Full/Part-Time) - Fulfill remaining ZSP/SFP slots, then use PDP as needed.
    -- T87 (Zone 1, Part-Time, MIDDLE)
    (201, 1, 87, 3, 3, 4, 'CONFIRMED', FALSE, 'Auto-matched E (ZSP)', NOW(), NOW()),
    (202, 1, 87, 4, 10, 4, 'CONFIRMED', FALSE, 'Auto-matched GE (SFP)', NOW(), NOW()), -- T87 Complete
    -- T88 (Zone 1, Full-Time, PRIMARY)
    (203, 1, 88, 3, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (ZSP)', NOW(), NOW()),
    (204, 1, 88, 4, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (SFP)', NOW(), NOW()), -- T88 Complete
    -- T89 (Zone 1, Full-Time, PRIMARY)
    (205, 1, 89, 3, 2, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (ZSP)', NOW(), NOW()),
    (206, 1, 89, 4, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (SFP)', NOW(), NOW()), -- T89 Complete
    -- T90 (Zone 1, Full-Time, PRIMARY)
    (207, 1, 90, 3, 23, 4, 'CONFIRMED', FALSE, 'Auto-matched E (ZSP)', NOW(), NOW()),
    (208, 1, 90, 4, 5, 4, 'CONFIRMED', FALSE, 'Auto-matched MU (SFP)', NOW(), NOW()), -- T90 Complete
    -- T91 (Zone 1, Part-Time, PRIMARY)
    (209, 1, 91, 3, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (ZSP)', NOW(), NOW()),
    (210, 1, 91, 4, 7, 4, 'CONFIRMED', FALSE, 'Auto-matched SP (SFP)', NOW(), NOW()), -- T91 Complete
    -- T92 (Zone 2, Full-Time, MIDDLE)
    (211, 1, 92, 3, 22, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (ZSP)', NOW(), NOW()),
    (212, 1, 92, 4, 3, 4, 'CONFIRMED', FALSE, 'Auto-matched E (SFP)', NOW(), NOW()), -- T92 Complete
    -- T93 (Zone 2, Full-Time, PRIMARY)
    (213, 1, 93, 3, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (ZSP)', NOW(), NOW()),
    (214, 1, 93, 4, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (SFP)', NOW(), NOW()), -- T93 Complete
    -- T94 (Zone 2, Full-Time, PRIMARY)
    (215, 1, 94, 3, 23, 4, 'CONFIRMED', FALSE, 'Auto-matched E (ZSP)', NOW(), NOW()),
    (216, 1, 94, 4, 7, 4, 'CONFIRMED', FALSE, 'Auto-matched SP (SFP)', NOW(), NOW()), -- T94 Complete
    -- T95 (Zone 2, Part-Time, PRIMARY)
    (217, 1, 95, 3, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (ZSP)', NOW(), NOW()),
    (218, 1, 95, 4, 2, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (SFP)', NOW(), NOW()), -- T95 Complete
    -- T96 (Zone 2, Full-Time, PRIMARY)
    (219, 1, 96, 3, 2, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (ZSP)', NOW(), NOW()),
    (220, 1, 96, 4, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (SFP)', NOW(), NOW()), -- T96 Complete
    -- T97 (Zone 2, Full-Time, PRIMARY)
    (221, 1, 97, 3, 23, 4, 'CONFIRMED', FALSE, 'Auto-matched E (ZSP)', NOW(), NOW()),
    (222, 1, 97, 4, 6, 4, 'CONFIRMED', FALSE, 'Auto-matched KE (SFP)', NOW(), NOW()), -- T97 Complete
    -- T98 (Zone 2, Part-Time, PRIMARY)
    (223, 1, 98, 3, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (ZSP)', NOW(), NOW()),
    (224, 1, 98, 4, 4, 4, 'CONFIRMED', FALSE, 'Auto-matched KRel (SFP)', NOW(), NOW()); -- T98 Complete

-- 13. CREDIT_HOUR_TRACKING
-- Hans has 2 assignments, so he earns 1.0 credit hour (Reduction hour).
INSERT INTO CREDIT_HOUR_TRACKING (id, teacher_id, academic_year_id, assignments_count, credit_hours_allocated, credit_balance, notes, created_at)
VALUES
(1, 1, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW());