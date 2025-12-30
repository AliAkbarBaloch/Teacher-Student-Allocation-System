INSERT INTO users ( id, email, password, full_name, enabled, account_locked, failed_login_attempts, last_login_date, last_password_reset_date, account_status, role, phone_number, created_at, updated_at)
VALUES
    (1,'admin@example.com','$2a$10$iu6rVkymWojgOcGgrzZmh.Om7y1910hk6aI/wZFVwdlh/wOn/hiB6','Admin User',TRUE,FALSE,0,NULL,NULL,'ACTIVE','ADMIN',NULL,NOW(),NOW());

INSERT INTO ACADEMIC_YEARS (id, year_name, total_credit_hours, elementary_school_hours, middle_school_hours, budget_announcement_date, allocation_deadline, is_locked, created_at, updated_at)
VALUES (1, '2025/2026', 210, 169, 41, '2025-05-15', '2025-06-30', FALSE, NOW(), NOW()),
       (2, '2026/2027', 210, 169, 41, '2026-05-15', '2026-06-30', FALSE, NOW(), NOW());

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
    (6, 4, 3, TRUE, NOW()), -- SFP + ZSP
    (7, 3, 4, TRUE, NOW()); -- ZSP + SFP


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
    (53, 'Grundschule Bad Kötzting', 'PRIMARY', 3, 'Bad Kötzting Kurstr', 49.1700, 12.8700, 80.0, 'None', 'gs.badkoetzting@schule.de', '+49 9941 90011', TRUE, NOW(), NOW()),
    
    (54, 'Grundschule Regen-Nord', 'PRIMARY', 3, 'Regen Nordstr', 48.9700, 13.1200, 59.0, 'None', 'gs.regen.n@schule.de', NULL, TRUE, NOW(), NOW()),
    (55, 'Grundschule Kirchdorf', 'PRIMARY', 3, 'Kirchdorf am Inn', 48.2400, 12.9800, 65.0, 'None', 'gs.kirchdorf@schule.de', NULL, TRUE, NOW(), NOW()),
    (56, 'Grundschule Eichendorf', 'PRIMARY', 3, 'Eichendorf Markt', 48.6300, 12.8500, 45.0, 'None', 'gs.eichendorf@schule.de', NULL, TRUE, NOW(), NOW()),
    (57, 'Grundschule Frontenhausen', 'PRIMARY', 3, 'Frontenhausen', 48.5500, 12.5200, 60.0, 'None', 'gs.frontenhausen@schule.de', NULL, TRUE, NOW(), NOW()),
    (58, 'Grundschule Wallersdorf', 'PRIMARY', 3, 'Wallersdorf', 48.7300, 12.7500, 48.0, 'None', 'gs.wallersdorf@schule.de', NULL, TRUE, NOW(), NOW()),
    (59, 'Grundschule Pilsting', 'PRIMARY', 3, 'Pilsting', 48.7000, 12.6500, 50.0, 'None', 'gs.pilsting@schule.de', NULL, TRUE, NOW(), NOW()),
    (60, 'Grundschule Reisbach', 'PRIMARY', 3, 'Reisbach', 48.5700, 12.6300, 55.0, 'None', 'gs.reisbach@schule.de', NULL, TRUE, NOW(), NOW()),
    (61, 'Grundschule Mengkofen', 'PRIMARY', 3, 'Mengkofen', 48.7200, 12.4300, 70.0, 'None', 'gs.mengkofen@schule.de', NULL, TRUE, NOW(), NOW()),
    (62, 'Grundschule Geiselhöring', 'PRIMARY', 3, 'Geiselhöring', 48.8300, 12.4000, 75.0, 'None', 'gs.geiselhoering@schule.de', NULL, TRUE, NOW(), NOW()),
    (63, 'Grundschule Mallersdorf', 'PRIMARY', 3, 'Mallersdorf', 48.7700, 12.1800, 85.0, 'None', 'gs.mallersdorf@schule.de', NULL, TRUE, NOW(), NOW()),
    (64, 'Grundschule Rottenburg', 'PRIMARY', 3, 'Rottenburg', 48.7000, 12.0200, 95.0, 'None', 'gs.rottenburg@schule.de', NULL, TRUE, NOW(), NOW()),
    (65, 'Grundschule Ergoldsbach', 'PRIMARY', 3, 'Ergoldsbach', 48.6800, 12.2000, 80.0, 'None', 'gs.ergoldsbach@schule.de', NULL, TRUE, NOW(), NOW()),
    (66, 'Grundschule Neufahrn', 'PRIMARY', 3, 'Neufahrn NB', 48.7300, 12.1800, 82.0, 'None', 'gs.neufahrn@schule.de', NULL, TRUE, NOW(), NOW()),
    (67, 'Grundschule Bayerbach', 'PRIMARY', 3, 'Bayerbach', 48.7000, 13.1500, 40.0, 'None', 'gs.bayerbach@schule.de', NULL, TRUE, NOW(), NOW()),
    (68, 'Grundschule Ruhstorf', 'PRIMARY', 3, 'Ruhstorf', 48.4300, 13.3300, 38.0, 'None', 'gs.ruhstorf@schule.de', NULL, TRUE, NOW(), NOW()),
    (69, 'Grundschule Rotthalmünster', 'PRIMARY', 3, 'Rotthalmünster', 48.3500, 13.2000, 45.0, 'None', 'gs.rotthal@schule.de', NULL, TRUE, NOW(), NOW()),
    (70, 'Grundschule Kößlarn', 'PRIMARY', 3, 'Kößlarn', 48.3700, 13.1200, 48.0, 'None', 'gs.koesslarn@schule.de', NULL, TRUE, NOW(), NOW()),
    (71, 'Grundschule Tann', 'PRIMARY', 3, 'Tann', 48.3200, 12.8800, 55.0, 'None', 'gs.tann@schule.de', NULL, TRUE, NOW(), NOW()),
    (72, 'Grundschule Wurmannsquick', 'PRIMARY', 3, 'Wurmannsquick', 48.3500, 12.7800, 60.0, 'None', 'gs.wurmannsquick@schule.de', NULL, TRUE, NOW(), NOW()),
    (73, 'Grundschule Zeilarn', 'PRIMARY', 3, 'Zeilarn', 48.3000, 12.8300, 62.0, 'None', 'gs.zeilarn@schule.de', NULL, TRUE, NOW(), NOW()),
    (74, 'Grundschule Gangkofen', 'PRIMARY', 3, 'Gangkofen', 48.4300, 12.5700, 65.0, 'None', 'gs.gangkofen@schule.de', NULL, TRUE, NOW(), NOW()),
    (75, 'Grundschule Massing', 'PRIMARY', 3, 'Massing', 48.4000, 12.6000, 63.0, 'None', 'gs.massing@schule.de', NULL, TRUE, NOW(), NOW()),

    -- ZONE 3 MIDDLE (Remote areas)
    (76, 'Mittelschule Regen', 'MIDDLE', 3, 'Regen', 48.9600, 13.1300, 58.0, 'None', 'ms.regen@schule.de', NULL, TRUE, NOW(), NOW()),
    (77, 'Mittelschule Zwiesel', 'MIDDLE', 3, 'Zwiesel', 49.0200, 13.2300, 52.0, 'None', 'ms.zwiesel@schule.de', NULL, TRUE, NOW(), NOW()),
    (78, 'Mittelschule Osterhofen', 'MIDDLE', 3, 'Osterhofen', 48.6500, 12.9800, 39.5, 'None', 'ms.osterhofen@schule.de', NULL, TRUE, NOW(), NOW()),
    (79, 'Mittelschule Landau', 'MIDDLE', 3, 'Landau Isar', 48.6700, 12.7000, 52.0, 'None', 'ms.landau@schule.de', NULL, TRUE, NOW(), NOW()),
    (80, 'Mittelschule Dingolfing', 'MIDDLE', 3, 'Dingolfing', 48.6300, 12.5000, 70.0, 'None', 'ms.dingolfing@schule.de', NULL, TRUE, NOW(), NOW()),
    (81, 'Mittelschule Vilsbiburg', 'MIDDLE', 3, 'Vilsbiburg', 48.4500, 12.3500, 80.0, 'None', 'ms.vilsbiburg@schule.de', NULL, TRUE, NOW(), NOW()),
    (82, 'Mittelschule Eggenfelden', 'MIDDLE', 3, 'Eggenfelden', 48.4000, 12.7600, 60.0, 'None', 'ms.eggenfelden@schule.de', NULL, TRUE, NOW(), NOW()),
    (83, 'Mittelschule Simbach', 'MIDDLE', 3, 'Simbach Inn', 48.2700, 13.0300, 62.0, 'None', 'ms.simbach@schule.de', NULL, TRUE, NOW(), NOW()),
    (84, 'Mittelschule Pocking', 'MIDDLE', 3, 'Pocking', 48.3800, 13.3100, 40.0, 'None', 'ms.pocking@schule.de', NULL, TRUE, NOW(), NOW()),
    (85, 'Mittelschule Bad Griesbach', 'MIDDLE', 3, 'Bad Griesbach', 48.4200, 13.2000, 25.0, 'None', 'ms.badgriesbach@schule.de', NULL, TRUE, NOW(), NOW()),

    -- ZONE 1/2 FILLERS (To balance availability)
    (86, 'Grundschule Tiefenbach', 'PRIMARY', 1, 'Tiefenbach', 48.6200, 13.4000, 8.0, '4a', 'gs.tiefenbach@schule.de', NULL, TRUE, NOW(), NOW()),
    (87, 'Grundschule Salzweg', 'PRIMARY', 1, 'Salzweg', 48.6000, 13.4800, 5.0, '4a', 'gs.salzweg@schule.de', NULL, TRUE, NOW(), NOW()),
    (88, 'Grundschule Büchlberg', 'PRIMARY', 2, 'Büchlberg', 48.6700, 13.5000, 15.0, '4b', 'gs.buechlberg@schule.de', NULL, TRUE, NOW(), NOW()),
    (89, 'Grundschule Hutthurm', 'PRIMARY', 1, 'Hutthurm', 48.6600, 13.4700, 12.0, '4a', 'gs.hutthurm@schule.de', NULL, TRUE, NOW(), NOW()),
    (90, 'Grundschule Fürstenzell', 'PRIMARY', 1, 'Fürstenzell', 48.5200, 13.3500, 12.0, '4a', 'gs.fuerstenzell@schule.de', NULL, TRUE, NOW(), NOW()),
    (91, 'Mittelschule Fürstenzell', 'MIDDLE', 1, 'Fürstenzell', 48.5200, 13.3500, 12.0, '4a', 'ms.fuerstenzell@schule.de', NULL, TRUE, NOW(), NOW()),
    (92, 'Grundschule Ruhstorf 2', 'PRIMARY', 2, 'Ruhstorf', 48.4300, 13.3300, 20.0, '4b', 'gs.ruhstorf@schule.de', NULL, TRUE, NOW(), NOW()),
    (93, 'Mittelschule Ruhstorf', 'MIDDLE', 2, 'Ruhstorf', 48.4300, 13.3300, 20.0, '4b', 'ms.ruhstorf@schule.de', NULL, TRUE, NOW(), NOW()),
    (94, 'Grundschule Neuburg am Inn', 'PRIMARY', 1, 'Neuburg', 48.5000, 13.4500, 10.0, '4a', 'gs.neuburg@schule.de', NULL, TRUE, NOW(), NOW()),
    (95, 'Grundschule Neuhaus am Inn', 'PRIMARY', 1, 'Neuhaus', 48.4600, 13.4200, 14.0, '4a', 'gs.neuhaus@schule.de', NULL, TRUE, NOW(), NOW()),
    (96, 'Mittelschule Rotthalmünster', 'MIDDLE', 2, 'Rotthalmünster', 48.3500, 13.2000, 30.0, '4b', 'ms.rotthal@schule.de', NULL, TRUE, NOW(), NOW()),
    (97, 'Grundschule Kirchham', 'PRIMARY', 3, 'Kirchham', 48.3500, 13.2700, 42.0, 'None', 'gs.kirchham@schule.de', NULL, TRUE, NOW(), NOW()),
    (98, 'Grundschule Malching', 'PRIMARY', 3, 'Malching', 48.3000, 13.1800, 45.0, 'None', 'gs.malching@schule.de', NULL, TRUE, NOW(), NOW()),
    (99, 'Grundschule Ering', 'PRIMARY', 3, 'Ering', 48.3000, 13.1500, 48.0, 'None', 'gs.ering@schule.de', NULL, TRUE, NOW(), NOW()),
    (100, 'Mittelschule Tann', 'MIDDLE', 3, 'Tann', 48.3200, 12.8800, 55.0, 'None', 'ms.tann@schule.de', NULL, TRUE, NOW(), NOW());

