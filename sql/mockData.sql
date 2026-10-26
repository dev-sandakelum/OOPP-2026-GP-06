-- AFMS mock data (MySQL 8.x)
-- Run the schema script first, then this one.
-- All users share the password: Password@123 (plain text — bcrypt removed for dev)

USE afms_db;

SET @pw = 'Password@123';

-- users
INSERT INTO users (user_id, name, email, password, phone, address, dob, user_type) VALUES
(1,  'Kasun Wijesinghe',        'kasun.wijesinghe@afms.example.com',   @pw, '+94 71 234 5601', '12 Temple Road, Matara',      '1985-03-14', 'ADMIN'),
(2,  'Dilini Fernando',         'dilini.fernando@afms.example.com',    @pw, '+94 71 234 5602', '45 Beach Road, Galle',        '1988-07-22', 'ADMIN'),
(3,  'Dr. Ruwan Jayasinghe',    'ruwan.jayasinghe@afms.example.com',   @pw, '+94 77 345 6701', '8 Lake View, Matara',         '1979-11-05', 'LECTURER'),
(4,  'Dr. Sanduni Rajapaksha',  'sanduni.rajapaksha@afms.example.com', @pw, '+94 77 345 6702', '23 Hill Street, Galle',        '1982-02-18', 'LECTURER'),
(5,  'Chamara Gunawardena',     'chamara.gunawardena@afms.example.com',@pw, '+94 77 345 6703', '5 Park Lane, Matara',         '1986-09-30', 'LECTURER'),
(6,  'Nadeesha Silva',          'nadeesha.silva@afms.example.com',     @pw, '+94 77 345 6704', '17 Station Road, Weligama',   '1984-06-12', 'LECTURER'),
(7,  'Pradeep Bandara',         'pradeep.bandara@afms.example.com',    @pw, '+94 76 456 7801', '31 Main Street, Akuressa',    '1990-01-25', 'TECHNICAL_OFFICER'),
(8,  'Thilini Herath',          'thilini.herath@afms.example.com',     @pw, '+94 76 456 7802', '9 Garden Road, Matara',       '1992-04-08', 'TECHNICAL_OFFICER'),
(9,  'Kavindu Lakshan',         'kavindu.lakshan@afms.example.com',    @pw, '+94 70 567 8901', '14 School Lane, Matara',      '2003-05-17', 'UNDERGRADUATE'),
(10, 'Sithumi Weerasinghe',     'sithumi.weerasinghe@afms.example.com',@pw, '+94 70 567 8902', '62 Church Road, Galle',        '2003-08-02', 'UNDERGRADUATE'),
(11, 'Dinuka Ranasinghe',       'dinuka.ranasinghe@afms.example.com',  @pw, '+94 70 567 8903', '3 Canal Road, Kamburupitiya', '2002-12-21', 'UNDERGRADUATE'),
(12, 'Ishara Madushani',        'ishara.madushani@afms.example.com',   @pw, '+94 70 567 8904', '27 Flower Road, Hakmana',     '2003-03-09', 'UNDERGRADUATE'),
(13, 'Tharindu Kumara',         'tharindu.kumara@afms.example.com',    @pw, '+94 70 567 8905', '40 Paddy Lane, Deniyaya',     '2003-10-30', 'UNDERGRADUATE'),
(14, 'Amaya Senanayake',        'amaya.senanayake@afms.example.com',   @pw, '+94 70 567 8906', '19 Sea Street, Dikwella',     '2003-01-14', 'UNDERGRADUATE'),
(15, 'Lahiru Dissanayake',      'lahiru.dissanayake@afms.example.com', @pw, '+94 70 567 8907', '56 Market Road, Tangalle',     '2002-09-26', 'UNDERGRADUATE');

-- department
INSERT INTO department (dep_id, dep_name, contact_no) VALUES
(1, 'Information and Communication Technology', '+94 41 222 0001'),
(2, 'Engineering Technology',                   '+94 41 222 0002'),
(3, 'Bio-system Technology',                    '+94 41 222 0003');

-- semester
INSERT INTO semester (semester_id, semester_number, academic_year, start_date, end_date, status) VALUES
(1, 1, '2025/2026', '2025-10-06', '2026-02-13', 'COMPLETED'),
(2, 2, '2025/2026', '2026-03-02', '2026-07-17', 'COMPLETED'),
(3, 1, '2026/2027', '2026-10-05', '2027-02-12', 'ONGOING');

-- timetable
INSERT INTO timetable (timetable_id, academic_year, published_date, status) VALUES
(1, '2025/2026', '2025-09-26', 'PUBLISHED'),
(2, '2026/2027', '2026-09-25', 'PUBLISHED');

-- batch
INSERT INTO batch (batch_id, batch_name, intake_year, dep_id) VALUES
(1, 'ICT Batch 2025', 2025, 1),
(2, 'ET Batch 2025',  2025, 2),
(3, 'BST Batch 2025', 2025, 3);

-- course
INSERT INTO course (course_id, course_name, course_code, credit, dep_id) VALUES
(1, 'Introduction to Programming',  'ICT1113', 3.00, 1),
(2, 'Discrete Mathematics',         'ICT1123', 3.00, 1),
(3, 'Database Management Systems', 'ICT1233', 3.00, 1),
(4, 'Object Oriented Programming',  'ICT1223', 3.00, 1),
(5, 'Engineering Mathematics',      'ET1113',  3.00, 2),
(6, 'Computer Networks',            'ICT2113', 3.00, 1);

-- admin
INSERT INTO admin (user_id) VALUES (1), (2);

-- undergraduate
INSERT INTO undergraduate (user_id, dep_id, batch_id, profile_picture) VALUES
(9,  1, 1, '/uploads/profile/9.jpg'),
(10, 1, 1, '/uploads/profile/10.jpg'),
(11, 1, 1, NULL),
(12, 1, 1, '/uploads/profile/12.jpg'),
(13, 1, 1, NULL),
(14, 2, 2, '/uploads/profile/14.jpg'),
(15, 2, 2, NULL);

-- lecturer
INSERT INTO lecturer (user_id, qualification, dep_id) VALUES
(3, 'PhD in Information Systems',         1),
(4, 'PhD in Computer Science',            1),
(5, 'MSc in Software Engineering',        1),
(6, 'MSc in Engineering Mathematics',     2);

-- technical_officer
INSERT INTO technical_officer (user_id, dep_id) VALUES
(7, 1),
(8, 2);

-- course_offering
INSERT INTO course_offering (offering_id, course_id, semester_id, lecturer_id) VALUES
(1, 1, 1, 3),
(2, 2, 1, 4),
(3, 3, 2, 3),
(4, 4, 2, 5),
(5, 5, 2, 6),
(6, 6, 3, 4);

-- course_material
INSERT INTO course_material (material_id, offering_id, lecturer_id, title, material_type, file_path, file_size, external_link, upload_date) VALUES
(1, 1, 3, 'Lecture 01 - Introduction to Programming', 'LECTURE_NOTES', '/uploads/materials/ict1113_lec01.pdf', 524288,  NULL, '2025-10-07 14:30:00'),
(2, 1, 3, 'Lab Sheet 01',                             'LAB_SHEET',     '/uploads/materials/ict1113_lab01.pdf', 204800,  NULL, '2025-10-20 10:15:00'),
(3, 1, 3, 'Java Tutorial for Beginners',              'LINK',          NULL, NULL, 'https://example.com/java-tutorial', '2025-10-10 09:00:00'),
(4, 2, 4, 'Set Theory Notes',                         'LECTURE_NOTES', '/uploads/materials/ict1123_sets.pdf',  412672,  NULL, '2025-10-09 11:45:00'),
(5, 3, 3, 'Lecture 01 - Introduction to DBMS',        'LECTURE_NOTES', '/uploads/materials/ict1233_lec01.pdf', 638976,  NULL, '2026-03-09 16:20:00'),
(6, 3, 3, 'ER Diagram Tutorial',                      'LINK',          NULL, NULL, 'https://example.com/er-diagrams', '2026-03-12 08:30:00'),
(7, 4, 5, 'OOP Concepts Slides',                      'SLIDES',        '/uploads/materials/ict1223_oop.pptx',  1048576, NULL, '2026-03-11 13:10:00'),
(8, 6, 4, 'Network Models Lecture Notes',             'LECTURE_NOTES', '/uploads/materials/ict2113_lec01.pdf', 786432,  NULL, '2026-10-07 15:00:00');

-- session
INSERT INTO `session` (session_id, offering_id, session_number, session_type, session_date, start_time, end_time, room) VALUES
(1, 1, 1, 'LECTURE',   '2025-10-08', '08:00:00', '10:00:00', 'LH-01'),
(2, 1, 2, 'LECTURE',   '2025-10-15', '08:00:00', '10:00:00', 'LH-01'),
(3, 1, 3, 'PRACTICAL', '2025-10-24', '13:00:00', '16:00:00', 'ICT Lab 1'),
(4, 1, 4, 'LECTURE',   '2025-10-29', '08:00:00', '10:00:00', 'LH-01'),
(5, 3, 1, 'LECTURE',   '2026-03-10', '08:00:00', '10:00:00', 'LH-01'),
(6, 3, 2, 'LECTURE',   '2026-03-17', '08:00:00', '10:00:00', 'LH-01'),
(7, 3, 3, 'PRACTICAL', '2026-03-26', '13:00:00', '16:00:00', 'ICT Lab 2');