-- 7. TEACHER
-- Creating supervisors. Note usage_cycle for HSU rotation.
INSERT INTO teachers (id, school_id, first_name, last_name, email, phone, is_part_time, employment_status, usage_cycle, credit_hour_balance, created_at, updated_at)
VALUES
    -- User's Original Teachers (IDs 1-4)
    (1, 1, 'Hans', 'Müller', 'hans.mueller@gs-passau.de', '+49 851 123456', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (2, 1, 'Anna', 'Schmidt', 'anna.schmidt@gs-passau.de', NULL, TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (3, 2, 'Peter', 'Weber', 'peter.weber@ms-salzweg.de', '+49 851 987654', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (4, 3, 'Julia', 'Wagner', 'julia.wagner@gs-freyung.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),

    -- Generated Teachers (IDs 5-114)

    -- ZONE 1 & PRIMARY Focused (GS 4, 6, 7, 9, 10, 11, 13, 15)
    (5, 4, 'Michael', 'Schneider', 'm.schneider@gs-grubweg.de', '+49 851 20111', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (6, 6, 'Laura', 'Fischer', 'laura.fischer@gs-ruderting.de', NULL, TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (7, 7, 'Thomas', 'Meyer', 'thomas.meyer@gs-neukirchen.de', '+49 8502 9181', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (8, 9, 'Sabine', 'Huber', 'sabine.huber@gs-patriching.de', '+49 851 88991', TRUE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (9, 10, 'Tobias', 'Lang', 'tobias.lang@gs-vilshofen-n.de', '+49 8541 7778', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (10, 11, 'Marie', 'Hoffmann', 'marie.hoffmann@gs-ortenburg.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (11, 13, 'Daniel', 'Schulz', 'daniel.schulz@gs-eging.de', '+49 8544 9111', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (12, 15, 'Katrin', 'Kruse', 'katrin.kruse@gs-neuhaus.de', '+49 851 33344', TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),

    -- ZONE 1 & MIDDLE Focused (MS 5, 12)
    (13, 5, 'Jonas', 'Wagner', 'jonas.wagner@ms-hutthurm.de', '+49 8505 50012', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (14, 12, 'Hannah', 'Bauer', 'hannah.bauer@ms-heining.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),

    -- ZONE 2 & PRIMARY Focused (GS 14, 16, 17, 19, 20, 21, 23, 24, 25, 26, 27, 28)
    (15, 14, 'Leon', 'Schwarz', 'leon.schwarz@gs-tittling.de', '+49 8504 90012', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (16, 16, 'Sophie', 'Weber', 'sophie.weber@gs-vilshofen-s.de', '+49 8541 6656', TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (17, 17, 'Max', 'Krüger', 'max.krueger@gs-schoellnach.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (18, 19, 'Lena', 'Richter', 'lena.richter@gs-aidenbach.de', '+49 8543 96961', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (19, 20, 'Paul', 'Wolf', 'paul.wolf@gs-osterhofen.de', '+49 9932 90056', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (20, 21, 'Emilia', 'Neumann', 'emilia.neumann@gs-waldkirchen.de', '+49 8581 9651', TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (21, 23, 'Felix', 'Köhler', 'felix.koehler@gs-deggendorf-m.de', '+49 991 30304', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (22, 24, 'Mia', 'Gärtner', 'mia.gaertner@gs-badgriesbach.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (23, 25, 'Elias', 'Hahn', 'elias.hahn@gs-fuerstenstein.de', '+49 8504 9301', TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (24, 26, 'Clara', 'Beck', 'clara.beck@gs-roehrnbach.de', '+49 8582 9101', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (25, 27, 'Leo', 'Herzog', 'leo.herzog@gs-kellberg.de', '+49 8503 9201', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (26, 28, 'Paula', 'Sauer', 'paula.sauer@gs-witzmannsberg.de', '+49 8504 90091', TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),

    -- ZONE 2 & MIDDLE Focused (MS 2, 8, 15, 18, 22)
    (27, 2, 'Karl', 'Zimmermann', 'karl.zimmermann@ms-salzweg.de', '+49 851 987655', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (28, 8, 'Luisa', 'Möller', 'luisa.moeller@ms-hutthurm.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (29, 15, 'Simon', 'Frank', 'simon.frank@ms-hauzenberg.de', '+49 8586 97881', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (30, 18, 'Nora', 'Vogel', 'nora.vogel@ms-grafenau.de', '+49 8552 40012', TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (31, 22, 'Moritz', 'Jung', 'moritz.jung@ms-bogen.de', '+49 9422 9661', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),

    -- ZONE 3 & PRIMARY Focused (GS 3, 29, 31, 32, 34, 35, 36, 38, 39, 40, 41, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53)
    (32, 3, 'Finn', 'Schuster', 'finn.schuster@gs-freyung.de', '+49 8551 11223', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (33, 29, 'Amelie', 'Peters', 'amelie.peters@gs-cham-w.de', NULL, TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (34, 31, 'Ben', 'Grams', 'ben.grams@gs-zwiesel.de', '+49 9922 9801', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (35, 32, 'Lilly', 'Koch', 'lilly.koch@gs-badbirnbach.de', '+49 8563 9701', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (36, 34, 'Elias', 'Schröder', 'e.schroeder@gs-landau.de', '+49 9951 90051', TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (37, 35, 'Sarah', 'Maier', 'sarah.maier@gs-straubing-s.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (38, 36, 'Niklas', 'Fuchs', 'niklas.fuchs@gs-pocking.de', '+49 8531 90012', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (39, 38, 'Anna', 'Reiter', 'anna.reiter@gs-altenmarkt.de', '+49 9932 90012', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (40, 39, 'Julian', 'Gross', 'julian.gross@gs-regen.de', '+49 9921 9501', TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (41, 40, 'Mona', 'Keller', 'mona.keller@gs-badfuessing.de', '+49 8531 9601', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (42, 41, 'Luis', 'Baumgartner', 'luis.baumgartner@gs-simbach-i.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (43, 43, 'Frida', 'Winkler', 'frida.winkler@gs-dingolfing-n.de', '+49 8732 90012', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (44, 44, 'Chris', 'Kunz', 'chris.kunz@gs-deggendorf-o.de', '+49 991 30301', TRUE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (45, 45, 'Elena', 'Stein', 'elena.stein@gs-viechtach-s.de', '+49 9942 9401', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (46, 46, 'Robert', 'Vogt', 'robert.vogt@gs-muehldorf.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (47, 47, 'Sophie', 'Engel', 'sophie.engel@gs-vilshofen-l.de', '+49 8541 6651', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (48, 48, 'Anton', 'Moser', 'anton.moser@gs-zwiesel-n.de', '+49 9922 9802', TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (49, 49, 'Lisa', 'König', 'lisa.koenig@gs-roding.de', '+49 9461 9601', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (50, 50, 'Erik', 'Lehmann', 'erik.lehmann@gs-arnstorf.de', '+49 8723 9601', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (51, 51, 'Maria', 'Sperr', 'maria.sperr@gs-mainburg.de', NULL, TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (52, 52, 'Philipp', 'Kamm', 'philipp.kamm@gs-simbach-l.de', '+49 9954 90051', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (53, 53, 'Nicole', 'Seidl', 'nicole.seidl@gs-badkoetzting.de', '+49 9941 90012', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),

    -- ZONE 3 & MIDDLE Focused (MS 30, 33, 37, 42)
    (54, 30, 'Wolfgang', 'Haas', 'wolfgang.haas@ms-viechtach.de', '+49 9942 9402', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (55, 33, 'Theresa', 'Meier', 'theresa.meier@ms-bodenmais.de', '+49 9924 9401', TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (56, 37, 'Jürgen', 'Herzog', 'juergen.herzog@ms-pfarrkirchen.de', NULL, FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (57, 42, 'Lena', 'Keller', 'lena.keller@ms-plattling.de', '+49 9938 90051', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),

    -- Additional Mixed Teachers (IDs 58-114, cycling through schools)
    (58, 1, 'Markus', 'Breu', 'markus.breu@gs-innstadt.de', '+49 851 123457', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (59, 2, 'Petra', 'Riedl', 'petra.riedl@ms-salzweg.de', NULL, TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (60, 4, 'Stefan', 'Eder', 'stefan.eder@gs-grubweg.de', '+49 851 20112', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (61, 5, 'Christa', 'Preiß', 'christa.preiss@ms-hutthurm.de', '+49 8505 50013', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (62, 7, 'Klaus', 'Lehner', 'klaus.lehner@gs-neukirchen.de', NULL, TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (63, 8, 'Elke', 'Huber', 'elke.huber@ms-tiefenbach.de', '+49 8503 92051', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (64, 10, 'Franz', 'Ziegler', 'franz.ziegler@gs-vilshofen-n.de', '+49 8541 7779', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (65, 12, 'Lisa', 'Bachmann', 'lisa.bachmann@ms-heining.de', '+49 851 55444', TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (66, 14, 'Andreas', 'Holzer', 'andreas.holzer@gs-tittling.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (67, 16, 'Monika', 'Kirchberger', 'monika.kirchberger@gs-vilshofen-s.de', '+49 8541 6657', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (68, 18, 'Robert', 'Dorsch', 'robert.dorsch@ms-grafenau.de', '+49 8552 40013', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (69, 20, 'Bianca', 'Mayer', 'bianca.mayer@gs-osterhofen.de', '+49 9932 90057', TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (70, 22, 'Johann', 'Reindl', 'johann.reindl@ms-bogen.de', NULL, FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (71, 24, 'Maria', 'Sinz', 'maria.sinz@gs-badgriesbach.de', '+49 8532 9601', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (72, 26, 'Herbert', 'Götz', 'herbert.goetz@gs-roehrnbach.de', '+49 8582 9102', TRUE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (73, 28, 'Manuela', 'Aigner', 'manuela.aigner@gs-witzmannsberg.de', '+49 8504 90092', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (74, 30, 'Werner', 'Schmid', 'werner.schmid@ms-viechtach.de', NULL, FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (75, 32, 'Silke', 'Heller', 'silke.heller@gs-badbirnbach.de', '+49 8563 9702', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (76, 34, 'Erich', 'Baumann', 'erich.baumann@gs-landau.de', '+49 9951 90052', TRUE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (77, 36, 'Ute', 'Heindl', 'ute.heindl@gs-pocking.de', '+49 8531 90013', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (78, 38, 'Gerd', 'Schäfer', 'gerd.schaefer@gs-altenmarkt.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (79, 40, 'Helga', 'Seidl', 'helga.seidl@gs-badfuessing.de', '+49 8531 9602', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (80, 42, 'Rolf', 'Wagner', 'rolf.wagner@ms-plattling.de', '+49 9938 90052', TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (81, 44, 'Inge', 'Huber', 'inge.huber@gs-deggendorf-o.de', '+49 991 30302', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (82, 46, 'Bernd', 'Kastner', 'bernd.kastner@gs-muehldorf.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (83, 48, 'Uschi', 'Hofmann', 'uschi.hofmann@gs-zwiesel-n.de', '+49 9922 9803', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (84, 50, 'Otto', 'Berger', 'otto.berger@gs-arnstorf.de', '+49 8723 9602', TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (85, 52, 'Heike', 'Meier', 'heike.meier@gs-simbach-l.de', '+49 9954 90052', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (86, 3, 'Gisela', 'Hartl', 'gisela.hartl@gs-freyung.de', '+49 8551 11224', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (87, 5, 'Hubert', 'Jahn', 'hubert.jahn@ms-hutthurm.de', '+49 8505 50014', TRUE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (88, 7, 'Christina', 'Wirth', 'christina.wirth@gs-neukirchen.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (89, 9, 'Rainer', 'Doll', 'rainer.doll@gs-patriching.de', '+49 851 88992', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (90, 11, 'Silvia', 'Brandl', 'silvia.brandl@gs-ortenburg.de', '+49 8542 96111', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (91, 13, 'Georg', 'Lindinger', 'georg.lindinger@gs-eging.de', '+49 8544 9112', TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (92, 15, 'Eva', 'Straub', 'eva.straub@ms-hauzenberg.de', '+49 8586 97882', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (93, 17, 'Josef', 'Wimmer', 'josef.wimmer@gs-schoellnach.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (94, 19, 'Renate', 'Schick', 'renate.schick@gs-aidenbach.de', '+49 8543 96962', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (95, 21, 'Lukas', 'Mühlbauer', 'lukas.muehlbauer@gs-waldkirchen.de', '+49 8581 9652', TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (96, 23, 'Kerstin', 'Friedl', 'kerstin.friedl@gs-deggendorf-m.de', '+49 991 30305', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (97, 25, 'Michael', 'Pretzl', 'michael.pretzl@gs-fuerstenstein.de', '+49 8504 9302', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (98, 27, 'Tanja', 'Maier', 'tanja.maier@gs-kellberg.de', '+49 8503 9202', TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (99, 29, 'Florian', 'Winter', 'florian.winter@gs-cham-w.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (100, 31, 'Susanne', 'Luger', 'susanne.luger@gs-zwiesel.de', '+49 9922 9804', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (101, 33, 'Harald', 'Vilsmeier', 'harald.vilsmeier@ms-bodenmais.de', '+49 9924 9402', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (102, 35, 'Vanessa', 'Strauss', 'vanessa.strauss@gs-straubing-s.de', NULL, TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (103, 37, 'Jochen', 'Koch', 'jochen.koch@ms-pfarrkirchen.de', '+49 8561 9701', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (104, 39, 'Tina', 'Baier', 'tina.baier@gs-regen.de', '+49 9921 9502', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (105, 41, 'Max', 'Schmitt', 'max.schmitt@gs-simbach-i.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (106, 43, 'Sabine', 'Hahn', 'sabine.hahn@gs-dingolfing-n.de', '+49 8732 90013', TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (107, 45, 'Christian', 'Meier', 'christian.meier@gs-viechtach-s.de', '+49 9942 9403', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (108, 47, 'Melanie', 'Schulz', 'melanie.schulz@gs-vilshofen-l.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (109, 49, 'Thomas', 'Wagner', 'thomas.wagner@gs-roding.de', '+49 9461 9602', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (110, 51, 'Verena', 'Zitzelsberger', 'verena.zitzelsberger@gs-mainburg.de', NULL, TRUE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (111, 53, 'Hubert', 'Müller', 'hubert.mueller@gs-badkoetzting.de', '+49 9941 90013', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    
    -- Teachers with specific status (INACTIVE) or balance (CREDIT/DEBT)
    (112, 1, 'Hanna', 'Reischl', 'hanna.reischl@gs-innstadt-inactive.de', '+49 851 123458', FALSE, 'INACTIVE_THIS_YEAR', 'FLEXIBLE', 0, NOW(), NOW()),
    (113, 22, 'Stefan', 'Kager', 'stefan.kager@ms-bogen-inactive.de', '+49 9422 9662', FALSE, 'INACTIVE_THIS_YEAR', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (114, 30, 'Ulrich', 'Schramm', 'ulrich.schramm@ms-viechtach.de', '+49 9942 9404', TRUE, 'ACTIVE', 'GRADES_5_TO_9', -1, NOW(), NOW()),
    -- =========================================================================
    -- GROUP 1: MISSING MIDDLE SCHOOL TEACHERS (Target: Reach 41 Total)
    -- Assigned to new Zone 3 MS Schools (IDs 76-85)
    -- =========================================================================
    (115, 76, 'Martin', 'Gruber', 'martin.gruber@ms-regen.de', '+49 9921 55501', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (116, 77, 'Renate', 'Bichler', 'renate.bichler@ms-zwiesel.de', '+49 9922 55502', TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (117, 78, 'Joachim', 'Eder', 'joachim.eder@ms-osterhofen.de', '+49 9932 55503', FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (118, 79, 'Cornelia', 'Stark', 'cornelia.stark@ms-landau.de', NULL, FALSE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (119, 80, 'Dieter', 'Schwarz', 'dieter.schwarz@ms-dingolfing.de', '+49 8731 55504', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (120, 81, 'Brigitte', 'Lang', 'brigitte.lang@ms-vilsbiburg.de', NULL, TRUE, 'ACTIVE', 'GRADES_5_TO_9', 0, NOW(), NOW()),
    (121, 82, 'Horst', 'Winkler', 'horst.winkler@ms-eggenfelden.de', '+49 8721 55505', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),

    -- =========================================================================
    -- GROUP 2: PRIMARY TEACHERS - REGEN / DEGGENDORF AREA (Zone 3)
    -- Assigned to Schools 54-60
    -- =========================================================================
    (122, 54, 'Monika', 'Fuchs', 'monika.fuchs@gs-regen-n.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (123, 54, 'Peter', 'Graf', 'peter.graf@gs-regen-n.de', '+49 9921 6601', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (124, 55, 'Julia', 'Hofer', 'julia.hofer@gs-kirchdorf.de', NULL, TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (125, 55, 'Karin', 'Moos', 'karin.moos@gs-kirchdorf.de', '+49 8571 6602', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (126, 56, 'Stefan', 'Rieger', 'stefan.rieger@gs-eichendorf.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (127, 56, 'Andrea', 'Wolf', 'andrea.wolf@gs-eichendorf.de', '+49 9952 6603', TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (128, 56, 'Thomas', 'Binder', 'thomas.binder@gs-eichendorf.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (129, 57, 'Sabine', 'Eich', 'sabine.eich@gs-frontenhausen.de', '+49 8732 6604', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (130, 57, 'Markus', 'Kurz', 'markus.kurz@gs-frontenhausen.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (131, 58, 'Bettina', 'Schenk', 'bettina.schenk@gs-wallersdorf.de', '+49 9933 6605', TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (132, 58, 'Christian', 'Mayr', 'christian.mayr@gs-wallersdorf.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (133, 59, 'Daniela', 'Hauser', 'daniela.hauser@gs-pilsting.de', '+49 9953 6606', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (134, 59, 'Erich', 'Brandt', 'erich.brandt@gs-pilsting.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (135, 60, 'Florian', 'Huber', 'florian.huber@gs-reisbach.de', '+49 8734 6607', TRUE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (136, 60, 'Gabriele', 'Wirt', 'gabriele.wirt@gs-reisbach.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (137, 60, 'Hannes', 'Koch', 'hannes.koch@gs-reisbach.de', '+49 8734 6608', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),

    -- =========================================================================
    -- GROUP 3: PRIMARY TEACHERS - STRAUBING / LANDSHUT REMOTE (Zone 3)
    -- Assigned to Schools 61-66
    -- =========================================================================
    (138, 61, 'Ingrid', 'Bauer', 'ingrid.bauer@gs-mengkofen.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (139, 61, 'Jürgen', 'Weiss', 'juergen.weiss@gs-mengkofen.de', '+49 8733 7701', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (140, 62, 'Katja', 'Diem', 'katja.diem@gs-geiselhoering.de', NULL, TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (141, 62, 'Lothar', 'Frank', 'lothar.frank@gs-geiselhoering.de', '+49 9423 7702', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (142, 62, 'Manuela', 'Götz', 'manuela.goetz@gs-geiselhoering.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (143, 63, 'Norbert', 'Hess', 'norbert.hess@gs-mallersdorf.de', '+49 8772 7703', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (144, 63, 'Oliver', 'Jung', 'oliver.jung@gs-mallersdorf.de', NULL, TRUE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (145, 64, 'Petra', 'Kraft', 'petra.kraft@gs-rottenburg.de', '+49 8781 7704', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (146, 64, 'Quirin', 'Lutz', 'quirin.lutz@gs-rottenburg.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (147, 65, 'Ralf', 'Moser', 'ralf.moser@gs-ergoldsbach.de', '+49 8771 7705', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (148, 65, 'Sandra', 'Noll', 'sandra.noll@gs-ergoldsbach.de', NULL, TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (149, 65, 'Tobias', 'Ott', 'tobias.ott@gs-ergoldsbach.de', '+49 8771 7706', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (150, 66, 'Ursula', 'Pohl', 'ursula.pohl@gs-neufahrn.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (151, 66, 'Viktor', 'Quast', 'viktor.quast@gs-neufahrn.de', '+49 8773 7707', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),

    -- =========================================================================
    -- GROUP 4: PRIMARY TEACHERS - ROTTAL-INN / PASSAU REMOTE (Zone 3)
    -- Assigned to Schools 67-75 & 97-100
    -- =========================================================================
    (152, 67, 'Walter', 'Raab', 'walter.raab@gs-bayerbach.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (153, 67, 'Xaver', 'Senn', 'xaver.senn@gs-bayerbach.de', '+49 8532 8801', TRUE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (154, 68, 'Yvonne', 'Thal', 'yvonne.thal@gs-ruhstorf.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (155, 68, 'Zeno', 'Uhl', 'zeno.uhl@gs-ruhstorf.de', '+49 8531 8802', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (156, 68, 'Anna', 'Vogt', 'anna.vogt@gs-ruhstorf.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (157, 69, 'Bernd', 'Wahl', 'bernd.wahl@gs-rotthal.de', '+49 8533 8803', TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (158, 69, 'Clara', 'Zahn', 'clara.zahn@gs-rotthal.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (159, 70, 'David', 'Alt', 'david.alt@gs-koesslarn.de', '+49 8536 8804', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (160, 70, 'Eva', 'Berg', 'eva.berg@gs-koesslarn.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (161, 71, 'Franz', 'Cohr', 'franz.cohr@gs-tann.de', '+49 8572 8805', TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (162, 71, 'Gabi', 'Dorn', 'gabi.dorn@gs-tann.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (163, 72, 'Hans', 'Eber', 'hans.eber@gs-wurmannsquick.de', '+49 8725 8806', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (164, 72, 'Ines', 'Funk', 'ines.funk@gs-wurmannsquick.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (165, 73, 'Jakob', 'Gast', 'jakob.gast@gs-zeilarn.de', '+49 8572 8807', TRUE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (166, 73, 'Klara', 'Heim', 'klara.heim@gs-zeilarn.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (167, 74, 'Lukas', 'Immer', 'lukas.immer@gs-gangkofen.de', '+49 8722 8808', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (168, 74, 'Maria', 'Jost', 'maria.jost@gs-gangkofen.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (169, 74, 'Nils', 'Karp', 'nils.karp@gs-gangkofen.de', '+49 8722 8809', TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (170, 75, 'Olga', 'Link', 'olga.link@gs-massing.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (171, 75, 'Paul', 'Mertz', 'paul.mertz@gs-massing.de', '+49 8724 8810', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (172, 97, 'Quinn', 'Noack', 'quinn.noack@gs-kirchham.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (173, 97, 'Rita', 'Opitz', 'rita.opitz@gs-kirchham.de', '+49 8533 9901', TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (174, 98, 'Simon', 'Patz', 'simon.patz@gs-malching.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (175, 98, 'Tina', 'Quade', 'tina.quade@gs-malching.de', '+49 8573 9902', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (176, 99, 'Udo', 'Ranz', 'udo.ranz@gs-ering.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (177, 99, 'Vera', 'Salm', 'vera.salm@gs-ering.de', '+49 8573 9903', TRUE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),

    -- =========================================================================
    -- GROUP 5: PRIMARY FILLERS - ZONE 2 (To reach exactly 169 Primary)
    -- Assigned to existing Zone 2 schools to ensure we hit the 210 total
    -- =========================================================================
    (178, 14, 'Willi', 'Thiel', 'willi.thiel@gs-tittling.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (179, 14, 'Xenia', 'Ulrich', 'xenia.ulrich@gs-tittling.de', '+49 8504 5511', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (180, 16, 'Yannik', 'Vogel', 'yannik.vogel@gs-vilshofen-s.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (181, 16, 'Zara', 'Weber', 'zara.weber@gs-vilshofen-s.de', '+49 8541 5512', TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (182, 17, 'Adam', 'Xander', 'adam.xander@gs-schoellnach.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (183, 17, 'Bea', 'Yilmaz', 'bea.yilmaz@gs-schoellnach.de', '+49 9903 5513', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (184, 19, 'Carl', 'Zeller', 'carl.zeller@gs-aidenbach.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (185, 19, 'Dora', 'Arnold', 'dora.arnold@gs-aidenbach.de', '+49 8543 5514', TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (186, 20, 'Emil', 'Bach', 'emil.bach@gs-osterhofen.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (187, 20, 'Fay', 'Busch', 'fay.busch@gs-osterhofen.de', '+49 9932 5515', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (188, 21, 'Gero', 'Christ', 'gero.christ@gs-waldkirchen.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (189, 21, 'Hana', 'Diehl', 'hana.diehl@gs-waldkirchen.de', '+49 8581 5516', TRUE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (190, 23, 'Ivan', 'Ebert', 'ivan.ebert@gs-deggendorf-m.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (191, 23, 'Jana', 'Fiedler', 'jana.fiedler@gs-deggendorf-m.de', '+49 991 5517', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (192, 24, 'Kai', 'Geier', 'kai.geier@gs-badgriesbach.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (193, 24, 'Lara', 'Hein', 'lara.hein@gs-badgriesbach.de', '+49 8532 5518', TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (194, 25, 'Milo', 'Ilg', 'milo.ilg@gs-fuerstenstein.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (195, 25, 'Nora', 'Jahn', 'nora.jahn@gs-fuerstenstein.de', '+49 8504 5519', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (196, 26, 'Ole', 'Keil', 'ole.keil@gs-roehrnbach.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (197, 26, 'Pia', 'Lenz', 'pia.lenz@gs-roehrnbach.de', '+49 8582 5520', TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (198, 27, 'Rico', 'Mai', 'rico.mai@gs-kellberg.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (199, 27, 'Sina', 'Neubauer', 'sina.neubauer@gs-kellberg.de', '+49 8503 5521', FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (200, 28, 'Tim', 'Ochs', 'tim.ochs@gs-witzmannsberg.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (201, 28, 'Uta', 'Paul', 'uta.paul@gs-witzmannsberg.de', '+49 8504 5522', TRUE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (202, 86, 'Vito', 'Reimann', 'vito.reimann@gs-tiefenbach.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (203, 86, 'Wanda', 'Sauter', 'wanda.sauter@gs-tiefenbach.de', '+49 8509 5523', FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (204, 87, 'Xaver', 'Tietz', 'xaver.tietz@gs-salzweg.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (205, 87, 'Yara', 'Unger', 'yara.unger@gs-salzweg.de', '+49 851 5524', TRUE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (206, 88, 'Zack', 'Vogt', 'zack.vogt@gs-buechlberg.de', NULL, FALSE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (207, 88, 'Alya', 'Wenz', 'alya.wenz@gs-buechlberg.de', '+49 8505 5525', FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW()),
    (208, 89, 'Bodo', 'Zink', 'bodo.zink@gs-hutthurm.de', NULL, FALSE, 'ACTIVE', 'FLEXIBLE', 0, NOW(), NOW()),
    (209, 89, 'Cora', 'Adam', 'cora.adam@gs-hutthurm.de', '+49 8505 5526', TRUE, 'ACTIVE', 'GRADES_1_2', 0, NOW(), NOW()),
    (210, 90, 'Dino', 'Beck', 'dino.beck@gs-fuerstenzell.de', NULL, FALSE, 'ACTIVE', 'GRADES_3_4', 0, NOW(), NOW());

-- 8. TEACHER_SUBJECT
INSERT INTO TEACHER_SUBJECTS (id, year_id, teacher_id, subject_id, availability_status, created_at, updated_at)
VALUES
    -- User's Original Examples (IDs 1-5)
    (1, 1, 1, 1, 'AVAILABLE', NOW(), NOW()), -- Hans (Primary) teaches German (Primary)
    (2, 1, 1, 2, 'AVAILABLE', NOW(), NOW()), -- Hans (Primary) teaches Math (Primary)
    (3, 1, 2, 4, 'AVAILABLE', NOW(), NOW()), -- Anna (Primary) teaches Catholic Religion (Primary)
    (4, 1, 3, 3, 'AVAILABLE', NOW(), NOW()), -- Peter (Middle) teaches English (Middle)
    (5, 1, 4, 1, 'AVAILABLE', NOW(), NOW()), -- Julia (Primary) teaches German (Primary)

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
    (426, 1, 114, 8, 'AVAILABLE', NOW(), NOW()),

    -- =========================================================================
    -- NEW GENERATED DATA (Teacher IDs 115-210, Assignment IDs 427-649)
    -- =========================================================================
    
    -- Teacher 115 (Martin Gruber, MS Regen) - German/History
    (427, 1, 115, 21, 'AVAILABLE', NOW(), NOW()), -- German (MS)
    (428, 1, 115, 10, 'AVAILABLE', NOW(), NOW()), -- History
    (429, 1, 115, 8, 'AVAILABLE', NOW(), NOW()),  -- Social Studies

    -- Teacher 116 (Renate Bichler, MS Zwiesel) - Math/PCB
    (430, 1, 116, 22, 'AVAILABLE', NOW(), NOW()), -- Math (MS)
    (431, 1, 116, 15, 'AVAILABLE', NOW(), NOW()), -- PCB
    (432, 1, 116, 16, 'AVAILABLE', NOW(), NOW()), -- IT

    -- Teacher 117 (Joachim Eder, MS Osterhofen) - English/Geography
    (433, 1, 117, 3, 'AVAILABLE', NOW(), NOW()),  -- English (MS)
    (434, 1, 117, 11, 'AVAILABLE', NOW(), NOW()), -- Geography
    (435, 1, 117, 17, 'AVAILABLE', NOW(), NOW()), -- GSE

    -- Teacher 118 (Cornelia Stark, MS Landau) - German/English
    (436, 1, 118, 21, 'AVAILABLE', NOW(), NOW()), -- German (MS)
    (437, 1, 118, 3, 'AVAILABLE', NOW(), NOW()),  -- English (MS)

    -- Teacher 119 (Dieter Schwarz, MS Dingolfing) - Math/Work
    (438, 1, 119, 22, 'AVAILABLE', NOW(), NOW()), -- Math (MS)
    (439, 1, 119, 13, 'AVAILABLE', NOW(), NOW()), -- AL (Work)

    -- Teacher 120 (Brigitte Lang, MS Vilsbiburg) - German/Politics
    (440, 1, 120, 21, 'AVAILABLE', NOW(), NOW()), -- German (MS)
    (441, 1, 120, 9, 'AVAILABLE', NOW(), NOW()),  -- PuG

    -- Teacher 121 (Horst Winkler, MS Eggenfelden) - Math/Physics
    (442, 1, 121, 22, 'AVAILABLE', NOW(), NOW()), -- Math (MS)
    (443, 1, 121, 15, 'AVAILABLE', NOW(), NOW()), -- PCB

    -- Teacher 122 (Monika Fuchs, GS Regen-N) - Standard Class Teacher
    (444, 1, 122, 1, 'AVAILABLE', NOW(), NOW()),  -- German
    (445, 1, 122, 2, 'AVAILABLE', NOW(), NOW()),  -- Math
    (446, 1, 122, 12, 'AVAILABLE', NOW(), NOW()), -- HSU

    -- Teacher 123 (Peter Graf, GS Regen-N) - English Focus
    (447, 1, 123, 23, 'AVAILABLE', NOW(), NOW()), -- English (Primary)
    (448, 1, 123, 12, 'AVAILABLE', NOW(), NOW()), -- HSU
    (449, 1, 123, 7, 'AVAILABLE', NOW(), NOW()),  -- Sport

    -- Teacher 124 (Julia Hofer, GS Kirchdorf) - Religion Focus
    (450, 1, 124, 1, 'AVAILABLE', NOW(), NOW()),  -- German
    (451, 1, 124, 4, 'AVAILABLE', NOW(), NOW()),  -- Catholic Religion
    (452, 1, 124, 6, 'AVAILABLE', NOW(), NOW()),  -- Art

    -- Teacher 125 (Karin Moos, GS Kirchdorf) - Math Focus
    (453, 1, 125, 2, 'AVAILABLE', NOW(), NOW()),  -- Math
    (454, 1, 125, 12, 'AVAILABLE', NOW(), NOW()), -- HSU
    (455, 1, 125, 5, 'AVAILABLE', NOW(), NOW()),  -- Music

    -- Teacher 126 (Stefan Rieger, GS Eichendorf)
    (456, 1, 126, 1, 'AVAILABLE', NOW(), NOW()),
    (457, 1, 126, 2, 'AVAILABLE', NOW(), NOW()),
    (458, 1, 126, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 127 (Andrea Wolf, GS Eichendorf)
    (459, 1, 127, 23, 'AVAILABLE', NOW(), NOW()),
    (460, 1, 127, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 128 (Thomas Binder, GS Eichendorf)
    (461, 1, 128, 1, 'AVAILABLE', NOW(), NOW()),
    (462, 1, 128, 20, 'AVAILABLE', NOW(), NOW()), -- SSE

    -- Teacher 129 (Sabine Eich, GS Frontenhausen)
    (463, 1, 129, 2, 'AVAILABLE', NOW(), NOW()),
    (464, 1, 129, 12, 'AVAILABLE', NOW(), NOW()),
    (465, 1, 129, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 130 (Markus Kurz, GS Frontenhausen)
    (466, 1, 130, 1, 'AVAILABLE', NOW(), NOW()),
    (467, 1, 130, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 131 (Bettina Schenk, GS Wallersdorf)
    (468, 1, 131, 23, 'AVAILABLE', NOW(), NOW()),
    (469, 1, 131, 1, 'AVAILABLE', NOW(), NOW()),
    (470, 1, 131, 5, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 132 (Christian Mayr, GS Wallersdorf)
    (471, 1, 132, 2, 'AVAILABLE', NOW(), NOW()),
    (472, 1, 132, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 133 (Daniela Hauser, GS Pilsting)
    (473, 1, 133, 1, 'AVAILABLE', NOW(), NOW()),
    (474, 1, 133, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 134 (Erich Brandt, GS Pilsting)
    (475, 1, 134, 12, 'AVAILABLE', NOW(), NOW()),
    (476, 1, 134, 2, 'AVAILABLE', NOW(), NOW()),
    (477, 1, 134, 24, 'AVAILABLE', NOW(), NOW()), -- SPAD

    -- Teacher 135 (Florian Huber, GS Reisbach)
    (478, 1, 135, 1, 'AVAILABLE', NOW(), NOW()),
    (479, 1, 135, 23, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 136 (Gabriele Wirt, GS Reisbach)
    (480, 1, 136, 12, 'AVAILABLE', NOW(), NOW()),
    (481, 1, 136, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 137 (Hannes Koch, GS Reisbach)
    (482, 1, 137, 1, 'AVAILABLE', NOW(), NOW()),
    (483, 1, 137, 2, 'AVAILABLE', NOW(), NOW()),
    (484, 1, 137, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 138 (Ingrid Bauer, GS Mengkofen)
    (485, 1, 138, 2, 'AVAILABLE', NOW(), NOW()),
    (486, 1, 138, 12, 'AVAILABLE', NOW(), NOW()),
    (487, 1, 138, 6, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 139 (Jürgen Weiss, GS Mengkofen)
    (488, 1, 139, 1, 'AVAILABLE', NOW(), NOW()),
    (489, 1, 139, 23, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 140 (Katja Diem, GS Geiselhöring)
    (490, 1, 140, 12, 'AVAILABLE', NOW(), NOW()),
    (491, 1, 140, 5, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 141 (Lothar Frank, GS Geiselhöring)
    (492, 1, 141, 1, 'AVAILABLE', NOW(), NOW()),
    (493, 1, 141, 2, 'AVAILABLE', NOW(), NOW()),
    (494, 1, 141, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 142 (Manuela Götz, GS Geiselhöring)
    (495, 1, 142, 23, 'AVAILABLE', NOW(), NOW()),
    (496, 1, 142, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 143 (Norbert Hess, GS Mallersdorf)
    (497, 1, 143, 1, 'AVAILABLE', NOW(), NOW()),
    (498, 1, 143, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 144 (Oliver Jung, GS Mallersdorf)
    (499, 1, 144, 2, 'AVAILABLE', NOW(), NOW()),
    (500, 1, 144, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 145 (Petra Kraft, GS Rottenburg)
    (501, 1, 145, 1, 'AVAILABLE', NOW(), NOW()),
    (502, 1, 145, 12, 'AVAILABLE', NOW(), NOW()),
    (503, 1, 145, 20, 'AVAILABLE', NOW(), NOW()), -- SSE

    -- Teacher 146 (Quirin Lutz, GS Rottenburg)
    (504, 1, 146, 1, 'AVAILABLE', NOW(), NOW()),
    (505, 1, 146, 2, 'AVAILABLE', NOW(), NOW()),
    (506, 1, 146, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 147 (Ralf Moser, GS Ergoldsbach)
    (507, 1, 147, 23, 'AVAILABLE', NOW(), NOW()),
    (508, 1, 147, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 148 (Sandra Noll, GS Ergoldsbach)
    (509, 1, 148, 1, 'AVAILABLE', NOW(), NOW()),
    (510, 1, 148, 2, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 149 (Tobias Ott, GS Ergoldsbach)
    (511, 1, 149, 12, 'AVAILABLE', NOW(), NOW()),
    (512, 1, 149, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 150 (Ursula Pohl, GS Neufahrn)
    (513, 1, 150, 1, 'AVAILABLE', NOW(), NOW()),
    (514, 1, 150, 2, 'AVAILABLE', NOW(), NOW()),
    (515, 1, 150, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 151 (Viktor Quast, GS Neufahrn)
    (516, 1, 151, 23, 'AVAILABLE', NOW(), NOW()),
    (517, 1, 151, 6, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 152 (Walter Raab, GS Bayerbach)
    (518, 1, 152, 1, 'AVAILABLE', NOW(), NOW()),
    (519, 1, 152, 12, 'AVAILABLE', NOW(), NOW()),
    (520, 1, 152, 24, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 153 (Xaver Senn, GS Bayerbach)
    (521, 1, 153, 2, 'AVAILABLE', NOW(), NOW()),
    (522, 1, 153, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 154 (Yvonne Thal, GS Ruhstorf)
    (523, 1, 154, 1, 'AVAILABLE', NOW(), NOW()),
    (524, 1, 154, 2, 'AVAILABLE', NOW(), NOW()),
    (525, 1, 154, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 155 (Zeno Uhl, GS Ruhstorf)
    (526, 1, 155, 23, 'AVAILABLE', NOW(), NOW()),
    (527, 1, 155, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 156 (Anna Vogt, GS Ruhstorf)
    (528, 1, 156, 1, 'AVAILABLE', NOW(), NOW()),
    (529, 1, 156, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 157 (Bernd Wahl, GS Rotthalmünster)
    (530, 1, 157, 12, 'AVAILABLE', NOW(), NOW()),
    (531, 1, 157, 5, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 158 (Clara Zahn, GS Rotthalmünster)
    (532, 1, 158, 1, 'AVAILABLE', NOW(), NOW()),
    (533, 1, 158, 2, 'AVAILABLE', NOW(), NOW()),
    (534, 1, 158, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 159 (David Alt, GS Kößlarn)
    (535, 1, 159, 23, 'AVAILABLE', NOW(), NOW()),
    (536, 1, 159, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 160 (Eva Berg, GS Kößlarn)
    (537, 1, 160, 1, 'AVAILABLE', NOW(), NOW()),
    (538, 1, 160, 2, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 161 (Franz Cohr, GS Tann)
    (539, 1, 161, 12, 'AVAILABLE', NOW(), NOW()),
    (540, 1, 161, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 162 (Gabi Dorn, GS Tann)
    (541, 1, 162, 1, 'AVAILABLE', NOW(), NOW()),
    (542, 1, 162, 2, 'AVAILABLE', NOW(), NOW()),
    (543, 1, 162, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 163 (Hans Eber, GS Wurmannsquick)
    (544, 1, 163, 1, 'AVAILABLE', NOW(), NOW()),
    (545, 1, 163, 23, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 164 (Ines Funk, GS Wurmannsquick)
    (546, 1, 164, 12, 'AVAILABLE', NOW(), NOW()),
    (547, 1, 164, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 165 (Jakob Gast, GS Zeilarn)
    (548, 1, 165, 1, 'AVAILABLE', NOW(), NOW()),
    (549, 1, 165, 2, 'AVAILABLE', NOW(), NOW()),
    (550, 1, 165, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 166 (Klara Heim, GS Zeilarn)
    (551, 1, 166, 23, 'AVAILABLE', NOW(), NOW()),
    (552, 1, 166, 6, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 167 (Lukas Immer, GS Gangkofen)
    (553, 1, 167, 12, 'AVAILABLE', NOW(), NOW()),
    (554, 1, 167, 1, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 168 (Maria Jost, GS Gangkofen)
    (555, 1, 168, 2, 'AVAILABLE', NOW(), NOW()),
    (556, 1, 168, 12, 'AVAILABLE', NOW(), NOW()),
    (557, 1, 168, 20, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 169 (Nils Karp, GS Gangkofen)
    (558, 1, 169, 1, 'AVAILABLE', NOW(), NOW()),
    (559, 1, 169, 23, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 170 (Olga Link, GS Massing)
    (560, 1, 170, 12, 'AVAILABLE', NOW(), NOW()),
    (561, 1, 170, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 171 (Paul Mertz, GS Massing)
    (562, 1, 171, 1, 'AVAILABLE', NOW(), NOW()),
    (563, 1, 171, 2, 'AVAILABLE', NOW(), NOW()),
    (564, 1, 171, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 172 (Quinn Noack, GS Kirchham)
    (565, 1, 172, 23, 'AVAILABLE', NOW(), NOW()),
    (566, 1, 172, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 173 (Rita Opitz, GS Kirchham)
    (567, 1, 173, 1, 'AVAILABLE', NOW(), NOW()),
    (568, 1, 173, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 174 (Simon Patz, GS Malching)
    (569, 1, 174, 12, 'AVAILABLE', NOW(), NOW()),
    (570, 1, 174, 2, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 175 (Tina Quade, GS Malching)
    (571, 1, 175, 1, 'AVAILABLE', NOW(), NOW()),
    (572, 1, 175, 23, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 176 (Udo Ranz, GS Ering)
    (573, 1, 176, 1, 'AVAILABLE', NOW(), NOW()),
    (574, 1, 176, 2, 'AVAILABLE', NOW(), NOW()),
    (575, 1, 176, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 177 (Vera Salm, GS Ering)
    (576, 1, 177, 12, 'AVAILABLE', NOW(), NOW()),
    (577, 1, 177, 5, 'AVAILABLE', NOW(), NOW()),

    -- =========================================================================
    -- FILLER TEACHERS FOR ZONE 2 (IDs 178-210)
    -- Needed to reach 210 count (GS Tittling, Vilshofen, etc.)
    -- =========================================================================

    -- Teacher 178 (Willi Thiel, GS Tittling)
    (578, 1, 178, 1, 'AVAILABLE', NOW(), NOW()),
    (579, 1, 178, 2, 'AVAILABLE', NOW(), NOW()),
    (580, 1, 178, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 179 (Xenia Ulrich, GS Tittling)
    (581, 1, 179, 23, 'AVAILABLE', NOW(), NOW()),
    (582, 1, 179, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 180 (Yannik Vogel, GS Vilshofen-S)
    (583, 1, 180, 12, 'AVAILABLE', NOW(), NOW()),
    (584, 1, 180, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 181 (Zara Weber, GS Vilshofen-S)
    (585, 1, 181, 1, 'AVAILABLE', NOW(), NOW()),
    (586, 1, 181, 2, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 182 (Adam Xander, GS Schöllnach)
    (587, 1, 182, 1, 'AVAILABLE', NOW(), NOW()),
    (588, 1, 182, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 183 (Bea Yilmaz, GS Schöllnach)
    (589, 1, 183, 23, 'AVAILABLE', NOW(), NOW()),
    (590, 1, 183, 6, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 184 (Carl Zeller, GS Aidenbach)
    (591, 1, 184, 2, 'AVAILABLE', NOW(), NOW()),
    (592, 1, 184, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 185 (Dora Arnold, GS Aidenbach)
    (593, 1, 185, 1, 'AVAILABLE', NOW(), NOW()),
    (594, 1, 185, 23, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 186 (Emil Bach, GS Osterhofen)
    (595, 1, 186, 12, 'AVAILABLE', NOW(), NOW()),
    (596, 1, 186, 5, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 187 (Fay Busch, GS Osterhofen)
    (597, 1, 187, 1, 'AVAILABLE', NOW(), NOW()),
    (598, 1, 187, 2, 'AVAILABLE', NOW(), NOW()),
    (599, 1, 187, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 188 (Gero Christ, GS Waldkirchen)
    (600, 1, 188, 23, 'AVAILABLE', NOW(), NOW()),
    (601, 1, 188, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 189 (Hana Diehl, GS Waldkirchen)
    (602, 1, 189, 1, 'AVAILABLE', NOW(), NOW()),
    (603, 1, 189, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 190 (Ivan Ebert, GS Deggendorf-M)
    (604, 1, 190, 2, 'AVAILABLE', NOW(), NOW()),
    (605, 1, 190, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 191 (Jana Fiedler, GS Deggendorf-M)
    (606, 1, 191, 1, 'AVAILABLE', NOW(), NOW()),
    (607, 1, 191, 23, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 192 (Kai Geier, GS Bad Griesbach)
    (608, 1, 192, 12, 'AVAILABLE', NOW(), NOW()),
    (609, 1, 192, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 193 (Lara Hein, GS Bad Griesbach)
    (610, 1, 193, 1, 'AVAILABLE', NOW(), NOW()),
    (611, 1, 193, 2, 'AVAILABLE', NOW(), NOW()),
    (612, 1, 193, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 194 (Milo Ilg, GS Fürstenstein)
    (613, 1, 194, 23, 'AVAILABLE', NOW(), NOW()),
    (614, 1, 194, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 195 (Nora Jahn, GS Fürstenstein)
    (615, 1, 195, 1, 'AVAILABLE', NOW(), NOW()),
    (616, 1, 195, 6, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 196 (Ole Keil, GS Röhrnbach)
    (617, 1, 196, 2, 'AVAILABLE', NOW(), NOW()),
    (618, 1, 196, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 197 (Pia Lenz, GS Röhrnbach)
    (619, 1, 197, 1, 'AVAILABLE', NOW(), NOW()),
    (620, 1, 197, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 198 (Rico Mai, GS Kellberg)
    (621, 1, 198, 12, 'AVAILABLE', NOW(), NOW()),
    (622, 1, 198, 23, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 199 (Sina Neubauer, GS Kellberg)
    (623, 1, 199, 1, 'AVAILABLE', NOW(), NOW()),
    (624, 1, 199, 2, 'AVAILABLE', NOW(), NOW()),
    (625, 1, 199, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 200 (Tim Ochs, GS Witzmannsberg)
    (626, 1, 200, 1, 'AVAILABLE', NOW(), NOW()),
    (627, 1, 200, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 201 (Uta Paul, GS Witzmannsberg)
    (628, 1, 201, 2, 'AVAILABLE', NOW(), NOW()),
    (629, 1, 201, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 202 (Vito Reimann, GS Tiefenbach)
    (630, 1, 202, 23, 'AVAILABLE', NOW(), NOW()),
    (631, 1, 202, 1, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 203 (Wanda Sauter, GS Tiefenbach)
    (632, 1, 203, 12, 'AVAILABLE', NOW(), NOW()),
    (633, 1, 203, 5, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 204 (Xaver Tietz, GS Salzweg)
    (634, 1, 204, 1, 'AVAILABLE', NOW(), NOW()),
    (635, 1, 204, 2, 'AVAILABLE', NOW(), NOW()),
    (636, 1, 204, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 205 (Yara Unger, GS Salzweg)
    (637, 1, 205, 23, 'AVAILABLE', NOW(), NOW()),
    (638, 1, 205, 7, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 206 (Zack Vogt, GS Büchlberg)
    (639, 1, 206, 1, 'AVAILABLE', NOW(), NOW()),
    (640, 1, 206, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 207 (Alya Wenz, GS Büchlberg)
    (641, 1, 207, 2, 'AVAILABLE', NOW(), NOW()),
    (642, 1, 207, 23, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 208 (Bodo Zink, GS Hutthurm)
    (643, 1, 208, 12, 'AVAILABLE', NOW(), NOW()),
    (644, 1, 208, 4, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 209 (Cora Adam, GS Hutthurm)
    (645, 1, 209, 1, 'AVAILABLE', NOW(), NOW()),
    (646, 1, 209, 2, 'AVAILABLE', NOW(), NOW()),
    (647, 1, 209, 12, 'AVAILABLE', NOW(), NOW()),

    -- Teacher 210 (Dino Beck, GS Fürstenzell)
    (648, 1, 210, 23, 'AVAILABLE', NOW(), NOW()),
    (649, 1, 210, 1, 'AVAILABLE', NOW(), NOW());

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
    (456, 114, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- =========================================================================
    -- NEW GENERATED DATA (Teacher IDs 115-210)
    -- =========================================================================

    -- T115 (Zone 3, Middle, Full-Time) - Block Only
    (457, 115, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (458, 115, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (459, 115, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (460, 115, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T116 (Zone 3, Middle, Part-Time) - Block Only
    (461, 116, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (462, 116, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (463, 116, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (464, 116, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T117 (Zone 3, Middle, Full-Time) - Block Only
    (465, 117, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (466, 117, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (467, 117, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (468, 117, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T118 (Zone 3, Middle, Full-Time) - Block Only
    (469, 118, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (470, 118, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (471, 118, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (472, 118, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T119 (Zone 3, Middle, Full-Time) - Block Only
    (473, 119, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (474, 119, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (475, 119, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (476, 119, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T120 (Zone 3, Middle, Part-Time) - Block Only
    (477, 120, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (478, 120, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (479, 120, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (480, 120, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T121 (Zone 3, Middle, Full-Time) - Block Only
    (481, 121, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (482, 121, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (483, 121, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (484, 121, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T122 (Zone 3, Primary, Full-Time) - Block Only
    (485, 122, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (486, 122, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (487, 122, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (488, 122, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T123 (Zone 3, Primary, Full-Time) - Block Only
    (489, 123, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (490, 123, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (491, 123, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (492, 123, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T124 (Zone 3, Primary, Part-Time) - Block Only
    (493, 124, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (494, 124, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (495, 124, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (496, 124, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T125 (Zone 3, Primary, Full-Time) - Block Only
    (497, 125, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (498, 125, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (499, 125, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (500, 125, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T126 (Zone 3, Primary, Full-Time) - Block Only
    (501, 126, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (502, 126, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (503, 126, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (504, 126, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T127 (Zone 3, Primary, Part-Time) - Block Only
    (505, 127, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (506, 127, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (507, 127, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (508, 127, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T128 (Zone 3, Primary, Full-Time) - Block Only
    (509, 128, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (510, 128, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (511, 128, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (512, 128, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T129 (Zone 3, Primary, Full-Time) - Block Only
    (513, 129, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (514, 129, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (515, 129, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (516, 129, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T130 (Zone 3, Primary, Full-Time) - Block Only
    (517, 130, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (518, 130, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (519, 130, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (520, 130, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T131 (Zone 3, Primary, Part-Time) - Block Only
    (521, 131, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (522, 131, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (523, 131, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (524, 131, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T132 (Zone 3, Primary, Full-Time) - Block Only
    (525, 132, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (526, 132, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (527, 132, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (528, 132, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T133 (Zone 3, Primary, Full-Time) - Block Only
    (529, 133, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (530, 133, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (531, 133, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (532, 133, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T134 (Zone 3, Primary, Full-Time) - Block Only
    (533, 134, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (534, 134, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (535, 134, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (536, 134, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T135 (Zone 3, Primary, Part-Time) - Block Only
    (537, 135, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (538, 135, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (539, 135, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (540, 135, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T136 (Zone 3, Primary, Full-Time) - Block Only
    (541, 136, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (542, 136, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (543, 136, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (544, 136, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T137 (Zone 3, Primary, Full-Time) - Block Only
    (545, 137, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (546, 137, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (547, 137, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (548, 137, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T138 (Zone 3, Primary, Full-Time) - Block Only
    (549, 138, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (550, 138, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (551, 138, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (552, 138, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T139 (Zone 3, Primary, Full-Time) - Block Only
    (553, 139, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (554, 139, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (555, 139, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (556, 139, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T140 (Zone 3, Primary, Part-Time) - Block Only
    (557, 140, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (558, 140, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (559, 140, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (560, 140, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T141 (Zone 3, Primary, Full-Time) - Block Only
    (561, 141, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (562, 141, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (563, 141, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (564, 141, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T142 (Zone 3, Primary, Full-Time) - Block Only
    (565, 142, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (566, 142, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (567, 142, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (568, 142, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T143 (Zone 3, Primary, Full-Time) - Block Only
    (569, 143, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (570, 143, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (571, 143, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (572, 143, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T144 (Zone 3, Primary, Part-Time) - Block Only
    (573, 144, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (574, 144, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (575, 144, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (576, 144, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T145 (Zone 3, Primary, Full-Time) - Block Only
    (577, 145, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (578, 145, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (579, 145, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (580, 145, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T146 (Zone 3, Primary, Full-Time) - Block Only
    (581, 146, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (582, 146, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (583, 146, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (584, 146, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T147 (Zone 3, Primary, Full-Time) - Block Only
    (585, 147, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (586, 147, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (587, 147, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (588, 147, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T148 (Zone 3, Primary, Part-Time) - Block Only
    (589, 148, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (590, 148, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (591, 148, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (592, 148, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T149 (Zone 3, Primary, Full-Time) - Block Only
    (593, 149, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (594, 149, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (595, 149, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (596, 149, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T150 (Zone 3, Primary, Full-Time) - Block Only
    (597, 150, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (598, 150, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (599, 150, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (600, 150, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T151 (Zone 3, Primary, Full-Time) - Block Only
    (601, 151, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (602, 151, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (603, 151, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (604, 151, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T152 (Zone 3, Primary, Full-Time) - Block Only
    (605, 152, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (606, 152, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (607, 152, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (608, 152, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T153 (Zone 3, Primary, Part-Time) - Block Only
    (609, 153, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (610, 153, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (611, 153, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (612, 153, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T154 (Zone 3, Primary, Full-Time) - Block Only
    (613, 154, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (614, 154, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (615, 154, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (616, 154, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T155 (Zone 3, Primary, Full-Time) - Block Only
    (617, 155, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (618, 155, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (619, 155, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (620, 155, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T156 (Zone 3, Primary, Full-Time) - Block Only
    (621, 156, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (622, 156, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (623, 156, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (624, 156, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T157 (Zone 3, Primary, Part-Time) - Block Only
    (625, 157, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (626, 157, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (627, 157, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (628, 157, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T158 (Zone 3, Primary, Full-Time) - Block Only
    (629, 158, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (630, 158, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (631, 158, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (632, 158, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T159 (Zone 3, Primary, Full-Time) - Block Only
    (633, 159, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (634, 159, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (635, 159, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (636, 159, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T160 (Zone 3, Primary, Full-Time) - Block Only
    (637, 160, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (638, 160, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (639, 160, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (640, 160, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T161 (Zone 3, Primary, Part-Time) - Block Only
    (641, 161, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (642, 161, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (643, 161, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (644, 161, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T162 (Zone 3, Primary, Full-Time) - Block Only
    (645, 162, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (646, 162, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (647, 162, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (648, 162, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T163 (Zone 3, Primary, Full-Time) - Block Only
    (649, 163, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (650, 163, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (651, 163, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (652, 163, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T164 (Zone 3, Primary, Full-Time) - Block Only
    (653, 164, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (654, 164, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (655, 164, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (656, 164, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T165 (Zone 3, Primary, Part-Time) - Block Only
    (657, 165, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (658, 165, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (659, 165, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (660, 165, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T166 (Zone 3, Primary, Full-Time) - Block Only
    (661, 166, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (662, 166, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (663, 166, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (664, 166, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T167 (Zone 3, Primary, Full-Time) - Block Only
    (665, 167, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (666, 167, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (667, 167, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (668, 167, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T168 (Zone 3, Primary, Full-Time) - Block Only
    (669, 168, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (670, 168, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (671, 168, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (672, 168, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T169 (Zone 3, Primary, Part-Time) - Block Only
    (673, 169, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (674, 169, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (675, 169, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (676, 169, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T170 (Zone 3, Primary, Full-Time) - Block Only
    (677, 170, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (678, 170, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (679, 170, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (680, 170, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T171 (Zone 3, Primary, Full-Time) - Block Only
    (681, 171, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (682, 171, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (683, 171, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (684, 171, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T172 (Zone 3, Primary, Full-Time) - Block Only
    (685, 172, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (686, 172, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (687, 172, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (688, 172, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T173 (Zone 3, Primary, Part-Time) - Block Only
    (689, 173, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (690, 173, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (691, 173, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (692, 173, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T174 (Zone 3, Primary, Full-Time) - Block Only
    (693, 174, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (694, 174, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (695, 174, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (696, 174, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T175 (Zone 3, Primary, Full-Time) - Block Only
    (697, 175, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (698, 175, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (699, 175, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (700, 175, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T176 (Zone 3, Primary, Full-Time) - Block Only
    (701, 176, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (702, 176, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (703, 176, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (704, 176, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- T177 (Zone 3, Primary, Part-Time) - Block Only
    (705, 177, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 3: Block preferred', NOW()),
    (706, 177, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 3: Block preferred', NOW()),
    (707, 177, 1, 3, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),
    (708, 177, 1, 4, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 3 constraint', NOW()),

    -- =========================================================================
    -- ZONE 2 FILLERS (Teachers 178-201, 206-207) - Flexible Availability
    -- =========================================================================

    -- T178 (Zone 2, Primary, Full-Time)
    (709, 178, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (710, 178, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (711, 178, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (712, 178, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T179 (Zone 2, Primary, Full-Time)
    (713, 179, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (714, 179, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (715, 179, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (716, 179, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T180 (Zone 2, Primary, Full-Time)
    (717, 180, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (718, 180, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (719, 180, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (720, 180, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T181 (Zone 2, Primary, Part-Time) - Prefer Wednesday, but can be flexible
    (721, 181, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (722, 181, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (723, 181, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (724, 181, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T182 (Zone 2, Primary, Full-Time)
    (725, 182, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (726, 182, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (727, 182, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (728, 182, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T183 (Zone 2, Primary, Full-Time)
    (729, 183, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (730, 183, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (731, 183, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (732, 183, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T184 (Zone 2, Primary, Full-Time)
    (733, 184, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (734, 184, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (735, 184, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (736, 184, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T185 (Zone 2, Primary, Part-Time)
    (737, 185, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (738, 185, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (739, 185, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (740, 185, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T186 (Zone 2, Primary, Full-Time)
    (741, 186, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (742, 186, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (743, 186, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (744, 186, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T187 (Zone 2, Primary, Full-Time)
    (745, 187, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (746, 187, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (747, 187, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (748, 187, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T188 (Zone 2, Primary, Full-Time)
    (749, 188, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (750, 188, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (751, 188, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (752, 188, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T189 (Zone 2, Primary, Part-Time)
    (753, 189, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (754, 189, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (755, 189, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (756, 189, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T190 (Zone 2, Primary, Full-Time)
    (757, 190, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (758, 190, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (759, 190, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (760, 190, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T191 (Zone 2, Primary, Full-Time)
    (761, 191, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (762, 191, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (763, 191, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (764, 191, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T192 (Zone 2, Primary, Full-Time)
    (765, 192, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (766, 192, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (767, 192, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (768, 192, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T193 (Zone 2, Primary, Part-Time)
    (769, 193, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (770, 193, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (771, 193, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (772, 193, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T194 (Zone 2, Primary, Full-Time)
    (773, 194, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (774, 194, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (775, 194, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (776, 194, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T195 (Zone 2, Primary, Full-Time)
    (777, 195, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (778, 195, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (779, 195, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (780, 195, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T196 (Zone 2, Primary, Full-Time)
    (781, 196, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (782, 196, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (783, 196, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (784, 196, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T197 (Zone 2, Primary, Part-Time)
    (785, 197, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (786, 197, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (787, 197, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (788, 197, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T198 (Zone 2, Primary, Full-Time)
    (789, 198, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (790, 198, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (791, 198, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (792, 198, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T199 (Zone 2, Primary, Full-Time)
    (793, 199, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (794, 199, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (795, 199, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (796, 199, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T200 (Zone 2, Primary, Full-Time)
    (797, 200, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (798, 200, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (799, 200, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (800, 200, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T201 (Zone 2, Primary, Part-Time)
    (801, 201, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (802, 201, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (803, 201, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (804, 201, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- =========================================================================
    -- ZONE 1 FILLERS (Teachers 202-205, 208-210) - Wednesday Preferred
    -- =========================================================================

    -- T202 (Zone 1, Primary, Full-Time)
    (805, 202, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (806, 202, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (807, 202, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (808, 202, 1, 4, 'AVAILABLE', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T203 (Zone 1, Primary, Full-Time)
    (809, 203, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (810, 203, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (811, 203, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (812, 203, 1, 4, 'AVAILABLE', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T204 (Zone 1, Primary, Full-Time)
    (813, 204, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (814, 204, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (815, 204, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (816, 204, 1, 4, 'AVAILABLE', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T205 (Zone 1, Primary, Part-Time)
    (817, 205, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1/Part-time constraint', NOW()),
    (818, 205, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1/Part-time constraint', NOW()),
    (819, 205, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (820, 205, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T206 (Zone 2, Primary, Full-Time) - Büchlberg (Flexible)
    (821, 206, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (822, 206, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (823, 206, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (824, 206, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T207 (Zone 2, Primary, Full-Time) - Büchlberg (Flexible)
    (825, 207, 1, 1, 'AVAILABLE', TRUE, 1, 'Zone 2: Flexible', NOW()),
    (826, 207, 1, 2, 'AVAILABLE', TRUE, 2, 'Zone 2: Flexible', NOW()),
    (827, 207, 1, 3, 'AVAILABLE', TRUE, 3, 'Zone 2: Flexible', NOW()),
    (828, 207, 1, 4, 'AVAILABLE', TRUE, 4, 'Zone 2: Flexible', NOW()),

    -- T208 (Zone 1, Primary, Full-Time) - Hutthurm
    (829, 208, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (830, 208, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (831, 208, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (832, 208, 1, 4, 'AVAILABLE', TRUE, 2, 'Zone 1 preference for Wednesday', NOW()),

    -- T209 (Zone 1, Primary, Part-Time) - Hutthurm
    (833, 209, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1/Part-time constraint', NOW()),
    (834, 209, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1/Part-time constraint', NOW()),
    (835, 209, 1, 3, 'PREFERRED', TRUE, 1, 'Part-time preference for Wednesday', NOW()),
    (836, 209, 1, 4, 'PREFERRED', TRUE, 2, 'Part-time preference for Wednesday', NOW()),

    -- T210 (Zone 1, Primary, Full-Time) - Fürstenzell
    (837, 210, 1, 1, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (838, 210, 1, 2, 'NOT_AVAILABLE', FALSE, NULL, 'Zone 1 constraint', NOW()),
    (839, 210, 1, 3, 'PREFERRED', TRUE, 1, 'Zone 1 preference for Wednesday', NOW()),
    (840, 210, 1, 4, 'AVAILABLE', TRUE, 2, 'Zone 1 preference for Wednesday', NOW());

-- 10. INTERNSHIP_DEMANDS
-- High priority: SFP German in Primary schools
INSERT INTO internship_demands (id, academic_year_id, internship_type_id, school_type, subject_id, required_teachers, student_count, is_forecasted, created_at, updated_at)
VALUES
    -- SFP (Summer, Forecasted) - Priority 1
    (1, 1, 4, 'Primary', 1, 10, 40, TRUE, NOW(), NOW()),    -- German
    (2, 1, 4, 'Middle', 3, 5, 20, TRUE, NOW(), NOW()),      -- English
    (3, 1, 1, 'Primary', 1, 15, 30, FALSE, NOW(), NOW()),   -- PDP1 German (Filler)
    (4, 1, 4, 'Primary', 23, 5, 20, TRUE, NOW(), NOW()),    -- English
    (5, 1, 4, 'Primary', 12, 7, 28, TRUE, NOW(), NOW()),    -- HSU
    (6, 1, 4, 'Primary', 2, 6, 24, TRUE, NOW(), NOW()),     -- Math
    (7, 1, 4, 'Primary', 4, 4, 16, TRUE, NOW(), NOW()),     -- Religion
    (8, 1, 4, 'Middle', 10, 2, 8, TRUE, NOW(), NOW()),      -- History
    (9, 1, 4, 'Middle', 22, 2, 8, TRUE, NOW(), NOW()),      -- Math
    (10, 1, 4, 'Middle', 21, 1, 4, TRUE, NOW(), NOW()),     -- German

    -- ZSP (Winter, Fixed) - Priority 2
    (11, 1, 3, 'Primary', 1, 8, 32, FALSE, NOW(), NOW()),   -- German
    (12, 1, 3, 'Middle', 3, 3, 12, FALSE, NOW(), NOW()),    -- English
    (13, 1, 3, 'Primary', 23, 4, 16, FALSE, NOW(), NOW()),  -- English
    (14, 1, 3, 'Primary', 12, 8, 32, FALSE, NOW(), NOW()),  -- HSU
    (15, 1, 3, 'Primary', 2, 7, 28, FALSE, NOW(), NOW()),   -- Math
    (16, 1, 3, 'Primary', 7, 4, 16, FALSE, NOW(), NOW()),   -- Sport
    (17, 1, 3, 'Primary', 4, 3, 12, FALSE, NOW(), NOW()),   -- Religion
    (18, 1, 3, 'Middle', 8, 2, 8, FALSE, NOW(), NOW()),     -- Social Studies
    (19, 1, 3, 'Middle', 15, 2, 8, FALSE, NOW(), NOW()),    -- PCB
    (20, 1, 3, 'Middle', 21, 2, 8, FALSE, NOW(), NOW()),    -- German

    -- PDP 1 (Winter, Block) - Priority 3
    (21, 1, 1, 'Primary', 1, 40, 80, FALSE, NOW(), NOW()),  -- German (General PDP Load)
    (22, 1, 1, 'Middle', 3, 10, 20, FALSE, NOW(), NOW()),   -- English (General PDP Load)

    -- PDP 2 (Summer, Block) - Priority 3
    (23, 1, 2, 'Primary', 1, 49, 98, TRUE, NOW(), NOW()),   -- German (General PDP Load)
    (24, 1, 2, 'Middle', 3, 11, 22, TRUE, NOW(), NOW()),    -- English (General PDP Load)

    -- =========================================================================
    -- 2. ADDITIONAL DEMAND (To reach ~420 Assignments total)
    -- =========================================================================

    -- SFP ADDITIONS (Summer) - Arts, Special, Vocational
    -- Need ~60 more slots here to utilize Zone 1/2 teachers in Summer
    (25, 1, 4, 'Primary', 19, 5, 20, TRUE, NOW(), NOW()),   -- DaZ (German as Second Language)
    (26, 1, 4, 'Primary', 20, 4, 16, TRUE, NOW(), NOW()),   -- SSE (Language Support)
    (27, 1, 4, 'Primary', 5, 6, 24, TRUE, NOW(), NOW()),    -- Music
    (28, 1, 4, 4, 6, 6, 24, TRUE, NOW(), NOW()),            -- Art (KE)
    (29, 1, 4, 'Primary', 7, 8, 32, TRUE, NOW(), NOW()),    -- Sport
    (30, 1, 4, 'Middle', 17, 4, 16, TRUE, NOW(), NOW()),    -- GSE
    (31, 1, 4, 'Middle', 15, 4, 16, TRUE, NOW(), NOW()),    -- PCB
    (32, 1, 4, 'Middle', 16, 4, 16, TRUE, NOW(), NOW()),    -- IT
    (33, 1, 4, 'Middle', 13, 3, 12, TRUE, NOW(), NOW()),    -- AL (Work)
    (34, 1, 4, 'Primary', 1, 12, 48, TRUE, NOW(), NOW()),   -- German (Extra Volume for large schools)

    -- ZSP ADDITIONS (Winter) - Arts, Special, Vocational
    -- Need ~60 more slots here to utilize Zone 1/2 teachers in Winter
    (35, 1, 3, 'Primary', 19, 6, 24, FALSE, NOW(), NOW()),  -- DaZ
    (36, 1, 3, 'Primary', 20, 4, 16, FALSE, NOW(), NOW()),  -- SSE
    (37, 1, 3, 'Primary', 5, 6, 24, FALSE, NOW(), NOW()),   -- Music
    (38, 1, 3, 'Primary', 6, 6, 24, FALSE, NOW(), NOW()),   -- Art
    (39, 1, 3, 'Middle', 11, 4, 16, FALSE, NOW(), NOW()),   -- Geography
    (40, 1, 3, 'Middle', 9, 3, 12, FALSE, NOW(), NOW()),    -- Politics (PuG)
    (41, 1, 3, 'Middle', 14, 3, 12, FALSE, NOW(), NOW()),   -- WiB (Economy)
    (42, 1, 3, 'Middle', 13, 3, 12, FALSE, NOW(), NOW()),   -- AL
    (43, 1, 3, 'Primary', 2, 12, 48, FALSE, NOW(), NOW()),  -- Math (Extra Volume)

    -- PDP 1 ADDITIONS (Winter, Block) - For Zone 3 Teachers
    -- Increasing volume to ensure Zone 3 teachers have assignments
    (44, 1, 1, 'Primary', 2, 25, 50, FALSE, NOW(), NOW()),  -- Math (General PDP Load)
    (45, 1, 1, 'Primary', 12, 15, 30, FALSE, NOW(), NOW()), -- HSU (General PDP Load)
    (46, 1, 1, 'Middle', 22, 8, 16, FALSE, NOW(), NOW()),   -- Math (General PDP Load)
    (47, 1, 1, 'Middle', 21, 5, 10, FALSE, NOW(), NOW()),   -- German (General PDP Load)

    -- PDP 2 ADDITIONS (Summer, Block) - For Zone 3 Teachers
    -- Increasing volume to ensure Zone 3 teachers have assignments
    (48, 1, 2, 'Primary', 2, 30, 60, TRUE, NOW(), NOW()),   -- Math (General PDP Load)
    (49, 1, 2, 'Primary', 12, 20, 40, TRUE, NOW(), NOW()),  -- HSU (General PDP Load)
    (50, 1, 2, 'Middle', 22, 10, 20, TRUE, NOW(), NOW()),   -- Math (General PDP Load)
    (51, 1, 2, 'Middle', 15, 5, 10, TRUE, NOW(), NOW()),    -- PCB (General PDP Load)
    
    -- Extra Buffer for Flexibility
    (52, 1, 4, 'Primary', 25, 4, 16, TRUE, NOW(), NOW());   -- GU (Art/Environment)

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

    -- T87-T98 (Zone 1/2, Full/Part-Time) - Fulfill remaining ZSP/SFP slots
    (201, 1, 87, 3, 3, 4, 'CONFIRMED', FALSE, 'Auto-matched E (ZSP)', NOW(), NOW()),
    (202, 1, 87, 4, 10, 4, 'CONFIRMED', FALSE, 'Auto-matched GE (SFP)', NOW(), NOW()), -- T87 Complete
    (203, 1, 88, 3, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (ZSP)', NOW(), NOW()),
    (204, 1, 88, 4, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (SFP)', NOW(), NOW()), -- T88 Complete
    (205, 1, 89, 3, 2, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (ZSP)', NOW(), NOW()),
    (206, 1, 89, 4, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (SFP)', NOW(), NOW()), -- T89 Complete
    (207, 1, 90, 3, 23, 4, 'CONFIRMED', FALSE, 'Auto-matched E (ZSP)', NOW(), NOW()),
    (208, 1, 90, 4, 5, 4, 'CONFIRMED', FALSE, 'Auto-matched MU (SFP)', NOW(), NOW()), -- T90 Complete
    (209, 1, 91, 3, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (ZSP)', NOW(), NOW()),
    (210, 1, 91, 4, 7, 4, 'CONFIRMED', FALSE, 'Auto-matched SP (SFP)', NOW(), NOW()), -- T91 Complete
    (211, 1, 92, 3, 22, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (ZSP)', NOW(), NOW()),
    (212, 1, 92, 4, 3, 4, 'CONFIRMED', FALSE, 'Auto-matched E (SFP)', NOW(), NOW()), -- T92 Complete
    (213, 1, 93, 3, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (ZSP)', NOW(), NOW()),
    (214, 1, 93, 4, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (SFP)', NOW(), NOW()), -- T93 Complete
    (215, 1, 94, 3, 23, 4, 'CONFIRMED', FALSE, 'Auto-matched E (ZSP)', NOW(), NOW()),
    (216, 1, 94, 4, 7, 4, 'CONFIRMED', FALSE, 'Auto-matched SP (SFP)', NOW(), NOW()), -- T94 Complete
    (217, 1, 95, 3, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (ZSP)', NOW(), NOW()),
    (218, 1, 95, 4, 2, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (SFP)', NOW(), NOW()), -- T95 Complete
    (219, 1, 96, 3, 2, 4, 'CONFIRMED', FALSE, 'Auto-matched MA (ZSP)', NOW(), NOW()),
    (220, 1, 96, 4, 1, 4, 'CONFIRMED', FALSE, 'Auto-matched D (SFP)', NOW(), NOW()), -- T96 Complete
    (221, 1, 97, 3, 23, 4, 'CONFIRMED', FALSE, 'Auto-matched E (ZSP)', NOW(), NOW()),
    (222, 1, 97, 4, 6, 4, 'CONFIRMED', FALSE, 'Auto-matched KE (SFP)', NOW(), NOW()), -- T97 Complete
    (223, 1, 98, 3, 12, 4, 'CONFIRMED', FALSE, 'Auto-matched HSU (ZSP)', NOW(), NOW()),
    (224, 1, 98, 4, 4, 4, 'CONFIRMED', FALSE, 'Auto-matched KRel (SFP)', NOW(), NOW()), -- T98 Complete

    -- =========================================================================
    -- NEW ALLOCATIONS (Assignments 225-420 for Teachers 115-210)
    -- =========================================================================

    -- Zone 3 Teachers (Remote) -> Strict Block Internships (PDP 1 + PDP 2)
    -- Teachers 115-177 (63 Teachers * 2 Assignments = 126 records)
    (225, 1, 115, 1, 21, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (226, 1, 115, 2, 21, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (227, 1, 116, 1, 22, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (228, 1, 116, 2, 22, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (229, 1, 117, 1, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (230, 1, 117, 2, 3, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (231, 1, 118, 1, 21, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (232, 1, 118, 2, 21, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (233, 1, 119, 1, 22, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (234, 1, 119, 2, 22, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (235, 1, 120, 1, 21, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (236, 1, 120, 2, 21, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (237, 1, 121, 1, 22, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (238, 1, 121, 2, 22, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (239, 1, 122, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (240, 1, 122, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (241, 1, 123, 1, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (242, 1, 123, 2, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (243, 1, 124, 1, 4, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (244, 1, 124, 2, 4, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (245, 1, 125, 1, 2, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (246, 1, 125, 2, 2, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (247, 1, 126, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (248, 1, 126, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (249, 1, 127, 1, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (250, 1, 127, 2, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (251, 1, 128, 1, 20, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (252, 1, 128, 2, 20, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (253, 1, 129, 1, 2, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (254, 1, 129, 2, 2, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (255, 1, 130, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (256, 1, 130, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (257, 1, 131, 1, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (258, 1, 131, 2, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (259, 1, 132, 1, 12, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (260, 1, 132, 2, 12, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (261, 1, 133, 1, 4, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (262, 1, 133, 2, 4, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (263, 1, 134, 1, 2, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (264, 1, 134, 2, 2, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (265, 1, 135, 1, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (266, 1, 135, 2, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (267, 1, 136, 1, 7, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (268, 1, 136, 2, 7, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (269, 1, 137, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (270, 1, 137, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (271, 1, 138, 1, 2, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (272, 1, 138, 2, 2, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (273, 1, 139, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (274, 1, 139, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (275, 1, 140, 1, 12, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (276, 1, 140, 2, 12, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (277, 1, 141, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (278, 1, 141, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (279, 1, 142, 1, 7, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (280, 1, 142, 2, 7, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (281, 1, 143, 1, 4, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (282, 1, 143, 2, 4, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (283, 1, 144, 1, 2, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (284, 1, 144, 2, 2, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (285, 1, 145, 1, 20, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (286, 1, 145, 2, 20, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (287, 1, 146, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (288, 1, 146, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (289, 1, 147, 1, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (290, 1, 147, 2, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (291, 1, 148, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (292, 1, 148, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (293, 1, 149, 1, 4, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (294, 1, 149, 2, 4, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (295, 1, 150, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (296, 1, 150, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (297, 1, 151, 1, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (298, 1, 151, 2, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (299, 1, 152, 1, 24, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (300, 1, 152, 2, 24, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (301, 1, 153, 1, 2, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (302, 1, 153, 2, 2, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (303, 1, 154, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (304, 1, 154, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (305, 1, 155, 1, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (306, 1, 155, 2, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (307, 1, 156, 1, 4, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (308, 1, 156, 2, 4, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (309, 1, 157, 1, 12, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (310, 1, 157, 2, 12, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (311, 1, 158, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (312, 1, 158, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (313, 1, 159, 1, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (314, 1, 159, 2, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (315, 1, 160, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (316, 1, 160, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (317, 1, 161, 1, 7, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (318, 1, 161, 2, 7, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (319, 1, 162, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (320, 1, 162, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (321, 1, 163, 1, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (322, 1, 163, 2, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (323, 1, 164, 1, 4, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (324, 1, 164, 2, 4, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (325, 1, 165, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (326, 1, 165, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (327, 1, 166, 1, 6, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (328, 1, 166, 2, 6, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (329, 1, 167, 1, 12, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (330, 1, 167, 2, 12, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (331, 1, 168, 1, 20, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (332, 1, 168, 2, 20, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (333, 1, 169, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (334, 1, 169, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (335, 1, 170, 1, 4, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (336, 1, 170, 2, 4, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (337, 1, 171, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (338, 1, 171, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (339, 1, 172, 1, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (340, 1, 172, 2, 23, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (341, 1, 173, 1, 7, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (342, 1, 173, 2, 7, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (343, 1, 174, 1, 2, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (344, 1, 174, 2, 2, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (345, 1, 175, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (346, 1, 175, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (347, 1, 176, 1, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (348, 1, 176, 2, 1, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),
    (349, 1, 177, 1, 5, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 1', NOW(), NOW()), (350, 1, 177, 2, 5, 2, 'CONFIRMED', FALSE, 'Zone 3: PDP 2', NOW(), NOW()),

    -- Zone 2/Filler Teachers (178-201) -> Flexible SFP/ZSP Assignments (Fill missing subjects)
    (351, 1, 178, 3, 1, 4, 'CONFIRMED', FALSE, 'Filler D (ZSP)', NOW(), NOW()),
    (352, 1, 178, 4, 12, 4, 'CONFIRMED', FALSE, 'Filler HSU (SFP)', NOW(), NOW()),
    (353, 1, 179, 3, 23, 4, 'CONFIRMED', FALSE, 'Filler E (ZSP)', NOW(), NOW()),
    (354, 1, 179, 4, 4, 4, 'CONFIRMED', FALSE, 'Filler KRel (SFP)', NOW(), NOW()),
    (355, 1, 180, 3, 7, 4, 'CONFIRMED', FALSE, 'Filler SP (ZSP)', NOW(), NOW()),
    (356, 1, 180, 4, 12, 4, 'CONFIRMED', FALSE, 'Filler HSU (SFP)', NOW(), NOW()),
    (357, 1, 181, 3, 2, 4, 'CONFIRMED', FALSE, 'Filler MA (ZSP)', NOW(), NOW()),
    (358, 1, 181, 4, 1, 4, 'CONFIRMED', FALSE, 'Filler D (SFP)', NOW(), NOW()),
    (359, 1, 182, 3, 1, 4, 'CONFIRMED', FALSE, 'Filler D (ZSP)', NOW(), NOW()),
    (360, 1, 182, 4, 12, 4, 'CONFIRMED', FALSE, 'Filler HSU (SFP)', NOW(), NOW()),
    (361, 1, 183, 3, 6, 4, 'CONFIRMED', FALSE, 'Filler KE (ZSP)', NOW(), NOW()),
    (362, 1, 183, 4, 23, 4, 'CONFIRMED', FALSE, 'Filler E (SFP)', NOW(), NOW()),
    (363, 1, 184, 3, 12, 4, 'CONFIRMED', FALSE, 'Filler HSU (ZSP)', NOW(), NOW()),
    (364, 1, 184, 4, 2, 4, 'CONFIRMED', FALSE, 'Filler MA (SFP)', NOW(), NOW()),
    (365, 1, 185, 3, 1, 4, 'CONFIRMED', FALSE, 'Filler D (ZSP)', NOW(), NOW()),
    (366, 1, 185, 4, 23, 4, 'CONFIRMED', FALSE, 'Filler E (SFP)', NOW(), NOW()),
    (367, 1, 186, 3, 5, 4, 'CONFIRMED', FALSE, 'Filler MU (ZSP)', NOW(), NOW()),
    (368, 1, 186, 4, 12, 4, 'CONFIRMED', FALSE, 'Filler HSU (SFP)', NOW(), NOW()),
    (369, 1, 187, 3, 2, 4, 'CONFIRMED', FALSE, 'Filler MA (ZSP)', NOW(), NOW()),
    (370, 1, 187, 4, 1, 4, 'CONFIRMED', FALSE, 'Filler D (SFP)', NOW(), NOW()),
    (371, 1, 188, 3, 7, 4, 'CONFIRMED', FALSE, 'Filler SP (ZSP)', NOW(), NOW()),
    (372, 1, 188, 4, 23, 4, 'CONFIRMED', FALSE, 'Filler E (SFP)', NOW(), NOW()),
    (373, 1, 189, 3, 12, 4, 'CONFIRMED', FALSE, 'Filler HSU (ZSP)', NOW(), NOW()),
    (374, 1, 189, 4, 1, 4, 'CONFIRMED', FALSE, 'Filler D (SFP)', NOW(), NOW()),
    (375, 1, 190, 3, 12, 4, 'CONFIRMED', FALSE, 'Filler HSU (ZSP)', NOW(), NOW()),
    (376, 1, 190, 4, 2, 4, 'CONFIRMED', FALSE, 'Filler MA (SFP)', NOW(), NOW()),
    (377, 1, 191, 3, 23, 4, 'CONFIRMED', FALSE, 'Filler E (ZSP)', NOW(), NOW()),
    (378, 1, 191, 4, 1, 4, 'CONFIRMED', FALSE, 'Filler D (SFP)', NOW(), NOW()),
    (379, 1, 192, 3, 4, 4, 'CONFIRMED', FALSE, 'Filler KRel (ZSP)', NOW(), NOW()),
    (380, 1, 192, 4, 12, 4, 'CONFIRMED', FALSE, 'Filler HSU (SFP)', NOW(), NOW()),
    (381, 1, 193, 3, 2, 4, 'CONFIRMED', FALSE, 'Filler MA (ZSP)', NOW(), NOW()),
    (382, 1, 193, 4, 1, 4, 'CONFIRMED', FALSE, 'Filler D (SFP)', NOW(), NOW()),
    (383, 1, 194, 3, 23, 4, 'CONFIRMED', FALSE, 'Filler E (ZSP)', NOW(), NOW()),
    (384, 1, 194, 4, 12, 4, 'CONFIRMED', FALSE, 'Filler HSU (SFP)', NOW(), NOW()),
    (385, 1, 195, 3, 6, 4, 'CONFIRMED', FALSE, 'Filler KE (ZSP)', NOW(), NOW()),
    (386, 1, 195, 4, 1, 4, 'CONFIRMED', FALSE, 'Filler D (SFP)', NOW(), NOW()),
    (387, 1, 196, 3, 12, 4, 'CONFIRMED', FALSE, 'Filler HSU (ZSP)', NOW(), NOW()),
    (388, 1, 196, 4, 2, 4, 'CONFIRMED', FALSE, 'Filler MA (SFP)', NOW(), NOW()),
    (389, 1, 197, 3, 7, 4, 'CONFIRMED', FALSE, 'Filler SP (ZSP)', NOW(), NOW()),
    (390, 1, 197, 4, 1, 4, 'CONFIRMED', FALSE, 'Filler D (SFP)', NOW(), NOW()),
    (391, 1, 198, 3, 23, 4, 'CONFIRMED', FALSE, 'Filler E (ZSP)', NOW(), NOW()),
    (392, 1, 198, 4, 12, 4, 'CONFIRMED', FALSE, 'Filler HSU (SFP)', NOW(), NOW()),
    (393, 1, 199, 3, 2, 4, 'CONFIRMED', FALSE, 'Filler MA (ZSP)', NOW(), NOW()),
    (394, 1, 199, 4, 1, 4, 'CONFIRMED', FALSE, 'Filler D (SFP)', NOW(), NOW()),
    (395, 1, 200, 3, 4, 4, 'CONFIRMED', FALSE, 'Filler KRel (ZSP)', NOW(), NOW()),
    (396, 1, 200, 4, 1, 4, 'CONFIRMED', FALSE, 'Filler D (SFP)', NOW(), NOW()),
    (397, 1, 201, 3, 12, 4, 'CONFIRMED', FALSE, 'Filler HSU (ZSP)', NOW(), NOW()),
    (398, 1, 201, 4, 2, 4, 'CONFIRMED', FALSE, 'Filler MA (SFP)', NOW(), NOW()),

    -- Zone 1/Close Fillers (Teachers 202-210) - Strictly ZSP/SFP
    (399, 1, 202, 3, 23, 4, 'CONFIRMED', FALSE, 'Filler E (ZSP)', NOW(), NOW()),
    (400, 1, 202, 4, 1, 4, 'CONFIRMED', FALSE, 'Filler D (SFP)', NOW(), NOW()),
    (401, 1, 203, 3, 5, 4, 'CONFIRMED', FALSE, 'Filler MU (ZSP)', NOW(), NOW()),
    (402, 1, 203, 4, 12, 4, 'CONFIRMED', FALSE, 'Filler HSU (SFP)', NOW(), NOW()),
    (403, 1, 204, 3, 1, 4, 'CONFIRMED', FALSE, 'Filler D (ZSP)', NOW(), NOW()),
    (404, 1, 204, 4, 2, 4, 'CONFIRMED', FALSE, 'Filler MA (SFP)', NOW(), NOW()),
    (405, 1, 205, 3, 23, 4, 'CONFIRMED', FALSE, 'Filler E (ZSP)', NOW(), NOW()),
    (406, 1, 205, 4, 7, 4, 'CONFIRMED', FALSE, 'Filler SP (SFP)', NOW(), NOW()),
    (407, 1, 206, 3, 1, 4, 'CONFIRMED', FALSE, 'Filler D (ZSP)', NOW(), NOW()),
    (408, 1, 206, 4, 12, 4, 'CONFIRMED', FALSE, 'Filler HSU (SFP)', NOW(), NOW()),
    (409, 1, 207, 3, 2, 4, 'CONFIRMED', FALSE, 'Filler MA (ZSP)', NOW(), NOW()),
    (410, 1, 207, 4, 23, 4, 'CONFIRMED', FALSE, 'Filler E (SFP)', NOW(), NOW()),
    (411, 1, 208, 3, 4, 4, 'CONFIRMED', FALSE, 'Filler KRel (ZSP)', NOW(), NOW()),
    (412, 1, 208, 4, 12, 4, 'CONFIRMED', FALSE, 'Filler HSU (SFP)', NOW(), NOW()),
    (413, 1, 209, 3, 1, 4, 'CONFIRMED', FALSE, 'Filler D (ZSP)', NOW(), NOW()),
    (414, 1, 209, 4, 2, 4, 'CONFIRMED', FALSE, 'Filler MA (SFP)', NOW(), NOW()),
    (415, 1, 210, 3, 23, 4, 'CONFIRMED', FALSE, 'Filler E (ZSP)', NOW(), NOW()),
    (416, 1, 210, 4, 1, 4, 'CONFIRMED', FALSE, 'Filler D (SFP)', NOW(), NOW());

-- 13. CREDIT_HOUR_TRACKING
-- Hans has 2 assignments, so he earns 1.0 credit hour (Reduction hour).
INSERT INTO CREDIT_HOUR_TRACKING (id, teacher_id, academic_year_id, assignments_count, credit_hours_allocated, credit_balance, notes, created_at)
VALUES
    (1, 1, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (2, 2, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (3, 3, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (4, 4, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (5, 5, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (6, 6, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (7, 7, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (8, 8, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (9, 9, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (10, 10, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (11, 11, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (12, 12, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (13, 13, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (14, 14, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (15, 15, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (16, 16, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (17, 17, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (18, 18, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (19, 19, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (20, 20, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (21, 21, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (22, 22, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (23, 23, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (24, 24, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (25, 25, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (26, 26, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (27, 27, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (28, 28, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (29, 29, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (30, 30, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (31, 31, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (32, 32, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (33, 33, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (34, 34, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (35, 35, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (36, 36, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (37, 37, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (38, 38, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (39, 39, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (40, 40, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (41, 41, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (42, 42, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (43, 43, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (44, 44, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (45, 45, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (46, 46, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (47, 47, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (48, 48, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (49, 49, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (50, 50, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (51, 51, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (52, 52, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (53, 53, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (54, 54, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (55, 55, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (56, 56, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (57, 57, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (58, 58, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (59, 59, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (60, 60, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (61, 61, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (62, 62, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (63, 63, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (64, 64, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (65, 65, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (66, 66, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (67, 67, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (68, 68, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (69, 69, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (70, 70, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (71, 71, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (72, 72, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (73, 73, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (74, 74, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (75, 75, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (76, 76, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (77, 77, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (78, 78, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (79, 79, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (80, 80, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (81, 81, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (82, 82, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (83, 83, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (84, 84, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (85, 85, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (86, 86, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (87, 87, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (88, 88, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (89, 89, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (90, 90, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (91, 91, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (92, 92, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (93, 93, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (94, 94, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (95, 95, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (96, 96, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (97, 97, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (98, 98, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (99, 99, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (100, 100, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (101, 101, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (102, 102, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (103, 103, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (104, 104, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (105, 105, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (106, 106, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (107, 107, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (108, 108, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (109, 109, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (110, 110, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (111, 111, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),

    -- =========================================================================
    -- EXCEPTIONS (Inactive / Debt)
    -- =========================================================================
    
    -- T112 (Inactive)
    (112, 112, 1, 0, 0.0, 0.0, 'Teacher inactive for current year', NOW()),
    
    -- T113 (Inactive)
    (113, 113, 1, 0, 0.0, 0.0, 'Teacher inactive for current year', NOW()),
    
    -- T114 (Active but has previous debt of -1.0)
    -- Met quota (2 assignments) for this year. Debt remains in "Teachers" table logic unless cleared.
    (114, 114, 1, 2, 1.0, 0.0, 'Standard allocation met (Previous debt remains)', NOW()),

    -- =========================================================================
    -- NEW GENERATED TEACHERS (IDs 115-210)
    -- Standard Load: 2 Assignments = 1.0 Credit Hour
    -- =========================================================================
    (115, 115, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (116, 116, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (117, 117, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (118, 118, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (119, 119, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (120, 120, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (121, 121, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (122, 122, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (123, 123, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (124, 124, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (125, 125, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (126, 126, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (127, 127, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (128, 128, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (129, 129, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (130, 130, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (131, 131, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (132, 132, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (133, 133, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (134, 134, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (135, 135, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (136, 136, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (137, 137, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (138, 138, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (139, 139, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (140, 140, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (141, 141, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (142, 142, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (143, 143, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (144, 144, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (145, 145, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (146, 146, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (147, 147, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (148, 148, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (149, 149, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (150, 150, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (151, 151, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (152, 152, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (153, 153, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (154, 154, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (155, 155, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (156, 156, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (157, 157, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (158, 158, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (159, 159, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (160, 160, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (161, 161, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (162, 162, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (163, 163, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (164, 164, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (165, 165, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (166, 166, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (167, 167, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (168, 168, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (169, 169, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (170, 170, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (171, 171, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (172, 172, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (173, 173, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (174, 174, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (175, 175, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (176, 176, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (177, 177, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (178, 178, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (179, 179, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (180, 180, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (181, 181, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (182, 182, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (183, 183, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (184, 184, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (185, 185, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (186, 186, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (187, 187, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (188, 188, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (189, 189, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (190, 190, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (191, 191, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (192, 192, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (193, 193, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (194, 194, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (195, 195, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (196, 196, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (197, 197, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (198, 198, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (199, 199, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (200, 200, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (201, 201, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (202, 202, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (203, 203, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (204, 204, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (205, 205, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (206, 206, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (207, 207, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (208, 208, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (209, 209, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW()),
    (210, 210, 1, 2, 1.0, 0.0, 'Standard allocation met', NOW());