-- attend
INSERT INTO attend (student_id, session_id, attend_status, recorded_date) VALUES
(9,  1, 'PRESENT', '2025-10-08'),
(10, 1, 'PRESENT', '2025-10-08'),
(11, 1, 'PRESENT', '2025-10-08'),
(12, 1, 'PRESENT', '2025-10-08'),
(13, 1, 'ABSENT',  '2025-10-08'),
(9,  2, 'PRESENT', '2025-10-15'),
(10, 2, 'PRESENT', '2025-10-15'),
(11, 2, 'PRESENT', '2025-10-15'),
(12, 2, 'ABSENT',  '2025-10-15'),
(13, 2, 'PRESENT', '2025-10-15'),
(9,  3, 'PRESENT', '2025-10-24'),
(10, 3, 'PRESENT', '2025-10-24'),
(11, 3, 'ABSENT',  '2025-10-24'),
(12, 3, 'LATE',    '2025-10-24'),
(13, 3, 'ABSENT',  '2025-10-24'),
(9,  4, 'PRESENT', '2025-10-29'),
(10, 4, 'PRESENT', '2025-10-29'),
(11, 4, 'PRESENT', '2025-10-29'),
(12, 4, 'PRESENT', '2025-10-29'),
(13, 4, 'PRESENT', '2025-10-29'),
(9,  5, 'PRESENT', '2026-03-10'),
(10, 5, 'PRESENT', '2026-03-10'),
(11, 5, 'PRESENT', '2026-03-10'),
(12, 5, 'PRESENT', '2026-03-10'),
(13, 5, 'ABSENT',  '2026-03-10'),
(9,  6, 'PRESENT', '2026-03-17'),
(10, 6, 'PRESENT', '2026-03-17'),
(11, 6, 'MEDICAL', '2026-03-17'),
(12, 6, 'PRESENT', '2026-03-17'),
(13, 6, 'PRESENT', '2026-03-17'),
(9,  7, 'PRESENT', '2026-03-26'),
(10, 7, 'PRESENT', '2026-03-26'),
(11, 7, 'PRESENT', '2026-03-26'),
(12, 7, 'ABSENT',  '2026-03-26'),
(13, 7, 'PRESENT', '2026-03-26');

-- assessment
INSERT INTO assessment (assessment_id, offering_id, assessment_name, assessment_type, submitted_date, reference_number, assessment_date) VALUES
(1, 1, 'Quiz 1',       'QUIZ',       '2025-11-12', 'ICT1113-QZ-01', '2025-11-05'),
(2, 1, 'Assignment 1', 'ASSIGNMENT', '2025-12-05', 'ICT1113-AS-01', '2025-11-28'),
(3, 1, 'Mid Exam',     'MID_EXAM',   '2025-12-20', 'ICT1113-ME-01', '2025-12-12'),
(4, 2, 'Quiz 1',       'QUIZ',       '2025-11-14', 'ICT1123-QZ-01', '2025-11-07'),
(5, 3, 'Mid Exam',     'MID_EXAM',   '2026-05-08', 'ICT1233-ME-01', '2026-04-30'),
(6, 3, 'Mini Project', 'PROJECT',    '2026-05-22', 'ICT1233-PR-01', '2026-05-15'),
(7, 4, 'Lab Test',     'PRACTICAL',  '2026-05-29', 'ICT1223-LT-01', '2026-05-22'),
(8, 6, 'Quiz 1',       'QUIZ',       NULL,         'ICT2113-QZ-01', '2026-10-21');

-- enrollment
INSERT INTO enrollment (student_id, offering_id, enrollment_date, enrollment_status) VALUES
(9,  1, '2025-10-03', 'COMPLETED'),
(10, 1, '2025-10-03', 'COMPLETED'),
(11, 1, '2025-10-03', 'COMPLETED'),
(12, 1, '2025-10-03', 'COMPLETED'),
(13, 1, '2025-10-03', 'COMPLETED'),
(9,  2, '2025-10-03', 'COMPLETED'),
(10, 2, '2025-10-03', 'COMPLETED'),
(11, 2, '2025-10-03', 'COMPLETED'),
(12, 2, '2025-10-03', 'COMPLETED'),
(13, 2, '2025-10-03', 'COMPLETED'),
(9,  3, '2026-02-27', 'COMPLETED'),
(10, 3, '2026-02-27', 'COMPLETED'),
(11, 3, '2026-02-27', 'COMPLETED'),
(12, 3, '2026-02-27', 'COMPLETED'),
(13, 3, '2026-02-27', 'COMPLETED'),
(9,  4, '2026-02-27', 'COMPLETED'),
(10, 4, '2026-02-27', 'COMPLETED'),
(11, 4, '2026-02-27', 'COMPLETED'),
(12, 4, '2026-02-27', 'COMPLETED'),
(13, 4, '2026-02-27', 'COMPLETED'),
(14, 5, '2026-02-27', 'COMPLETED'),
(15, 5, '2026-02-27', 'COMPLETED'),
(9,  6, '2026-10-02', 'ENROLLED'),
(10, 6, '2026-10-02', 'ENROLLED'),
(11, 6, '2026-10-02', 'ENROLLED'),
(12, 6, '2026-10-02', 'ENROLLED'),
(13, 6, '2026-10-02', 'ENROLLED');

-- eligibility
INSERT INTO eligibility (eligibility_id, student_id, offering_id, ca_average, attendance_percentage, eligibility_status) VALUES
(1,  9,  1, 85.00, 100.00, 'ELIGIBLE'),
(2,  10, 1, 75.00,  96.00, 'ELIGIBLE'),
(3,  11, 1, 65.00,  88.00, 'ELIGIBLE'),
(4,  12, 1, 55.00,  84.00, 'ELIGIBLE'),
(5,  13, 1, 35.00,  80.00, 'ELIGIBLE'),
(6,  9,  3, 87.50,  98.00, 'ELIGIBLE'),
(7,  10, 3, 77.50,  94.00, 'ELIGIBLE'),
(8,  11, 3, 67.50,  90.00, 'ELIGIBLE'),
(9,  12, 3, 52.50,  82.00, 'ELIGIBLE'),
(10, 13, 3, 42.50,  80.00, 'ELIGIBLE'),
(11, 14, 5, 75.00,  92.00, 'ELIGIBLE'),
(12, 15, 5, 60.00,  85.00, 'ELIGIBLE'),
(13, 12, 6, NULL,   85.00, 'PENDING'),
(14, 13, 6, NULL,   60.00, 'NOT_ELIGIBLE');

-- exam
INSERT INTO exam (exam_id, offering_id, type) VALUES
(1, 1, 'FINAL'),
(2, 2, 'FINAL'),
(3, 3, 'MID'),
(4, 3, 'FINAL'),
(5, 4, 'FINAL'),
(6, 5, 'FINAL');

-- result
INSERT INTO result (student_id, exam_id, ca_result, final_exam_mark, total_mark, sgpa, result_status, published_date, cgpa) VALUES
(9,  1, 34.00, 52.00, 86.00, 4.00, 'PASS', '2026-03-27', 4.00),
(10, 1, 30.00, 45.00, 75.00, 3.50, 'PASS', '2026-03-27', 3.50),
(11, 1, 26.00, 38.00, 64.00, 2.50, 'PASS', '2026-03-27', 2.50),
(12, 1, 22.00, 28.00, 50.00, 2.00, 'PASS', '2026-03-27', 2.00),
(13, 1, 14.00, 18.00, 32.00, 0.85, 'FAIL', '2026-03-27', 0.85),
(9,  2, 32.00, 48.00, 80.00, 4.00, 'PASS', '2026-03-27', 4.00),
(10, 2, 28.00, 44.00, 72.00, 3.50, 'PASS', '2026-03-27', 3.50),
(11, 2, 24.00, 34.00, 58.00, 2.50, 'PASS', '2026-03-27', 2.50),
(12, 2, 20.00, 30.00, 50.00, 2.00, 'PASS', '2026-03-27', 2.00),
(13, 2, 18.00, 30.00, 48.00, 0.85, 'PASS', '2026-03-27', 0.85),
(9,  4, 35.00, 54.00, 89.00, 4.00, 'PASS', '2026-09-04', 4.00),
(10, 4, 31.00, 46.00, 77.00, 3.50, 'PASS', '2026-09-04', 3.50),
(11, 4, 27.00, 40.00, 67.00, 2.85, 'PASS', '2026-09-04', 2.68),
(12, 4, 21.00, 33.00, 54.00, 1.85, 'PASS', '2026-09-04', 1.93),
(13, 4, 17.00, 30.00, 47.00, 1.50, 'PASS', '2026-09-04', 1.18),
(9,  5, 33.00, 50.00, 83.00, 4.00, 'PASS', '2026-09-04', 4.00),
(10, 5, 29.00, 42.00, 71.00, 3.50, 'PASS', '2026-09-04', 3.50),
(11, 5, 25.00, 36.00, 61.00, 2.85, 'PASS', '2026-09-04', 2.68),
(12, 5, 20.00, 27.00, 47.00, 1.85, 'PASS', '2026-09-04', 1.93),
(13, 5, 15.00, 25.00, 40.00, 1.50, 'PASS', '2026-09-04', 1.18),
(14, 6, 30.00, 44.00, 74.00, 3.30, 'PASS', '2026-09-04', 3.30),
(15, 6, 24.00, 36.00, 60.00, 2.70, 'PASS', '2026-09-04', 2.70);

-- repeat_student
INSERT INTO repeat_student (student_id, subject_id, repeat_year, repeat_shy, review) VALUES
(13, 1, 2026, 'Second attempt', 'Failed the final exam in the first attempt. Registered to repeat the course.');

-- timetable_entity
INSERT INTO timetable_entity (timetable_entity_id, timetable_id, offering_id, day, start_time, end_time, lecture_hall, session_type) VALUES
(1, 1, 1, 'WEDNESDAY', '08:00:00', '10:00:00', 'LH-01',    'LECTURE'),
(2, 1, 1, 'FRIDAY',    '13:00:00', '16:00:00', 'ICT Lab 1', 'PRACTICAL'),
(3, 1, 2, 'TUESDAY',   '10:00:00', '12:00:00', 'LH-02',    'LECTURE'),
(4, 1, 3, 'TUESDAY',   '08:00:00', '10:00:00', 'LH-01',    'LECTURE'),
(5, 1, 3, 'THURSDAY',  '13:00:00', '16:00:00', 'ICT Lab 2', 'PRACTICAL'),
(6, 1, 4, 'WEDNESDAY', '10:00:00', '12:00:00', 'LH-02',    'LECTURE'),
(7, 1, 5, 'MONDAY',    '08:00:00', '10:00:00', 'ET-LH-01', 'LECTURE'),
(8, 2, 6, 'THURSDAY',  '08:00:00', '10:00:00', 'LH-03',    'LECTURE');

-- medical
INSERT INTO medical (medical_id, student_id, medical_date, reason, certificate_path, status, reviewer_note, reviewed_date) VALUES
(1, 11, '2026-03-17', 'Viral fever', '/uploads/medical/11_2026-03-17.pdf', 'APPROVED', 'Certificate verified.', '2026-03-20'),
(2, 12, '2026-03-26', 'Family emergency', '/uploads/medical/12_2026-03-26.pdf', 'REJECTED', 'Certificate not issued by a registered practitioner.', '2026-03-30'),
(3, 13, '2026-10-08', 'Dental surgery', '/uploads/medical/13_2026-10-08.pdf', 'PENDING', NULL, NULL);

-- notice
INSERT INTO notice (notice_id, created_by, title, description, created_date, audience, expiry_date, status) VALUES
(1, 1, 'Semester 1 Course Registration',  'Complete your course registration before the deadline.',           '2026-09-28 09:00:00', 'UNDERGRADUATE',      '2026-10-20', 'ACTIVE'),
(2, 1, 'Semester 2 Results Released',     'Results for 2025/2026 Semester 2 are now available.',              '2026-09-04 10:30:00', 'UNDERGRADUATE',      '2026-09-30', 'EXPIRED'),
(3, 2, 'Lecturer Meeting',                'Department meeting on Friday at 2.00 PM in the conference room.',  '2026-10-05 08:45:00', 'LECTURER',           '2026-10-16', 'ACTIVE'),
(4, 2, 'Lab Maintenance',                 'ICT labs will be closed for maintenance this Saturday.',           '2026-10-06 14:00:00', 'TECHNICAL_OFFICER',  '2026-10-17', 'ACTIVE'),
(5, 1, 'Public Holiday',                  'The university will be closed on the upcoming public holiday.',    '2026-10-08 11:15:00', 'ALL',                '2026-10-25', 'ACTIVE'),
(6, 1, 'Mid Semester Exam Schedule',      'Draft schedule for mid semester exams.',                           '2026-10-09 16:00:00', 'UNDERGRADUATE',      NULL,         'DRAFT');
