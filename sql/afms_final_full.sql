-- =========================================================
-- AFMS - FINAL FULL DATABASE SCRIPT (MySQL 8.x / MariaDB 10.5+)
-- =========================================================
-- Combines, in order:
--   1. schema            (+ users.photo, + undergraduate.student_number)
--   2. triggers          (auto photo, auto student number TG/year/number)
--   3. mock data         (course_material = 100 sample lecture PDFs)
--   4. DB user accounts  (afms_login, afms_admin, afms_lecturer, afms_officer, afms_student)
--
-- Run ONCE as root (phpMyAdmin SQL tab / mysql client).
-- WARNING: DROP DATABASE below deletes any existing afms_db.
--
-- All app users share the DEV password: Password@123 (plain text - development only).
-- DB account passwords (development only):
--   afms_login: login_dev_pw      afms_admin: admin_dev_pw
--   afms_lecturer: lecturer_dev_pw  afms_officer: officer_dev_pw
--   afms_student: student_dev_pw
-- Data is anchored to 2026-10-31: semesters 1-4 complete, semester 5 ongoing.
-- =========================================================

DROP DATABASE IF EXISTS afms_db;
CREATE DATABASE afms_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE afms_db;



-- =========================================================
-- Base tables (no dependencies)
-- =========================================================

CREATE TABLE users (
                       user_id   INT AUTO_INCREMENT PRIMARY KEY,
                       name      VARCHAR(150) NOT NULL,           -- shared by all user types
                       email     VARCHAR(150) NOT NULL UNIQUE,
                       password  VARCHAR(255) NOT NULL,           -- DEV ONLY: mock data uses plain text; use a hash in production
                       phone     VARCHAR(30),                     -- shared by all user types
                       address   VARCHAR(255),
                       dob       DATE,
                       gender    ENUM('MALE','FEMALE','OTHER') DEFAULT NULL,
                       user_type ENUM('ADMIN','UNDERGRADUATE','LECTURER','TECHNICAL_OFFICER') NOT NULL,
                       photo     VARCHAR(50) DEFAULT NULL COMMENT 'Profile photo filename (e.g. b1.jpg, g5.jpg); auto-assigned by trigger'
) ;

CREATE TABLE department (
                            dep_id     INT AUTO_INCREMENT PRIMARY KEY,
                            dep_name   VARCHAR(150) NOT NULL,
                            contact_no VARCHAR(30)
) ;

CREATE TABLE semester (
                          semester_id     INT AUTO_INCREMENT PRIMARY KEY,
                          semester_number INT NOT NULL,
                          academic_year   VARCHAR(20) NOT NULL,
                          start_date      DATE,
                          end_date        DATE,
                          status          VARCHAR(30),
                          CONSTRAINT chk_semester_dates CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
) ;

CREATE TABLE timetable (
                           timetable_id   INT AUTO_INCREMENT PRIMARY KEY,
                           academic_year  VARCHAR(20) NOT NULL,
                           published_date DATE,
                           status         VARCHAR(30)
) ;

-- =========================================================
-- Department-dependent tables
-- =========================================================

CREATE TABLE batch (
                       batch_id    INT AUTO_INCREMENT PRIMARY KEY,
                       batch_name  VARCHAR(100) NOT NULL,
                       intake_year YEAR NOT NULL,
                       dep_id      INT,
                       CONSTRAINT fk_batch_department FOREIGN KEY (dep_id)
                           REFERENCES department(dep_id) ON UPDATE CASCADE ON DELETE SET NULL
) ;

CREATE TABLE course (
                        course_id   INT AUTO_INCREMENT PRIMARY KEY,
                        course_name VARCHAR(200) NOT NULL,
                        course_code VARCHAR(50)  NOT NULL UNIQUE,
                        credit      DECIMAL(4,2) NOT NULL,
                        dep_id      INT,
                        CONSTRAINT fk_course_department FOREIGN KEY (dep_id)
                            REFERENCES department(dep_id) ON UPDATE CASCADE ON DELETE SET NULL
) ;

-- =========================================================
-- User subtypes: shared primary key (user_id is both PK and FK).
-- name, email, phone live only in users.
-- =========================================================

CREATE TABLE admin (
                       user_id       INT PRIMARY KEY,
                       CONSTRAINT fk_admin_user FOREIGN KEY (user_id)
                           REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE
) ;

CREATE TABLE undergraduate (
                               user_id         INT PRIMARY KEY,
                               student_number  VARCHAR(20) NOT NULL DEFAULT '' UNIQUE,   -- faculty/year/number, e.g. TG/2024/1001 (auto-filled by trigger)
                               dep_id          INT,
                               batch_id        INT,
                               profile_picture VARCHAR(255),
                               CONSTRAINT chk_student_number CHECK (student_number REGEXP '^[A-Z]{2}/[0-9]{4}/[0-9]{4}$'),
                               CONSTRAINT fk_undergraduate_user FOREIGN KEY (user_id)
                                   REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE,
                               CONSTRAINT fk_undergraduate_department FOREIGN KEY (dep_id)
                                   REFERENCES department(dep_id) ON UPDATE CASCADE ON DELETE SET NULL,
                               CONSTRAINT fk_undergraduate_batch FOREIGN KEY (batch_id)
                                   REFERENCES batch(batch_id) ON UPDATE CASCADE ON DELETE SET NULL
) ;

CREATE TABLE lecturer (
                          user_id        INT PRIMARY KEY,
                          qualification  VARCHAR(255),
                          dep_id         INT,
                          CONSTRAINT fk_lecturer_user FOREIGN KEY (user_id)
                              REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE,
                          CONSTRAINT fk_lecturer_department FOREIGN KEY (dep_id)
                              REFERENCES department(dep_id) ON UPDATE CASCADE ON DELETE SET NULL
) ;

CREATE TABLE technical_officer (
                                   user_id        INT PRIMARY KEY,
                                   dep_id         INT,
                                   CONSTRAINT fk_to_user FOREIGN KEY (user_id)
                                       REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE,
                                   CONSTRAINT fk_to_department FOREIGN KEY (dep_id)
                                       REFERENCES department(dep_id) ON UPDATE CASCADE ON DELETE SET NULL
) ;

-- =========================================================
-- Course offering and related tables
-- =========================================================

CREATE TABLE course_offering (
                                 offering_id INT AUTO_INCREMENT PRIMARY KEY,
                                 course_id   INT NOT NULL,
                                 semester_id INT NOT NULL,
                                 lecturer_id INT,
                                 UNIQUE (course_id, semester_id),
                                 CONSTRAINT fk_offering_course FOREIGN KEY (course_id)
                                     REFERENCES course(course_id) ON UPDATE CASCADE ON DELETE CASCADE,
                                 CONSTRAINT fk_offering_semester FOREIGN KEY (semester_id)
                                     REFERENCES semester(semester_id) ON UPDATE CASCADE ON DELETE CASCADE,
                                 CONSTRAINT fk_offering_lecturer FOREIGN KEY (lecturer_id)
                                     REFERENCES lecturer(user_id) ON UPDATE CASCADE ON DELETE SET NULL
) ;

CREATE TABLE course_material (
                                 material_id   INT AUTO_INCREMENT PRIMARY KEY,
                                 offering_id   INT,
                                 lecturer_id   INT,
                                 title         VARCHAR(200) NOT NULL,
                                 material_type VARCHAR(100),
                                 file_path     VARCHAR(500),
                                 file_size     BIGINT,
                                 external_link VARCHAR(500),
                                 upload_date   DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_material_offering FOREIGN KEY (offering_id)
                                     REFERENCES course_offering(offering_id) ON UPDATE CASCADE ON DELETE CASCADE,
                                 CONSTRAINT fk_material_lecturer FOREIGN KEY (lecturer_id)
                                     REFERENCES lecturer(user_id) ON UPDATE CASCADE ON DELETE SET NULL
) ;

CREATE TABLE `session` (
                           session_id     INT AUTO_INCREMENT PRIMARY KEY,
                           offering_id    INT NOT NULL,
                           session_number INT NOT NULL,
                           session_type   VARCHAR(50),
                           session_date   DATE NOT NULL,
                           start_time     TIME,
                           end_time       TIME,
                           room           VARCHAR(100),
                           UNIQUE (offering_id, session_number),
                           CONSTRAINT fk_session_offering FOREIGN KEY (offering_id)
                               REFERENCES course_offering(offering_id) ON UPDATE CASCADE ON DELETE CASCADE,
                           CONSTRAINT chk_session_time CHECK (end_time IS NULL OR start_time IS NULL OR end_time > start_time)
) ;

CREATE TABLE attend (
                        student_id    INT NOT NULL,
                        session_id    INT NOT NULL,
                        attend_status VARCHAR(30) NOT NULL,
                        recorded_date DATE,
                        PRIMARY KEY (student_id, session_id),
                        CONSTRAINT fk_attend_student FOREIGN KEY (student_id)
                            REFERENCES undergraduate(user_id) ON UPDATE CASCADE ON DELETE CASCADE,
                        CONSTRAINT fk_attend_session FOREIGN KEY (session_id)
                            REFERENCES `session`(session_id) ON UPDATE CASCADE ON DELETE CASCADE
) ;

CREATE TABLE assessment (
                            assessment_id    INT AUTO_INCREMENT PRIMARY KEY,   -- fixed typo: was "assesment_id"
                            offering_id      INT NOT NULL,
                            assessment_name  VARCHAR(200) NOT NULL,
                            assessment_type  VARCHAR(100),
                            submitted_date   DATE,
                            reference_number VARCHAR(100),
                            assessment_date  DATE,
                            CONSTRAINT fk_assessment_offering FOREIGN KEY (offering_id)
                                REFERENCES course_offering(offering_id) ON UPDATE CASCADE ON DELETE CASCADE
) ;

CREATE TABLE enrollment (
                            student_id        INT NOT NULL,
                            offering_id       INT NOT NULL,
                            enrollment_date   DATE NOT NULL,
                            enrollment_status VARCHAR(30),
                            PRIMARY KEY (student_id, offering_id),
                            CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id)
                                REFERENCES undergraduate(user_id) ON UPDATE CASCADE ON DELETE CASCADE,
                            CONSTRAINT fk_enrollment_offering FOREIGN KEY (offering_id)
                                REFERENCES course_offering(offering_id) ON UPDATE CASCADE ON DELETE CASCADE
) ;

CREATE TABLE eligibility (
                             eligibility_id        INT AUTO_INCREMENT PRIMARY KEY,
                             student_id            INT NOT NULL,
                             offering_id           INT NOT NULL,
                             ca_average            DECIMAL(5,2),
                             attendance_percentage DECIMAL(5,2),
                             eligibility_status    VARCHAR(30),
                             UNIQUE (student_id, offering_id),
                             CONSTRAINT fk_eligibility_student FOREIGN KEY (student_id)
                                 REFERENCES undergraduate(user_id) ON UPDATE CASCADE ON DELETE CASCADE,
                             CONSTRAINT fk_eligibility_offering FOREIGN KEY (offering_id)
                                 REFERENCES course_offering(offering_id) ON UPDATE CASCADE ON DELETE CASCADE,
                             CONSTRAINT chk_attendance_pct CHECK (attendance_percentage IS NULL OR attendance_percentage BETWEEN 0 AND 100)
) ;

CREATE TABLE exam (
                      exam_id     INT AUTO_INCREMENT PRIMARY KEY,
                      offering_id INT NOT NULL,
                      type        VARCHAR(50) NOT NULL,
                      CONSTRAINT fk_exam_offering FOREIGN KEY (offering_id)
                          REFERENCES course_offering(offering_id) ON UPDATE CASCADE ON DELETE CASCADE
) ;

CREATE TABLE result (
                        result_id       INT AUTO_INCREMENT PRIMARY KEY,
                        student_id      INT NOT NULL,
                        exam_id         INT NOT NULL,
                        ca_result       DECIMAL(5,2),
                        final_exam_mark DECIMAL(5,2),
                        total_mark      DECIMAL(5,2),
                        sgpa            DECIMAL(4,2),
                        result_status   VARCHAR(30),
                        published_date  DATE,
                        cgpa            DECIMAL(4,2),
                        UNIQUE (student_id, exam_id),
                        CONSTRAINT fk_result_student FOREIGN KEY (student_id)
                            REFERENCES undergraduate(user_id) ON UPDATE CASCADE ON DELETE CASCADE,
                        CONSTRAINT fk_result_exam FOREIGN KEY (exam_id)
                            REFERENCES exam(exam_id) ON UPDATE CASCADE ON DELETE CASCADE
) ;

CREATE TABLE repeat_student (
                                student_id  INT NOT NULL,
                                subject_id  INT NOT NULL,          -- references course(course_id)
                                repeat_year YEAR NOT NULL,
                                repeat_shy  VARCHAR(50),
                                review      TEXT,
                                PRIMARY KEY (student_id, subject_id, repeat_year),
                                CONSTRAINT fk_repeat_student FOREIGN KEY (student_id)
                                    REFERENCES undergraduate(user_id) ON UPDATE CASCADE ON DELETE CASCADE,
                                CONSTRAINT fk_repeat_subject FOREIGN KEY (subject_id)
                                    REFERENCES course(course_id) ON UPDATE CASCADE ON DELETE CASCADE
) ;

CREATE TABLE timetable_entity (
                                  timetable_entity_id INT AUTO_INCREMENT PRIMARY KEY,
                                  timetable_id        INT NOT NULL,
                                  offering_id         INT,
                                  day                 VARCHAR(20),
                                  start_time          TIME,
                                  end_time            TIME,
                                  lecture_hall        VARCHAR(100),
                                  session_type        VARCHAR(50),
                                  CONSTRAINT fk_timetable_entity_timetable FOREIGN KEY (timetable_id)
                                      REFERENCES timetable(timetable_id) ON UPDATE CASCADE ON DELETE CASCADE,
                                  CONSTRAINT fk_timetable_entity_offering FOREIGN KEY (offering_id)
                                      REFERENCES course_offering(offering_id) ON UPDATE CASCADE ON DELETE SET NULL,
                                  CONSTRAINT chk_timetable_time CHECK (end_time IS NULL OR start_time IS NULL OR end_time > start_time)
) ;

CREATE TABLE medical (
                         medical_id       INT AUTO_INCREMENT PRIMARY KEY,
                         student_id       INT NOT NULL,
                         medical_date     DATE NOT NULL,
                         reason           TEXT,
                         certificate_path VARCHAR(500),
                         status           VARCHAR(30),
                         reviewer_note    TEXT,
                         reviewed_date    DATE,
                         CONSTRAINT fk_medical_student FOREIGN KEY (student_id)
                             REFERENCES undergraduate(user_id) ON UPDATE CASCADE ON DELETE CASCADE
) ;

CREATE TABLE notice (
                        notice_id    INT AUTO_INCREMENT PRIMARY KEY,
                        created_by   INT,
                        title        VARCHAR(250) NOT NULL,
                        description  TEXT,
                        created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                        audience     VARCHAR(100),
                        expiry_date  DATE,
                        status       VARCHAR(30),
                        CONSTRAINT fk_notice_admin FOREIGN KEY (created_by)
                            REFERENCES admin(user_id) ON UPDATE CASCADE ON DELETE SET NULL
) ;

-- =========================================================
-- Indexes
-- (InnoDB already indexes every foreign key column, so only
--  non-FK lookups need an explicit index.)
-- =========================================================
CREATE INDEX idx_notice_expiry ON notice(expiry_date);

-- =========================================================
-- Triggers
-- (created BEFORE the mock data is loaded, so every inserted
--  user / undergraduate automatically gets a photo / student number)
-- =========================================================
DELIMITER $$

-- Auto-assign a profile photo to every new user.
--   FEMALE      -> g1.jpg .. g10.jpg
--   MALE/OTHER/NULL -> b1.jpg .. b15.jpg
-- The first unused photo is picked; once all are taken they are reused in rotation.
CREATE TRIGGER trg_users_assign_photo
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    DECLARE v_prefix CHAR(1);
    DECLARE v_max    INT;
    DECLARE v_n      INT DEFAULT 1;
    DECLARE v_used   INT;

    IF NEW.photo IS NULL THEN
        IF NEW.gender = 'FEMALE' THEN
            SET v_prefix = 'g';
            SET v_max = 10;
        ELSE
            SET v_prefix = 'b';
            SET v_max = 15;
        END IF;

        photo_loop: WHILE v_n <= v_max DO
            IF NOT EXISTS (SELECT 1 FROM users WHERE photo = CONCAT(v_prefix, v_n, '.jpg')) THEN
                SET NEW.photo = CONCAT(v_prefix, v_n, '.jpg');
                LEAVE photo_loop;
            END IF;
            SET v_n = v_n + 1;
        END WHILE;

        -- every photo already used: rotate
        IF NEW.photo IS NULL THEN
            SET v_used = (SELECT COUNT(*) FROM users WHERE photo LIKE CONCAT(v_prefix, '%.jpg'));
            SET NEW.photo = CONCAT(v_prefix, (v_used MOD v_max) + 1, '.jpg');
        END IF;
    END IF;
END$$

-- Auto-generate a student number (TG/<intake year>/<number>) when none is supplied.
--   TG     = faculty code
--   year   = intake year of the student's batch (current year if no batch)
--   number = next free 4-digit number for that year, starting at 1001
CREATE TRIGGER trg_undergraduate_student_number
BEFORE INSERT ON undergraduate
FOR EACH ROW
BEGIN
    DECLARE v_year INT;
    DECLARE v_next INT;

    IF NEW.student_number IS NULL OR NEW.student_number = '' THEN
        SET v_year = (SELECT intake_year FROM batch WHERE batch_id = NEW.batch_id);
        IF v_year IS NULL THEN
            SET v_year = YEAR(CURDATE());
        END IF;

        SET v_next = (SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(student_number, '/', -1) AS UNSIGNED)), 1000) + 1
                      FROM undergraduate
                      WHERE student_number LIKE CONCAT('TG/', v_year, '/%'));

        SET NEW.student_number = CONCAT('TG/', v_year, '/', v_next);
    END IF;
END$$

DELIMITER ;

-- =========================================================
-- Mock data
-- =========================================================


SET @pw = 'Password@123';

-- users

INSERT INTO users (user_id, name, email, password, phone, address, dob, gender, user_type) VALUES
(1, 'Kasun Wijesinghe', 'kasun.wijesinghe@afms.example.com', @pw, '+94 71 234 5601', '12 Temple Road, Matara', '1985-03-14', 'MALE', 'ADMIN'),
(2, 'Dilini Fernando', 'dilini.fernando@afms.example.com', @pw, '+94 71 234 5602', '45 Beach Road, Galle', '1988-07-22', 'FEMALE', 'ADMIN'),
(3, 'Dr. Ruwan Jayasinghe', 'ruwan.jayasinghe@afms.example.com', @pw, '+94 77 345 6701', '8 Lake View, Matara', '1979-11-05', 'MALE', 'LECTURER'),
(4, 'Dr. Sanduni Rajapaksha', 'sanduni.rajapaksha@afms.example.com', @pw, '+94 77 345 6702', '23 Hill Street, Galle', '1982-02-18', 'FEMALE', 'LECTURER'),
(5, 'Chamara Gunawardena', 'chamara.gunawardena@afms.example.com', @pw, '+94 77 345 6703', '5 Park Lane, Matara', '1986-09-30', 'MALE', 'LECTURER'),
(6, 'Nadeesha Silva', 'nadeesha.silva@afms.example.com', @pw, '+94 77 345 6704', '17 Station Road, Weligama', '1984-06-12', 'FEMALE', 'LECTURER'),
(7, 'Pradeep Bandara', 'pradeep.bandara@afms.example.com', @pw, '+94 76 456 7801', '31 Main Street, Akuressa', '1990-01-25', 'MALE', 'TECHNICAL_OFFICER'),
(8, 'Thilini Herath', 'thilini.herath@afms.example.com', @pw, '+94 76 456 7802', '9 Garden Road, Matara', '1992-04-08', 'FEMALE', 'TECHNICAL_OFFICER'),
(9, 'Kavindu Lakshan', 'kavindu.lakshan@afms.example.com', @pw, '+94 70 567 8901', '14 School Lane, Matara', '2003-05-17', 'MALE', 'UNDERGRADUATE'),
(10, 'Sithumi Weerasinghe', 'sithumi.weerasinghe@afms.example.com', @pw, '+94 70 567 8902', '62 Church Road, Galle', '2003-08-02', 'FEMALE', 'UNDERGRADUATE'),
(11, 'Dinuka Ranasinghe', 'dinuka.ranasinghe@afms.example.com', @pw, '+94 70 567 8903', '3 Canal Road, Kamburupitiya', '2002-12-21', 'MALE', 'UNDERGRADUATE'),
(12, 'Ishara Madushani', 'ishara.madushani@afms.example.com', @pw, '+94 70 567 8904', '27 Flower Road, Hakmana', '2003-03-09', 'FEMALE', 'UNDERGRADUATE'),
(13, 'Tharindu Kumara', 'tharindu.kumara@afms.example.com', @pw, '+94 70 567 8905', '40 Paddy Lane, Deniyaya', '2003-10-30', 'MALE', 'UNDERGRADUATE'),
(14, 'Amaya Senanayake', 'amaya.senanayake@afms.example.com', @pw, '+94 70 567 8906', '19 Sea Street, Dikwella', '2003-01-14', 'FEMALE', 'UNDERGRADUATE'),
(15, 'Lahiru Dissanayake', 'lahiru.dissanayake@afms.example.com', @pw, '+94 70 567 8907', '56 Market Road, Tangalle', '2002-09-26', 'MALE', 'UNDERGRADUATE'),
(16, 'Malsha Perera', 'malsha.perera@afms.example.com', @pw, '+94 71 234 5603', '7 Fort Road, Galle', '1991-05-19', 'FEMALE', 'ADMIN'),
(17, 'Dr. Asela Ratnayake', 'asela.ratnayake@afms.example.com', @pw, '+94 77 345 6705', '14 Lake Drive, Matara', '1980-08-27', 'MALE', 'LECTURER'),
(18, 'Dr. Kumari Abeysekara', 'kumari.abeysekara@afms.example.com', @pw, '+94 77 345 6706', '36 Temple Lane, Galle', '1983-12-03', 'FEMALE', 'LECTURER'),
(19, 'Janaka Rathnayake', 'janaka.rathnayake@afms.example.com', @pw, '+94 77 345 6707', '22 Rail Road, Weligama', '1987-03-21', 'MALE', 'LECTURER'),
(20, 'Hiruni Jayawardena', 'hiruni.jayawardena@afms.example.com', @pw, '+94 77 345 6708', '11 Orchid Lane, Matara', '1989-10-09', 'FEMALE', 'LECTURER'),
(21, 'Saman Kumara', 'saman.kumara@afms.example.com', @pw, '+94 76 456 7803', '48 Hospital Road, Akuressa', '1991-07-14', 'MALE', 'TECHNICAL_OFFICER'),
(22, 'Nimali Karunaratne', 'nimali.karunaratne@afms.example.com', @pw, '+94 76 456 7804', '6 Rose Garden, Galle', '1993-02-06', 'FEMALE', 'TECHNICAL_OFFICER'),
(23, 'Nuwan Pathirana', 'nuwan.pathirana@afms.example.com', @pw, '+94 70 567 8915', '21 Lotus Road, Matara', '2002-04-11', 'MALE', 'UNDERGRADUATE'),
(24, 'Sanjana Rodrigo', 'sanjana.rodrigo@afms.example.com', @pw, '+94 70 567 8916', '8 Coral Lane, Mirissa', '2002-06-23', 'FEMALE', 'UNDERGRADUATE'),
(25, 'Chathura Amarasinghe', 'chathura.amarasinghe@afms.example.com', @pw, '+94 70 567 8917', '33 Temple Road, Beliatta', '2002-01-30', 'MALE', 'UNDERGRADUATE'),
(26, 'Ayesha Nanayakkara', 'ayesha.nanayakkara@afms.example.com', @pw, '+94 70 567 8918', '15 Lake Road, Galle', '2002-09-05', 'FEMALE', 'UNDERGRADUATE'),
(27, 'Supun Jayatilake', 'supun.jayatilake@afms.example.com', @pw, '+94 70 567 8919', '4 Bridge Street, Akuressa', '2002-11-17', 'MALE', 'UNDERGRADUATE'),
(28, 'Hansani Gamage', 'hansani.gamage@afms.example.com', @pw, '+94 70 567 8920', '52 Hill Road, Deniyaya', '2002-03-28', 'FEMALE', 'UNDERGRADUATE'),
(29, 'Madhawa Liyanage', 'madhawa.liyanage@afms.example.com', @pw, '+94 70 567 8921', '9 Beach Road, Weligama', '2002-07-09', 'MALE', 'UNDERGRADUATE'),
(30, 'Sewwandi Peiris', 'sewwandi.peiris@afms.example.com', @pw, '+94 70 567 8922', '27 Church Lane, Hakmana', '2002-12-02', 'FEMALE', 'UNDERGRADUATE'),
(31, 'Yasiru Samarasinghe', 'yasiru.samarasinghe@afms.example.com', @pw, '+94 70 567 8923', '18 Garden Lane, Matara', '2003-02-20', 'MALE', 'UNDERGRADUATE'),
(32, 'Tharushi Abeywickrama', 'tharushi.abeywickrama@afms.example.com', @pw, '+94 70 567 8924', '41 Main Road, Kamburupitiya', '2003-06-14', 'FEMALE', 'UNDERGRADUATE'),
(33, 'Pasindu Wickramasinghe', 'pasindu.wickramasinghe@afms.example.com', @pw, '+94 70 567 8925', '6 Mill Road, Tangalle', '2003-09-01', 'MALE', 'UNDERGRADUATE'),
(34, 'Nethmi Kodikara', 'nethmi.kodikara@afms.example.com', @pw, '+94 70 567 8926', '30 Sea View, Dikwella', '2003-04-25', 'FEMALE', 'UNDERGRADUATE'),
(35, 'Randika Jayasundara', 'randika.jayasundara@afms.example.com', @pw, '+94 70 567 8927', '12 Railway Avenue, Galle', '2003-08-19', 'MALE', 'UNDERGRADUATE'),
(36, 'Sachini Ekanayake', 'sachini.ekanayake@afms.example.com', @pw, '+94 70 567 8928', '25 Palm Grove, Matara', '2003-05-03', 'FEMALE', 'UNDERGRADUATE'),
(37, 'Malith Ariyaratne', 'malith.ariyaratne@afms.example.com', @pw, '+94 70 567 8929', '37 Paddy Road, Walasmulla', '2002-10-12', 'MALE', 'UNDERGRADUATE'),
(38, 'Dilshani Kariyawasam', 'dilshani.kariyawasam@afms.example.com', @pw, '+94 70 567 8930', '5 Market Lane, Baddegama', '2003-01-08', 'FEMALE', 'UNDERGRADUATE'),
(39, 'Isuru Welikala', 'isuru.welikala@afms.example.com', @pw, '+94 70 567 8931', '44 Farm Road, Kotapola', '2003-03-16', 'MALE', 'UNDERGRADUATE'),
(40, 'Oshadi Mendis', 'oshadi.mendis@afms.example.com', @pw, '+94 70 567 8932', '19 Garden City, Matara', '2003-07-27', 'FEMALE', 'UNDERGRADUATE'),
(41, 'Gayan Thennakoon', 'gayan.thennakoon@afms.example.com', @pw, '+94 70 567 8933', '8 Stream Lane, Deniyaya', '2002-12-06', 'MALE', 'UNDERGRADUATE'),
(42, 'Piumi Hettiarachchi', 'piumi.hettiarachchi@afms.example.com', @pw, '+94 70 567 8934', '23 Tea Estate Road, Akuressa', '2003-11-21', 'FEMALE', 'UNDERGRADUATE'),
(43, 'Charith Ranatunga', 'charith.ranatunga@afms.example.com', @pw, '+94 70 567 8935', '60 Coconut Lane, Devinuwara', '2003-02-09', 'MALE', 'UNDERGRADUATE'),
(44, 'Vindya Dharmasena', 'vindya.dharmasena@afms.example.com', @pw, '+94 70 567 8936', '13 Lagoon Road, Hambantota', '2003-09-18', 'FEMALE', 'UNDERGRADUATE');

-- department

INSERT INTO department (dep_id, dep_name, contact_no) VALUES
(1, 'Information and Communication Technology', '+94 41 222 0001'),
(2, 'Engineering Technology', '+94 41 222 0002'),
(3, 'Bio-system Technology', '+94 41 222 0003');

-- semester

INSERT INTO semester (semester_id, semester_number, academic_year, start_date, end_date, status) VALUES
(1, 1, '2024/2025', '2024-10-07', '2025-02-14', 'COMPLETED'),
(2, 2, '2024/2025', '2025-03-03', '2025-07-18', 'COMPLETED'),
(3, 1, '2025/2026', '2025-10-06', '2026-02-13', 'COMPLETED'),
(4, 2, '2025/2026', '2026-03-02', '2026-07-17', 'COMPLETED'),
(5, 1, '2026/2027', '2026-10-05', '2027-02-12', 'ONGOING'),
(6, 2, '2026/2027', '2027-03-01', '2027-07-16', 'UPCOMING');

-- timetable

INSERT INTO timetable (timetable_id, academic_year, published_date, status) VALUES
(1, '2024/2025', '2024-09-27', 'PUBLISHED'),
(2, '2025/2026', '2025-09-26', 'PUBLISHED'),
(3, '2026/2027', '2026-09-25', 'PUBLISHED');

-- batch

INSERT INTO batch (batch_id, batch_name, intake_year, dep_id) VALUES
(1, 'ICT Batch 2025', 2025, 1),
(2, 'ET Batch 2025', 2025, 2),
(3, 'BST Batch 2025', 2025, 3),
(4, 'ICT Batch 2024', 2024, 1);

-- course

INSERT INTO course (course_id, course_name, course_code, credit, dep_id) VALUES
(1, 'Introduction to Programming', 'ICT1113', 3.00, 1),
(2, 'Discrete Mathematics', 'ICT1123', 3.00, 1),
(3, 'Database Management Systems', 'ICT1233', 3.00, 1),
(4, 'Object Oriented Programming', 'ICT1223', 3.00, 1),
(5, 'Engineering Mathematics', 'ET1113', 3.00, 2),
(6, 'Computer Networks', 'ICT2113', 3.00, 1),
(7, 'Web Technologies', 'ICT1213', 2.00, 1),
(8, 'Data Structures and Algorithms', 'ICT2123', 3.00, 1),
(9, 'Software Engineering', 'ICT2213', 3.00, 1),
(10, 'Operating Systems', 'ICT2223', 3.00, 1),
(11, 'Engineering Mechanics', 'ET1123', 2.00, 2),
(12, 'Electrical Circuits', 'ET1213', 3.00, 2),
(13, 'Introduction to Plant Science', 'BST1113', 3.00, 3),
(14, 'Biochemistry', 'BST1123', 2.00, 3),
(15, 'Biostatistics', 'BST1213', 3.00, 3),
(16, 'Mobile Application Development', 'ICT3113', 3.00, 1),
(17, 'Information Security', 'ICT3123', 2.00, 1),
(18, 'Thermodynamics', 'ET2113', 3.00, 2),
(19, 'Soil Science', 'BST2113', 3.00, 3);

-- admin

INSERT INTO admin (user_id) VALUES
(1), (2), (16);

-- undergraduate

INSERT INTO undergraduate (user_id, dep_id, batch_id, profile_picture) VALUES
(9, 1, 1, '/uploads/profile/9.jpg'),
(10, 1, 1, '/uploads/profile/10.jpg'),
(11, 1, 1, NULL),
(12, 1, 1, '/uploads/profile/12.jpg'),
(13, 1, 1, NULL),
(14, 2, 2, '/uploads/profile/14.jpg'),
(15, 2, 2, NULL),
(23, 1, 4, '/uploads/profile/23.jpg'),
(24, 1, 4, NULL),
(25, 1, 4, '/uploads/profile/25.jpg'),
(26, 1, 4, '/uploads/profile/26.jpg'),
(27, 1, 4, '/uploads/profile/27.jpg'),
(28, 1, 4, '/uploads/profile/28.jpg'),
(29, 1, 4, '/uploads/profile/29.jpg'),
(30, 1, 4, '/uploads/profile/30.jpg'),
(31, 1, 1, NULL),
(32, 1, 1, NULL),
(33, 1, 1, NULL),
(34, 1, 1, '/uploads/profile/34.jpg'),
(35, 2, 2, '/uploads/profile/35.jpg'),
(36, 2, 2, '/uploads/profile/36.jpg'),
(37, 2, 2, '/uploads/profile/37.jpg'),
(38, 2, 2, '/uploads/profile/38.jpg'),
(39, 3, 3, NULL),
(40, 3, 3, NULL),
(41, 3, 3, '/uploads/profile/41.jpg'),
(42, 3, 3, '/uploads/profile/42.jpg'),
(43, 3, 3, '/uploads/profile/43.jpg'),
(44, 3, 3, '/uploads/profile/44.jpg');

-- lecturer

INSERT INTO lecturer (user_id, qualification, dep_id) VALUES
(3, 'PhD in Information Systems', 1),
(4, 'PhD in Computer Science', 1),
(5, 'MSc in Software Engineering', 1),
(6, 'MSc in Engineering Mathematics', 2),
(17, 'PhD in Network Engineering', 1),
(18, 'MSc in Data Science', 1),
(19, 'PhD in Mechanical Engineering', 2),
(20, 'PhD in Agricultural Science', 3);

-- technical_officer

INSERT INTO technical_officer (user_id, dep_id) VALUES
(7, 1),
(8, 2),
(21, 3),
(22, 1);

-- course_offering

INSERT INTO course_offering (offering_id, course_id, semester_id, lecturer_id) VALUES
(1, 1, 1, 3),
(2, 2, 1, 4),
(3, 3, 2, 3),
(4, 4, 2, 5),
(5, 7, 2, 17),
(6, 1, 3, 3),
(7, 2, 3, 4),
(8, 6, 3, 18),
(9, 8, 3, 17),
(10, 5, 3, 6),
(11, 11, 3, 19),
(12, 13, 3, 20),
(13, 14, 3, 20),
(14, 3, 4, 3),
(15, 4, 4, 5),
(16, 7, 4, 17),
(17, 9, 4, 18),
(18, 10, 4, 4),
(19, 12, 4, 6),
(20, 15, 4, 20),
(21, 6, 5, 4),
(22, 8, 5, 17),
(23, 16, 5, 18),
(24, 17, 5, 5),
(25, 18, 5, 19),
(26, 19, 5, 20),
(27, 1, 5, 3);

-- course_material (100 sample lecture PDFs)
INSERT INTO course_material (offering_id, lecturer_id, title, material_type, file_path, file_size, external_link, upload_date) VALUES
(1, 3, 'Introduction to Programming - Lecture Notes CRS-0001', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_001_ICT1113.pdf', 62843, NULL, '2024-10-14 09:00:00'),
(2, 4, 'Discrete Mathematics - Lecture Notes CRS-0002', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_002_ICT1123.pdf', 62791, NULL, '2024-10-14 09:00:00'),
(3, 3, 'Database Management Systems - Lecture Notes CRS-0003', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_003_ICT1233.pdf', 62765, NULL, '2025-03-10 09:00:00'),
(4, 5, 'Object Oriented Programming - Lecture Notes CRS-0004', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_004_ICT1223.pdf', 62845, NULL, '2025-03-10 09:00:00'),
(10, 6, 'Engineering Mathematics - Lecture Notes CRS-0005', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_005_ET1113.pdf', 62733, NULL, '2025-10-13 09:00:00'),
(8, 18, 'Computer Networks - Lecture Notes CRS-0006', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_006_ICT2113.pdf', 62794, NULL, '2025-10-13 09:00:00'),
(5, 17, 'Web Technologies - Lecture Notes CRS-0007', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_007_ICT1213.pdf', 62769, NULL, '2025-03-10 09:00:00'),
(9, 17, 'Data Structures and Algorithms - Lecture Notes CRS-0008', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_008_ICT2123.pdf', 62838, NULL, '2025-10-13 09:00:00'),
(17, 18, 'Software Engineering - Lecture Notes CRS-0009', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_009_ICT2213.pdf', 62840, NULL, '2026-03-09 09:00:00'),
(18, 4, 'Operating Systems - Lecture Notes CRS-0010', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_010_ICT2223.pdf', 62797, NULL, '2026-03-09 09:00:00'),
(11, 19, 'Engineering Mechanics - Lecture Notes CRS-0011', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_011_ET1123.pdf', 62747, NULL, '2025-10-13 09:00:00'),
(19, 6, 'Electrical Circuits - Lecture Notes CRS-0012', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_012_ET1213.pdf', 62744, NULL, '2026-03-09 09:00:00'),
(12, 20, 'Introduction to Plant Science - Lecture Notes CRS-0013', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_013_BST1113.pdf', 62760, NULL, '2025-10-13 09:00:00'),
(13, 20, 'Biochemistry - Lecture Notes CRS-0014', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_014_BST1123.pdf', 62752, NULL, '2025-10-13 09:00:00'),
(20, 20, 'Biostatistics - Lecture Notes CRS-0015', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_015_BST1213.pdf', 62788, NULL, '2026-03-09 09:00:00'),
(23, 18, 'Mobile Application Development - Lecture Notes CRS-0016', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_016_ICT3113.pdf', 62793, NULL, '2026-10-12 09:00:00'),
(24, 5, 'Information Security - Lecture Notes CRS-0017', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_017_ICT3123.pdf', 62796, NULL, '2026-10-12 09:00:00'),
(25, 19, 'Thermodynamics - Lecture Notes CRS-0018', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_018_ET2113.pdf', 62729, NULL, '2026-10-12 09:00:00'),
(26, 20, 'Soil Science - Lecture Notes CRS-0019', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_019_BST2113.pdf', 62734, NULL, '2026-10-12 09:00:00'),
(6, 3, 'Introduction to Programming - Lecture Notes CRS-0020', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_020_ICT1113.pdf', 62842, NULL, '2025-10-13 10:07:00'),
(7, 4, 'Discrete Mathematics - Lecture Notes CRS-0021', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_021_ICT1123.pdf', 62791, NULL, '2025-10-13 10:07:00'),
(14, 3, 'Database Management Systems - Lecture Notes CRS-0022', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_022_ICT1233.pdf', 62763, NULL, '2026-03-09 10:07:00'),
(15, 5, 'Object Oriented Programming - Lecture Notes CRS-0023', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_023_ICT1223.pdf', 62848, NULL, '2026-03-09 10:07:00'),
(10, 6, 'Engineering Mathematics - Lecture Notes CRS-0024', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_024_ET1113.pdf', 62732, NULL, '2025-10-20 10:07:00'),
(21, 4, 'Computer Networks - Lecture Notes CRS-0025', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_025_ICT2113.pdf', 62792, NULL, '2026-10-12 10:07:00'),
(16, 17, 'Web Technologies - Lecture Notes CRS-0026', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_026_ICT1213.pdf', 62767, NULL, '2026-03-09 10:07:00'),
(22, 17, 'Data Structures and Algorithms - Lecture Notes CRS-0027', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_027_ICT2123.pdf', 62841, NULL, '2026-10-12 10:07:00'),
(17, 18, 'Software Engineering - Lecture Notes CRS-0028', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_028_ICT2213.pdf', 62836, NULL, '2026-03-16 10:07:00'),
(18, 4, 'Operating Systems - Lecture Notes CRS-0029', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_029_ICT2223.pdf', 62800, NULL, '2026-03-16 10:07:00'),
(11, 19, 'Engineering Mechanics - Lecture Notes CRS-0030', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_030_ET1123.pdf', 62746, NULL, '2025-10-20 10:07:00'),
(19, 6, 'Electrical Circuits - Lecture Notes CRS-0031', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_031_ET1213.pdf', 62748, NULL, '2026-03-16 10:07:00'),
(12, 20, 'Introduction to Plant Science - Lecture Notes CRS-0032', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_032_BST1113.pdf', 62759, NULL, '2025-10-20 10:07:00'),
(13, 20, 'Biochemistry - Lecture Notes CRS-0033', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_033_BST1123.pdf', 62753, NULL, '2025-10-20 10:07:00'),
(20, 20, 'Biostatistics - Lecture Notes CRS-0034', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_034_BST1213.pdf', 62785, NULL, '2026-03-16 10:07:00'),
(23, 18, 'Mobile Application Development - Lecture Notes CRS-0035', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_035_ICT3113.pdf', 62796, NULL, '2026-10-19 10:07:00'),
(24, 5, 'Information Security - Lecture Notes CRS-0036', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_036_ICT3123.pdf', 62793, NULL, '2026-10-19 10:07:00'),
(25, 19, 'Thermodynamics - Lecture Notes CRS-0037', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_037_ET2113.pdf', 62731, NULL, '2026-10-19 10:07:00'),
(26, 20, 'Soil Science - Lecture Notes CRS-0038', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_038_BST2113.pdf', 62733, NULL, '2026-10-19 10:07:00'),
(27, 3, 'Introduction to Programming - Lecture Notes CRS-0039', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_039_ICT1113.pdf', 62844, NULL, '2026-10-12 11:14:00'),
(2, 4, 'Discrete Mathematics - Lecture Notes CRS-0040', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_040_ICT1123.pdf', 62792, NULL, '2024-10-21 11:14:00'),
(3, 3, 'Database Management Systems - Lecture Notes CRS-0041', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_041_ICT1233.pdf', 62767, NULL, '2025-03-17 11:14:00'),
(4, 5, 'Object Oriented Programming - Lecture Notes CRS-0042', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_042_ICT1223.pdf', 62845, NULL, '2025-03-17 11:14:00'),
(10, 6, 'Engineering Mathematics - Lecture Notes CRS-0043', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_043_ET1113.pdf', 62733, NULL, '2025-10-27 11:14:00'),
(8, 18, 'Computer Networks - Lecture Notes CRS-0044', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_044_ICT2113.pdf', 62794, NULL, '2025-10-20 11:14:00'),
(5, 17, 'Web Technologies - Lecture Notes CRS-0045', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_045_ICT1213.pdf', 62768, NULL, '2025-03-17 11:14:00'),
(9, 17, 'Data Structures and Algorithms - Lecture Notes CRS-0046', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_046_ICT2123.pdf', 62838, NULL, '2025-10-20 11:14:00'),
(17, 18, 'Software Engineering - Lecture Notes CRS-0047', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_047_ICT2213.pdf', 62837, NULL, '2026-03-23 11:14:00'),
(18, 4, 'Operating Systems - Lecture Notes CRS-0048', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_048_ICT2223.pdf', 62797, NULL, '2026-03-23 11:14:00'),
(11, 19, 'Engineering Mechanics - Lecture Notes CRS-0049', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_049_ET1123.pdf', 62748, NULL, '2025-10-27 11:14:00'),
(19, 6, 'Electrical Circuits - Lecture Notes CRS-0050', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_050_ET1213.pdf', 62745, NULL, '2026-03-23 11:14:00'),
(12, 20, 'Introduction to Plant Science - Lecture Notes CRS-0051', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_051_BST1113.pdf', 62761, NULL, '2025-10-27 11:14:00'),
(13, 20, 'Biochemistry - Lecture Notes CRS-0052', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_052_BST1123.pdf', 62753, NULL, '2025-10-27 11:14:00'),
(20, 20, 'Biostatistics - Lecture Notes CRS-0053', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_053_BST1213.pdf', 62786, NULL, '2026-03-23 11:14:00'),
(23, 18, 'Mobile Application Development - Lecture Notes CRS-0054', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_054_ICT3113.pdf', 62795, NULL, '2026-10-26 11:14:00'),
(24, 5, 'Information Security - Lecture Notes CRS-0055', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_055_ICT3123.pdf', 62797, NULL, '2026-10-26 11:14:00'),
(25, 19, 'Thermodynamics - Lecture Notes CRS-0056', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_056_ET2113.pdf', 62730, NULL, '2026-10-26 11:14:00'),
(26, 20, 'Soil Science - Lecture Notes CRS-0057', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_057_BST2113.pdf', 62735, NULL, '2026-10-26 11:14:00'),
(1, 3, 'Introduction to Programming - Lecture Notes CRS-0058', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_058_ICT1113.pdf', 62842, NULL, '2024-10-21 12:21:00'),
(7, 4, 'Discrete Mathematics - Lecture Notes CRS-0059', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_059_ICT1123.pdf', 62795, NULL, '2025-10-20 12:21:00'),
(14, 3, 'Database Management Systems - Lecture Notes CRS-0060', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_060_ICT1233.pdf', 62765, NULL, '2026-03-16 12:21:00'),
(15, 5, 'Object Oriented Programming - Lecture Notes CRS-0061', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_061_ICT1223.pdf', 62847, NULL, '2026-03-16 12:21:00'),
(10, 6, 'Engineering Mathematics - Lecture Notes CRS-0062', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_062_ET1113.pdf', 62732, NULL, '2025-11-03 12:21:00'),
(21, 4, 'Computer Networks - Lecture Notes CRS-0063', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_063_ICT2113.pdf', 62793, NULL, '2026-10-19 12:21:00'),
(16, 17, 'Web Technologies - Lecture Notes CRS-0064', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_064_ICT1213.pdf', 62767, NULL, '2026-03-16 12:21:00'),
(22, 17, 'Data Structures and Algorithms - Lecture Notes CRS-0065', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_065_ICT2123.pdf', 62840, NULL, '2026-10-19 12:21:00'),
(17, 18, 'Software Engineering - Lecture Notes CRS-0066', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_066_ICT2213.pdf', 62836, NULL, '2026-03-30 12:21:00'),
(18, 4, 'Operating Systems - Lecture Notes CRS-0067', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_067_ICT2223.pdf', 62799, NULL, '2026-03-30 12:21:00'),
(11, 19, 'Engineering Mechanics - Lecture Notes CRS-0068', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_068_ET1123.pdf', 62746, NULL, '2025-11-03 12:21:00'),
(19, 6, 'Electrical Circuits - Lecture Notes CRS-0069', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_069_ET1213.pdf', 62750, NULL, '2026-03-30 12:21:00'),
(12, 20, 'Introduction to Plant Science - Lecture Notes CRS-0070', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_070_BST1113.pdf', 62760, NULL, '2025-11-03 12:21:00'),
(13, 20, 'Biochemistry - Lecture Notes CRS-0071', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_071_BST1123.pdf', 62754, NULL, '2025-11-03 12:21:00'),
(20, 20, 'Biostatistics - Lecture Notes CRS-0072', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_072_BST1213.pdf', 62784, NULL, '2026-03-30 12:21:00'),
(23, 18, 'Mobile Application Development - Lecture Notes CRS-0073', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_073_ICT3113.pdf', 62796, NULL, '2026-11-02 12:21:00'),
(24, 5, 'Information Security - Lecture Notes CRS-0074', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_074_ICT3123.pdf', 62795, NULL, '2026-11-02 12:21:00'),
(25, 19, 'Thermodynamics - Lecture Notes CRS-0075', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_075_ET2113.pdf', 62731, NULL, '2026-11-02 12:21:00'),
(26, 20, 'Soil Science - Lecture Notes CRS-0076', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_076_BST2113.pdf', 62732, NULL, '2026-11-02 12:21:00'),
(6, 3, 'Introduction to Programming - Lecture Notes CRS-0077', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_077_ICT1113.pdf', 62847, NULL, '2025-10-20 13:28:00'),
(2, 4, 'Discrete Mathematics - Lecture Notes CRS-0078', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_078_ICT1123.pdf', 62791, NULL, '2024-10-28 13:28:00'),
(3, 3, 'Database Management Systems - Lecture Notes CRS-0079', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_079_ICT1233.pdf', 62768, NULL, '2025-03-24 13:28:00'),
(4, 5, 'Object Oriented Programming - Lecture Notes CRS-0080', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_080_ICT1223.pdf', 62845, NULL, '2025-03-24 13:28:00'),
(10, 6, 'Engineering Mathematics - Lecture Notes CRS-0081', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_081_ET1113.pdf', 62733, NULL, '2025-11-10 13:28:00'),
(8, 18, 'Computer Networks - Lecture Notes CRS-0082', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_082_ICT2113.pdf', 62793, NULL, '2025-10-27 13:28:00'),
(5, 17, 'Web Technologies - Lecture Notes CRS-0083', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_083_ICT1213.pdf', 62768, NULL, '2025-03-24 13:28:00'),
(9, 17, 'Data Structures and Algorithms - Lecture Notes CRS-0084', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_084_ICT2123.pdf', 62837, NULL, '2025-10-27 13:28:00'),
(17, 18, 'Software Engineering - Lecture Notes CRS-0085', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_085_ICT2213.pdf', 62837, NULL, '2026-04-06 13:28:00'),
(18, 4, 'Operating Systems - Lecture Notes CRS-0086', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_086_ICT2223.pdf', 62797, NULL, '2026-04-06 13:28:00'),
(11, 19, 'Engineering Mechanics - Lecture Notes CRS-0087', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_087_ET1123.pdf', 62748, NULL, '2025-11-10 13:28:00'),
(19, 6, 'Electrical Circuits - Lecture Notes CRS-0088', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_088_ET1213.pdf', 62745, NULL, '2026-04-06 13:28:00'),
(12, 20, 'Introduction to Plant Science - Lecture Notes CRS-0089', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_089_BST1113.pdf', 62762, NULL, '2025-11-10 13:28:00'),
(13, 20, 'Biochemistry - Lecture Notes CRS-0090', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_090_BST1123.pdf', 62753, NULL, '2025-11-10 13:28:00'),
(20, 20, 'Biostatistics - Lecture Notes CRS-0091', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_091_BST1213.pdf', 62788, NULL, '2026-04-06 13:28:00'),
(23, 18, 'Mobile Application Development - Lecture Notes CRS-0092', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_092_ICT3113.pdf', 62795, NULL, '2026-11-09 13:28:00'),
(24, 5, 'Information Security - Lecture Notes CRS-0093', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_093_ICT3123.pdf', 62796, NULL, '2026-11-09 13:28:00'),
(25, 19, 'Thermodynamics - Lecture Notes CRS-0094', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_094_ET2113.pdf', 62729, NULL, '2026-11-09 13:28:00'),
(26, 20, 'Soil Science - Lecture Notes CRS-0095', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_095_BST2113.pdf', 62734, NULL, '2026-11-09 13:28:00'),
(27, 3, 'Introduction to Programming - Lecture Notes CRS-0096', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_096_ICT1113.pdf', 62842, NULL, '2026-10-19 14:35:00'),
(7, 4, 'Discrete Mathematics - Lecture Notes CRS-0097', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_097_ICT1123.pdf', 62796, NULL, '2025-10-27 14:35:00'),
(14, 3, 'Database Management Systems - Lecture Notes CRS-0098', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_098_ICT1233.pdf', 62763, NULL, '2026-03-23 14:35:00'),
(15, 5, 'Object Oriented Programming - Lecture Notes CRS-0099', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_099_ICT1223.pdf', 62848, NULL, '2026-03-23 14:35:00'),
(10, 6, 'Engineering Mathematics - Lecture Notes CRS-0100', 'LECTURE_NOTES', 'src/assets/course_pdfs_sample_pack/course_pdfs/course_100_ET1113.pdf', 62732, NULL, '2025-11-17 14:35:00');


-- Nothing may be dated after "today" (2026-11-01): pull the few later dates back two weeks.
UPDATE course_material SET upload_date = DATE_SUB(upload_date, INTERVAL 14 DAY)
WHERE upload_date > '2026-11-01 23:59:59';

-- session

INSERT INTO `session` (session_id, offering_id, session_number, session_type, session_date, start_time, end_time, room) VALUES
(1, 1, 1, 'LECTURE', '2024-10-07', '08:00:00', '10:00:00', 'LH-01'),
(2, 1, 2, 'LECTURE', '2024-10-21', '08:00:00', '10:00:00', 'LH-01'),
(3, 1, 3, 'PRACTICAL', '2024-11-06', '13:00:00', '16:00:00', 'ICT Lab 1'),
(4, 1, 4, 'LECTURE', '2024-11-18', '08:00:00', '10:00:00', 'LH-01'),
(5, 1, 5, 'LECTURE', '2024-12-02', '08:00:00', '10:00:00', 'LH-01'),
(6, 1, 6, 'PRACTICAL', '2024-12-18', '13:00:00', '16:00:00', 'ICT Lab 1'),
(7, 2, 1, 'LECTURE', '2024-10-08', '08:00:00', '10:00:00', 'LH-02'),
(8, 2, 2, 'LECTURE', '2024-10-22', '08:00:00', '10:00:00', 'LH-02'),
(9, 2, 3, 'LECTURE', '2024-11-05', '08:00:00', '10:00:00', 'LH-02'),
(10, 2, 4, 'LECTURE', '2024-11-19', '08:00:00', '10:00:00', 'LH-02'),
(11, 2, 5, 'LECTURE', '2024-12-03', '08:00:00', '10:00:00', 'LH-02'),
(12, 2, 6, 'LECTURE', '2024-12-17', '08:00:00', '10:00:00', 'LH-02'),
(13, 3, 1, 'LECTURE', '2025-03-03', '08:00:00', '10:00:00', 'LH-01'),
(14, 3, 2, 'LECTURE', '2025-03-17', '08:00:00', '10:00:00', 'LH-01'),
(15, 3, 3, 'PRACTICAL', '2025-04-02', '13:00:00', '16:00:00', 'ICT Lab 1'),
(16, 3, 4, 'LECTURE', '2025-04-14', '08:00:00', '10:00:00', 'LH-01'),
(17, 3, 5, 'LECTURE', '2025-04-28', '08:00:00', '10:00:00', 'LH-01'),
(18, 3, 6, 'PRACTICAL', '2025-05-14', '13:00:00', '16:00:00', 'ICT Lab 1'),
(19, 4, 1, 'LECTURE', '2025-03-04', '08:00:00', '10:00:00', 'LH-02'),
(20, 4, 2, 'LECTURE', '2025-03-18', '08:00:00', '10:00:00', 'LH-02'),
(21, 4, 3, 'PRACTICAL', '2025-04-03', '13:00:00', '16:00:00', 'ICT Lab 2'),
(22, 4, 4, 'LECTURE', '2025-04-15', '08:00:00', '10:00:00', 'LH-02'),
(23, 4, 5, 'LECTURE', '2025-04-29', '08:00:00', '10:00:00', 'LH-02'),
(24, 4, 6, 'PRACTICAL', '2025-05-15', '13:00:00', '16:00:00', 'ICT Lab 2'),
(25, 5, 1, 'LECTURE', '2025-03-05', '08:00:00', '10:00:00', 'LH-03'),
(26, 5, 2, 'LECTURE', '2025-03-19', '08:00:00', '10:00:00', 'LH-03'),
(27, 5, 3, 'PRACTICAL', '2025-04-04', '13:00:00', '16:00:00', 'ICT Lab 1'),
(28, 5, 4, 'LECTURE', '2025-04-16', '08:00:00', '10:00:00', 'LH-03'),
(29, 5, 5, 'LECTURE', '2025-04-30', '08:00:00', '10:00:00', 'LH-03'),
(30, 5, 6, 'PRACTICAL', '2025-05-16', '13:00:00', '16:00:00', 'ICT Lab 1'),
(31, 6, 1, 'LECTURE', '2025-10-06', '08:00:00', '10:00:00', 'LH-01'),
(32, 6, 2, 'LECTURE', '2025-10-20', '08:00:00', '10:00:00', 'LH-01'),
(33, 6, 3, 'PRACTICAL', '2025-11-05', '13:00:00', '16:00:00', 'ICT Lab 1'),
(34, 6, 4, 'LECTURE', '2025-11-17', '08:00:00', '10:00:00', 'LH-01'),
(35, 6, 5, 'LECTURE', '2025-12-01', '08:00:00', '10:00:00', 'LH-01'),
(36, 6, 6, 'PRACTICAL', '2025-12-17', '13:00:00', '16:00:00', 'ICT Lab 1'),
(37, 7, 1, 'LECTURE', '2025-10-07', '08:00:00', '10:00:00', 'LH-02'),
(38, 7, 2, 'LECTURE', '2025-10-21', '08:00:00', '10:00:00', 'LH-02'),
(39, 7, 3, 'LECTURE', '2025-11-04', '08:00:00', '10:00:00', 'LH-02'),
(40, 7, 4, 'LECTURE', '2025-11-18', '08:00:00', '10:00:00', 'LH-02'),
(41, 7, 5, 'LECTURE', '2025-12-02', '08:00:00', '10:00:00', 'LH-02'),
(42, 7, 6, 'LECTURE', '2025-12-16', '08:00:00', '10:00:00', 'LH-02'),
(43, 8, 1, 'LECTURE', '2025-10-08', '08:00:00', '10:00:00', 'LH-03'),
(44, 8, 2, 'LECTURE', '2025-10-22', '08:00:00', '10:00:00', 'LH-03'),
(45, 8, 3, 'PRACTICAL', '2025-11-07', '13:00:00', '16:00:00', 'ICT Lab 1'),
(46, 8, 4, 'LECTURE', '2025-11-19', '08:00:00', '10:00:00', 'LH-03'),
(47, 8, 5, 'LECTURE', '2025-12-03', '08:00:00', '10:00:00', 'LH-03'),
(48, 8, 6, 'PRACTICAL', '2025-12-19', '13:00:00', '16:00:00', 'ICT Lab 1'),
(49, 9, 1, 'LECTURE', '2025-10-09', '08:00:00', '10:00:00', 'LH-01'),
(50, 9, 2, 'LECTURE', '2025-10-23', '08:00:00', '10:00:00', 'LH-01'),
(51, 9, 3, 'PRACTICAL', '2025-11-03', '13:00:00', '16:00:00', 'ICT Lab 2'),
(52, 9, 4, 'LECTURE', '2025-11-20', '08:00:00', '10:00:00', 'LH-01'),
(53, 9, 5, 'LECTURE', '2025-12-04', '08:00:00', '10:00:00', 'LH-01'),
(54, 9, 6, 'PRACTICAL', '2025-12-15', '13:00:00', '16:00:00', 'ICT Lab 2'),
(55, 10, 1, 'LECTURE', '2025-10-10', '08:00:00', '10:00:00', 'ET-LH-01'),
(56, 10, 2, 'LECTURE', '2025-10-24', '08:00:00', '10:00:00', 'ET-LH-01'),
(57, 10, 3, 'LECTURE', '2025-11-07', '08:00:00', '10:00:00', 'ET-LH-01'),
(58, 10, 4, 'LECTURE', '2025-11-21', '08:00:00', '10:00:00', 'ET-LH-01'),
(59, 10, 5, 'LECTURE', '2025-12-05', '08:00:00', '10:00:00', 'ET-LH-01'),
(60, 10, 6, 'LECTURE', '2025-12-19', '08:00:00', '10:00:00', 'ET-LH-01'),
(61, 11, 1, 'LECTURE', '2025-10-06', '10:00:00', '12:00:00', 'ET-LH-01'),
(62, 11, 2, 'LECTURE', '2025-10-20', '10:00:00', '12:00:00', 'ET-LH-01'),
(63, 11, 3, 'LECTURE', '2025-11-03', '10:00:00', '12:00:00', 'ET-LH-01'),
(64, 11, 4, 'LECTURE', '2025-11-17', '10:00:00', '12:00:00', 'ET-LH-01'),
(65, 11, 5, 'LECTURE', '2025-12-01', '10:00:00', '12:00:00', 'ET-LH-01'),
(66, 11, 6, 'LECTURE', '2025-12-15', '10:00:00', '12:00:00', 'ET-LH-01'),
(67, 12, 1, 'LECTURE', '2025-10-07', '10:00:00', '12:00:00', 'BST-LH-01'),
(68, 12, 2, 'LECTURE', '2025-10-21', '10:00:00', '12:00:00', 'BST-LH-01'),
(69, 12, 3, 'PRACTICAL', '2025-11-06', '13:00:00', '16:00:00', 'BST Lab 1'),
(70, 12, 4, 'LECTURE', '2025-11-18', '10:00:00', '12:00:00', 'BST-LH-01'),
(71, 12, 5, 'LECTURE', '2025-12-02', '10:00:00', '12:00:00', 'BST-LH-01'),
(72, 12, 6, 'PRACTICAL', '2025-12-18', '13:00:00', '16:00:00', 'BST Lab 1'),
(73, 13, 1, 'LECTURE', '2025-10-08', '10:00:00', '12:00:00', 'BST-LH-01'),
(74, 13, 2, 'LECTURE', '2025-10-22', '10:00:00', '12:00:00', 'BST-LH-01'),
(75, 13, 3, 'LECTURE', '2025-11-05', '10:00:00', '12:00:00', 'BST-LH-01'),
(76, 13, 4, 'LECTURE', '2025-11-19', '10:00:00', '12:00:00', 'BST-LH-01'),
(77, 13, 5, 'LECTURE', '2025-12-03', '10:00:00', '12:00:00', 'BST-LH-01'),
(78, 13, 6, 'LECTURE', '2025-12-17', '10:00:00', '12:00:00', 'BST-LH-01'),
(79, 14, 1, 'LECTURE', '2026-03-02', '08:00:00', '10:00:00', 'LH-01'),
(80, 14, 2, 'LECTURE', '2026-03-16', '08:00:00', '10:00:00', 'LH-01'),
(81, 14, 3, 'PRACTICAL', '2026-04-01', '13:00:00', '16:00:00', 'ICT Lab 1'),
(82, 14, 4, 'LECTURE', '2026-04-13', '08:00:00', '10:00:00', 'LH-01'),
(83, 14, 5, 'LECTURE', '2026-04-27', '08:00:00', '10:00:00', 'LH-01'),
(84, 14, 6, 'PRACTICAL', '2026-05-13', '13:00:00', '16:00:00', 'ICT Lab 1'),
(85, 15, 1, 'LECTURE', '2026-03-03', '08:00:00', '10:00:00', 'LH-02'),
(86, 15, 2, 'LECTURE', '2026-03-17', '08:00:00', '10:00:00', 'LH-02'),
(87, 15, 3, 'PRACTICAL', '2026-04-02', '13:00:00', '16:00:00', 'ICT Lab 2'),
(88, 15, 4, 'LECTURE', '2026-04-14', '08:00:00', '10:00:00', 'LH-02'),
(89, 15, 5, 'LECTURE', '2026-04-28', '08:00:00', '10:00:00', 'LH-02'),
(90, 15, 6, 'PRACTICAL', '2026-05-14', '13:00:00', '16:00:00', 'ICT Lab 2'),
(91, 16, 1, 'LECTURE', '2026-03-04', '08:00:00', '10:00:00', 'LH-03'),
(92, 16, 2, 'LECTURE', '2026-03-18', '08:00:00', '10:00:00', 'LH-03'),
(93, 16, 3, 'PRACTICAL', '2026-04-03', '13:00:00', '16:00:00', 'ICT Lab 1'),
(94, 16, 4, 'LECTURE', '2026-04-15', '08:00:00', '10:00:00', 'LH-03'),
(95, 16, 5, 'LECTURE', '2026-04-29', '08:00:00', '10:00:00', 'LH-03'),
(96, 16, 6, 'PRACTICAL', '2026-05-15', '13:00:00', '16:00:00', 'ICT Lab 1'),
(97, 17, 1, 'LECTURE', '2026-03-05', '08:00:00', '10:00:00', 'LH-01'),
(98, 17, 2, 'LECTURE', '2026-03-19', '08:00:00', '10:00:00', 'LH-01'),
(99, 17, 3, 'LECTURE', '2026-04-02', '08:00:00', '10:00:00', 'LH-01'),
(100, 17, 4, 'LECTURE', '2026-04-16', '08:00:00', '10:00:00', 'LH-01'),
(101, 17, 5, 'LECTURE', '2026-04-30', '08:00:00', '10:00:00', 'LH-01'),
(102, 17, 6, 'LECTURE', '2026-05-14', '08:00:00', '10:00:00', 'LH-01'),
(103, 18, 1, 'LECTURE', '2026-03-06', '08:00:00', '10:00:00', 'LH-02'),
(104, 18, 2, 'LECTURE', '2026-03-20', '08:00:00', '10:00:00', 'LH-02'),
(105, 18, 3, 'LECTURE', '2026-04-03', '08:00:00', '10:00:00', 'LH-02'),
(106, 18, 4, 'LECTURE', '2026-04-17', '08:00:00', '10:00:00', 'LH-02'),
(107, 18, 5, 'LECTURE', '2026-05-01', '08:00:00', '10:00:00', 'LH-02'),
(108, 18, 6, 'LECTURE', '2026-05-15', '08:00:00', '10:00:00', 'LH-02'),
(109, 19, 1, 'LECTURE', '2026-03-02', '10:00:00', '12:00:00', 'ET-LH-01'),
(110, 19, 2, 'LECTURE', '2026-03-16', '10:00:00', '12:00:00', 'ET-LH-01'),
(111, 19, 3, 'PRACTICAL', '2026-04-01', '13:00:00', '16:00:00', 'ET Lab 1'),
(112, 19, 4, 'LECTURE', '2026-04-13', '10:00:00', '12:00:00', 'ET-LH-01'),
(113, 19, 5, 'LECTURE', '2026-04-27', '10:00:00', '12:00:00', 'ET-LH-01'),
(114, 19, 6, 'PRACTICAL', '2026-05-13', '13:00:00', '16:00:00', 'ET Lab 1'),
(115, 20, 1, 'LECTURE', '2026-03-03', '10:00:00', '12:00:00', 'BST-LH-01'),
(116, 20, 2, 'LECTURE', '2026-03-17', '10:00:00', '12:00:00', 'BST-LH-01'),
(117, 20, 3, 'LECTURE', '2026-03-31', '10:00:00', '12:00:00', 'BST-LH-01'),
(118, 20, 4, 'LECTURE', '2026-04-14', '10:00:00', '12:00:00', 'BST-LH-01'),
(119, 20, 5, 'LECTURE', '2026-04-28', '10:00:00', '12:00:00', 'BST-LH-01'),
(120, 20, 6, 'LECTURE', '2026-05-12', '10:00:00', '12:00:00', 'BST-LH-01'),
(121, 21, 1, 'LECTURE', '2026-10-05', '08:00:00', '10:00:00', 'LH-01'),
(122, 21, 2, 'LECTURE', '2026-10-12', '08:00:00', '10:00:00', 'LH-01'),
(123, 21, 3, 'PRACTICAL', '2026-10-21', '13:00:00', '16:00:00', 'ICT Lab 1'),
(124, 21, 4, 'LECTURE', '2026-10-26', '08:00:00', '10:00:00', 'LH-01'),
(125, 22, 1, 'LECTURE', '2026-10-06', '08:00:00', '10:00:00', 'LH-02'),
(126, 22, 2, 'LECTURE', '2026-10-13', '08:00:00', '10:00:00', 'LH-02'),
(127, 22, 3, 'PRACTICAL', '2026-10-22', '13:00:00', '16:00:00', 'ICT Lab 2'),
(128, 22, 4, 'LECTURE', '2026-10-27', '08:00:00', '10:00:00', 'LH-02'),
(129, 23, 1, 'LECTURE', '2026-10-07', '08:00:00', '10:00:00', 'LH-03'),
(130, 23, 2, 'LECTURE', '2026-10-14', '08:00:00', '10:00:00', 'LH-03'),
(131, 23, 3, 'PRACTICAL', '2026-10-23', '13:00:00', '16:00:00', 'ICT Lab 1'),
(132, 23, 4, 'LECTURE', '2026-10-28', '08:00:00', '10:00:00', 'LH-03'),
(133, 24, 1, 'LECTURE', '2026-10-08', '08:00:00', '10:00:00', 'LH-01'),
(134, 24, 2, 'LECTURE', '2026-10-15', '08:00:00', '10:00:00', 'LH-01'),
(135, 24, 3, 'LECTURE', '2026-10-22', '08:00:00', '10:00:00', 'LH-01'),
(136, 24, 4, 'LECTURE', '2026-10-29', '08:00:00', '10:00:00', 'LH-01'),
(137, 25, 1, 'LECTURE', '2026-10-09', '08:00:00', '10:00:00', 'ET-LH-01'),
(138, 25, 2, 'LECTURE', '2026-10-16', '08:00:00', '10:00:00', 'ET-LH-01'),
(139, 25, 3, 'LECTURE', '2026-10-23', '08:00:00', '10:00:00', 'ET-LH-01'),
(140, 25, 4, 'LECTURE', '2026-10-30', '08:00:00', '10:00:00', 'ET-LH-01'),
(141, 26, 1, 'LECTURE', '2026-10-05', '10:00:00', '12:00:00', 'BST-LH-01'),
(142, 26, 2, 'LECTURE', '2026-10-12', '10:00:00', '12:00:00', 'BST-LH-01'),
(143, 26, 3, 'LECTURE', '2026-10-19', '10:00:00', '12:00:00', 'BST-LH-01'),
(144, 26, 4, 'LECTURE', '2026-10-26', '10:00:00', '12:00:00', 'BST-LH-01'),
(145, 27, 1, 'LECTURE', '2026-10-06', '10:00:00', '12:00:00', 'LH-01'),
(146, 27, 2, 'LECTURE', '2026-10-13', '10:00:00', '12:00:00', 'LH-01'),
(147, 27, 3, 'PRACTICAL', '2026-10-22', '13:00:00', '16:00:00', 'ICT Lab 1'),
(148, 27, 4, 'LECTURE', '2026-10-27', '10:00:00', '12:00:00', 'LH-01');

-- attend

INSERT INTO attend (student_id, session_id, attend_status, recorded_date) VALUES
(23, 1, 'PRESENT', '2024-10-07'), (24, 1, 'PRESENT', '2024-10-07'), (25, 1, 'LATE', '2024-10-07'),
(26, 1, 'ABSENT', '2024-10-07'), (27, 1, 'PRESENT', '2024-10-07'), (28, 1, 'PRESENT', '2024-10-07'),
(29, 1, 'PRESENT', '2024-10-07'), (30, 1, 'PRESENT', '2024-10-07'), (23, 2, 'LATE', '2024-10-21'),
(24, 2, 'PRESENT', '2024-10-21'), (25, 2, 'PRESENT', '2024-10-21'), (26, 2, 'PRESENT', '2024-10-21'),
(27, 2, 'PRESENT', '2024-10-21'), (28, 2, 'LATE', '2024-10-21'), (29, 2, 'PRESENT', '2024-10-21'),
(30, 2, 'PRESENT', '2024-10-21'), (23, 3, 'PRESENT', '2024-11-06'), (24, 3, 'PRESENT', '2024-11-06'),
(25, 3, 'PRESENT', '2024-11-06'), (26, 3, 'PRESENT', '2024-11-06'), (27, 3, 'PRESENT', '2024-11-06'),
(28, 3, 'PRESENT', '2024-11-06'), (29, 3, 'PRESENT', '2024-11-06'), (30, 3, 'PRESENT', '2024-11-06'),
(23, 4, 'PRESENT', '2024-11-18'), (24, 4, 'PRESENT', '2024-11-18'), (25, 4, 'PRESENT', '2024-11-18'),
(26, 4, 'PRESENT', '2024-11-18'), (27, 4, 'PRESENT', '2024-11-18'), (28, 4, 'PRESENT', '2024-11-18'),
(29, 4, 'LATE', '2024-11-18'), (30, 4, 'PRESENT', '2024-11-18'), (23, 5, 'PRESENT', '2024-12-02'),
(24, 5, 'PRESENT', '2024-12-02'), (25, 5, 'PRESENT', '2024-12-02'), (26, 5, 'PRESENT', '2024-12-02'),
(27, 5, 'PRESENT', '2024-12-02'), (28, 5, 'PRESENT', '2024-12-02'), (29, 5, 'PRESENT', '2024-12-02'),
(30, 5, 'PRESENT', '2024-12-02'), (23, 6, 'PRESENT', '2024-12-18'), (24, 6, 'PRESENT', '2024-12-18'),
(25, 6, 'PRESENT', '2024-12-18'), (26, 6, 'LATE', '2024-12-18'), (27, 6, 'PRESENT', '2024-12-18'),
(28, 6, 'PRESENT', '2024-12-18'), (29, 6, 'PRESENT', '2024-12-18'), (30, 6, 'PRESENT', '2024-12-18'),
(23, 7, 'PRESENT', '2024-10-08'), (24, 7, 'PRESENT', '2024-10-08'), (25, 7, 'PRESENT', '2024-10-08'),
(26, 7, 'PRESENT', '2024-10-08'), (27, 7, 'LATE', '2024-10-08'), (28, 7, 'PRESENT', '2024-10-08'),
(29, 7, 'ABSENT', '2024-10-08'), (30, 7, 'PRESENT', '2024-10-08'), (23, 8, 'PRESENT', '2024-10-22'),
(24, 8, 'PRESENT', '2024-10-22'), (25, 8, 'PRESENT', '2024-10-22'), (26, 8, 'PRESENT', '2024-10-22'),
(27, 8, 'PRESENT', '2024-10-22'), (28, 8, 'PRESENT', '2024-10-22'), (29, 8, 'PRESENT', '2024-10-22'),
(30, 8, 'PRESENT', '2024-10-22'), (23, 9, 'PRESENT', '2024-11-05'), (24, 9, 'PRESENT', '2024-11-05'),
(25, 9, 'PRESENT', '2024-11-05'), (26, 9, 'PRESENT', '2024-11-05'), (27, 9, 'PRESENT', '2024-11-05'),
(28, 9, 'PRESENT', '2024-11-05'), (29, 9, 'PRESENT', '2024-11-05'), (30, 9, 'PRESENT', '2024-11-05'),
(23, 10, 'PRESENT', '2024-11-19'), (24, 10, 'PRESENT', '2024-11-19'), (25, 10, 'PRESENT', '2024-11-19'),
(26, 10, 'PRESENT', '2024-11-19'), (27, 10, 'PRESENT', '2024-11-19'), (28, 10, 'PRESENT', '2024-11-19'),
(29, 10, 'PRESENT', '2024-11-19'), (30, 10, 'PRESENT', '2024-11-19'), (23, 11, 'PRESENT', '2024-12-03'),
(24, 11, 'PRESENT', '2024-12-03'), (25, 11, 'PRESENT', '2024-12-03'), (26, 11, 'PRESENT', '2024-12-03'),
(27, 11, 'PRESENT', '2024-12-03'), (28, 11, 'PRESENT', '2024-12-03'), (29, 11, 'LATE', '2024-12-03'),
(30, 11, 'PRESENT', '2024-12-03'), (23, 12, 'PRESENT', '2024-12-17'), (24, 12, 'LATE', '2024-12-17'),
(25, 12, 'PRESENT', '2024-12-17'), (26, 12, 'PRESENT', '2024-12-17'), (27, 12, 'PRESENT', '2024-12-17'),
(28, 12, 'ABSENT', '2024-12-17'), (29, 12, 'PRESENT', '2024-12-17'), (30, 12, 'PRESENT', '2024-12-17'),
(23, 13, 'PRESENT', '2025-03-03'), (24, 13, 'PRESENT', '2025-03-03'), (25, 13, 'PRESENT', '2025-03-03'),
(26, 13, 'PRESENT', '2025-03-03'), (27, 13, 'PRESENT', '2025-03-03'), (28, 13, 'PRESENT', '2025-03-03'),
(29, 13, 'PRESENT', '2025-03-03'), (30, 13, 'PRESENT', '2025-03-03'), (23, 14, 'PRESENT', '2025-03-17'),
(24, 14, 'PRESENT', '2025-03-17'), (25, 14, 'PRESENT', '2025-03-17'), (26, 14, 'PRESENT', '2025-03-17'),
(27, 14, 'PRESENT', '2025-03-17'), (28, 14, 'PRESENT', '2025-03-17'), (29, 14, 'PRESENT', '2025-03-17'),
(30, 14, 'LATE', '2025-03-17'), (23, 15, 'PRESENT', '2025-04-02'), (24, 15, 'PRESENT', '2025-04-02'),
(25, 15, 'ABSENT', '2025-04-02'), (26, 15, 'PRESENT', '2025-04-02'), (27, 15, 'PRESENT', '2025-04-02'),
(28, 15, 'PRESENT', '2025-04-02'), (29, 15, 'ABSENT', '2025-04-02'), (30, 15, 'ABSENT', '2025-04-02'),
(23, 16, 'PRESENT', '2025-04-14'), (24, 16, 'PRESENT', '2025-04-14'), (25, 16, 'PRESENT', '2025-04-14'),
(26, 16, 'PRESENT', '2025-04-14'), (27, 16, 'PRESENT', '2025-04-14'), (28, 16, 'PRESENT', '2025-04-14'),
(29, 16, 'ABSENT', '2025-04-14'), (30, 16, 'PRESENT', '2025-04-14'), (23, 17, 'PRESENT', '2025-04-28'),
(24, 17, 'PRESENT', '2025-04-28'), (25, 17, 'PRESENT', '2025-04-28'), (26, 17, 'PRESENT', '2025-04-28'),
(27, 17, 'PRESENT', '2025-04-28'), (28, 17, 'MEDICAL', '2025-04-28'), (29, 17, 'LATE', '2025-04-28'),
(30, 17, 'PRESENT', '2025-04-28'), (23, 18, 'PRESENT', '2025-05-14'), (24, 18, 'PRESENT', '2025-05-14'),
(25, 18, 'PRESENT', '2025-05-14'), (26, 18, 'PRESENT', '2025-05-14'), (27, 18, 'PRESENT', '2025-05-14'),
(28, 18, 'PRESENT', '2025-05-14'), (29, 18, 'PRESENT', '2025-05-14'), (30, 18, 'PRESENT', '2025-05-14'),
(23, 19, 'PRESENT', '2025-03-04'), (24, 19, 'PRESENT', '2025-03-04'), (25, 19, 'PRESENT', '2025-03-04'),
(26, 19, 'PRESENT', '2025-03-04'), (27, 19, 'LATE', '2025-03-04'), (28, 19, 'PRESENT', '2025-03-04');

INSERT INTO attend (student_id, session_id, attend_status, recorded_date) VALUES
(29, 19, 'PRESENT', '2025-03-04'), (30, 19, 'PRESENT', '2025-03-04'), (23, 20, 'LATE', '2025-03-18'),
(24, 20, 'PRESENT', '2025-03-18'), (25, 20, 'PRESENT', '2025-03-18'), (26, 20, 'MEDICAL', '2025-03-18'),
(27, 20, 'PRESENT', '2025-03-18'), (28, 20, 'PRESENT', '2025-03-18'), (29, 20, 'PRESENT', '2025-03-18'),
(30, 20, 'PRESENT', '2025-03-18'), (23, 21, 'PRESENT', '2025-04-03'), (24, 21, 'PRESENT', '2025-04-03'),
(25, 21, 'PRESENT', '2025-04-03'), (26, 21, 'PRESENT', '2025-04-03'), (27, 21, 'PRESENT', '2025-04-03'),
(28, 21, 'PRESENT', '2025-04-03'), (29, 21, 'ABSENT', '2025-04-03'), (30, 21, 'PRESENT', '2025-04-03'),
(23, 22, 'PRESENT', '2025-04-15'), (24, 22, 'PRESENT', '2025-04-15'), (25, 22, 'PRESENT', '2025-04-15'),
(26, 22, 'PRESENT', '2025-04-15'), (27, 22, 'PRESENT', '2025-04-15'), (28, 22, 'LATE', '2025-04-15'),
(29, 22, 'PRESENT', '2025-04-15'), (30, 22, 'PRESENT', '2025-04-15'), (23, 23, 'PRESENT', '2025-04-29'),
(24, 23, 'PRESENT', '2025-04-29'), (25, 23, 'PRESENT', '2025-04-29'), (26, 23, 'PRESENT', '2025-04-29'),
(27, 23, 'PRESENT', '2025-04-29'), (28, 23, 'PRESENT', '2025-04-29'), (29, 23, 'ABSENT', '2025-04-29'),
(30, 23, 'PRESENT', '2025-04-29'), (23, 24, 'PRESENT', '2025-05-15'), (24, 24, 'PRESENT', '2025-05-15'),
(25, 24, 'PRESENT', '2025-05-15'), (26, 24, 'PRESENT', '2025-05-15'), (27, 24, 'PRESENT', '2025-05-15'),
(28, 24, 'ABSENT', '2025-05-15'), (29, 24, 'PRESENT', '2025-05-15'), (30, 24, 'PRESENT', '2025-05-15'),
(23, 25, 'PRESENT', '2025-03-05'), (24, 25, 'PRESENT', '2025-03-05'), (25, 25, 'PRESENT', '2025-03-05'),
(26, 25, 'PRESENT', '2025-03-05'), (27, 25, 'PRESENT', '2025-03-05'), (28, 25, 'PRESENT', '2025-03-05'),
(29, 25, 'PRESENT', '2025-03-05'), (30, 25, 'PRESENT', '2025-03-05'), (23, 26, 'ABSENT', '2025-03-19'),
(24, 26, 'PRESENT', '2025-03-19'), (25, 26, 'PRESENT', '2025-03-19'), (26, 26, 'PRESENT', '2025-03-19'),
(27, 26, 'PRESENT', '2025-03-19'), (28, 26, 'PRESENT', '2025-03-19'), (29, 26, 'PRESENT', '2025-03-19'),
(30, 26, 'PRESENT', '2025-03-19'), (23, 27, 'PRESENT', '2025-04-04'), (24, 27, 'PRESENT', '2025-04-04'),
(25, 27, 'PRESENT', '2025-04-04'), (26, 27, 'ABSENT', '2025-04-04'), (27, 27, 'PRESENT', '2025-04-04'),
(28, 27, 'PRESENT', '2025-04-04'), (29, 27, 'ABSENT', '2025-04-04'), (30, 27, 'PRESENT', '2025-04-04'),
(23, 28, 'PRESENT', '2025-04-16'), (24, 28, 'PRESENT', '2025-04-16'), (25, 28, 'PRESENT', '2025-04-16'),
(26, 28, 'PRESENT', '2025-04-16'), (27, 28, 'PRESENT', '2025-04-16'), (28, 28, 'PRESENT', '2025-04-16'),
(29, 28, 'PRESENT', '2025-04-16'), (30, 28, 'PRESENT', '2025-04-16'), (23, 29, 'PRESENT', '2025-04-30'),
(24, 29, 'PRESENT', '2025-04-30'), (25, 29, 'ABSENT', '2025-04-30'), (26, 29, 'LATE', '2025-04-30'),
(27, 29, 'PRESENT', '2025-04-30'), (28, 29, 'PRESENT', '2025-04-30'), (29, 29, 'PRESENT', '2025-04-30'),
(30, 29, 'PRESENT', '2025-04-30'), (23, 30, 'PRESENT', '2025-05-16'), (24, 30, 'PRESENT', '2025-05-16'),
(25, 30, 'ABSENT', '2025-05-16'), (26, 30, 'PRESENT', '2025-05-16'), (27, 30, 'PRESENT', '2025-05-16'),
(28, 30, 'LATE', '2025-05-16'), (29, 30, 'PRESENT', '2025-05-16'), (30, 30, 'PRESENT', '2025-05-16'),
(9, 31, 'PRESENT', '2025-10-06'), (10, 31, 'PRESENT', '2025-10-06'), (11, 31, 'LATE', '2025-10-06'),
(12, 31, 'LATE', '2025-10-06'), (13, 31, 'ABSENT', '2025-10-06'), (31, 31, 'PRESENT', '2025-10-06'),
(32, 31, 'PRESENT', '2025-10-06'), (33, 31, 'PRESENT', '2025-10-06'), (34, 31, 'PRESENT', '2025-10-06'),
(9, 32, 'ABSENT', '2025-10-20'), (10, 32, 'PRESENT', '2025-10-20'), (11, 32, 'PRESENT', '2025-10-20'),
(12, 32, 'PRESENT', '2025-10-20'), (13, 32, 'PRESENT', '2025-10-20'), (31, 32, 'PRESENT', '2025-10-20'),
(32, 32, 'PRESENT', '2025-10-20'), (33, 32, 'LATE', '2025-10-20'), (34, 32, 'MEDICAL', '2025-10-20'),
(9, 33, 'PRESENT', '2025-11-05'), (10, 33, 'PRESENT', '2025-11-05'), (11, 33, 'PRESENT', '2025-11-05'),
(12, 33, 'LATE', '2025-11-05'), (13, 33, 'PRESENT', '2025-11-05'), (31, 33, 'PRESENT', '2025-11-05'),
(32, 33, 'PRESENT', '2025-11-05'), (33, 33, 'PRESENT', '2025-11-05'), (34, 33, 'PRESENT', '2025-11-05'),
(9, 34, 'LATE', '2025-11-17'), (10, 34, 'PRESENT', '2025-11-17'), (11, 34, 'PRESENT', '2025-11-17'),
(12, 34, 'PRESENT', '2025-11-17'), (13, 34, 'PRESENT', '2025-11-17'), (31, 34, 'MEDICAL', '2025-11-17'),
(32, 34, 'PRESENT', '2025-11-17'), (33, 34, 'PRESENT', '2025-11-17'), (34, 34, 'PRESENT', '2025-11-17'),
(9, 35, 'PRESENT', '2025-12-01'), (10, 35, 'PRESENT', '2025-12-01'), (11, 35, 'PRESENT', '2025-12-01'),
(12, 35, 'PRESENT', '2025-12-01'), (13, 35, 'PRESENT', '2025-12-01'), (31, 35, 'PRESENT', '2025-12-01'),
(32, 35, 'PRESENT', '2025-12-01'), (33, 35, 'PRESENT', '2025-12-01'), (34, 35, 'PRESENT', '2025-12-01'),
(9, 36, 'PRESENT', '2025-12-17'), (10, 36, 'PRESENT', '2025-12-17'), (11, 36, 'PRESENT', '2025-12-17'),
(12, 36, 'PRESENT', '2025-12-17'), (13, 36, 'PRESENT', '2025-12-17'), (31, 36, 'PRESENT', '2025-12-17'),
(32, 36, 'PRESENT', '2025-12-17'), (33, 36, 'PRESENT', '2025-12-17'), (34, 36, 'PRESENT', '2025-12-17'),
(9, 37, 'LATE', '2025-10-07'), (10, 37, 'PRESENT', '2025-10-07'), (11, 37, 'PRESENT', '2025-10-07'),
(12, 37, 'PRESENT', '2025-10-07'), (13, 37, 'PRESENT', '2025-10-07'), (23, 37, 'PRESENT', '2025-10-07');

INSERT INTO attend (student_id, session_id, attend_status, recorded_date) VALUES
(31, 37, 'PRESENT', '2025-10-07'), (32, 37, 'PRESENT', '2025-10-07'), (33, 37, 'PRESENT', '2025-10-07'),
(34, 37, 'PRESENT', '2025-10-07'), (9, 38, 'PRESENT', '2025-10-21'), (10, 38, 'PRESENT', '2025-10-21'),
(11, 38, 'PRESENT', '2025-10-21'), (12, 38, 'PRESENT', '2025-10-21'), (13, 38, 'PRESENT', '2025-10-21'),
(23, 38, 'PRESENT', '2025-10-21'), (31, 38, 'ABSENT', '2025-10-21'), (32, 38, 'LATE', '2025-10-21'),
(33, 38, 'PRESENT', '2025-10-21'), (34, 38, 'PRESENT', '2025-10-21'), (9, 39, 'PRESENT', '2025-11-04'),
(10, 39, 'PRESENT', '2025-11-04'), (11, 39, 'ABSENT', '2025-11-04'), (12, 39, 'PRESENT', '2025-11-04'),
(13, 39, 'PRESENT', '2025-11-04'), (23, 39, 'PRESENT', '2025-11-04'), (31, 39, 'PRESENT', '2025-11-04'),
(32, 39, 'PRESENT', '2025-11-04'), (33, 39, 'PRESENT', '2025-11-04'), (34, 39, 'PRESENT', '2025-11-04'),
(9, 40, 'PRESENT', '2025-11-18'), (10, 40, 'PRESENT', '2025-11-18'), (11, 40, 'MEDICAL', '2025-11-18'),
(12, 40, 'PRESENT', '2025-11-18'), (13, 40, 'PRESENT', '2025-11-18'), (23, 40, 'PRESENT', '2025-11-18'),
(31, 40, 'PRESENT', '2025-11-18'), (32, 40, 'PRESENT', '2025-11-18'), (33, 40, 'PRESENT', '2025-11-18'),
(34, 40, 'PRESENT', '2025-11-18'), (9, 41, 'PRESENT', '2025-12-02'), (10, 41, 'ABSENT', '2025-12-02'),
(11, 41, 'LATE', '2025-12-02'), (12, 41, 'MEDICAL', '2025-12-02'), (13, 41, 'PRESENT', '2025-12-02'),
(23, 41, 'PRESENT', '2025-12-02'), (31, 41, 'PRESENT', '2025-12-02'), (32, 41, 'PRESENT', '2025-12-02'),
(33, 41, 'LATE', '2025-12-02'), (34, 41, 'PRESENT', '2025-12-02'), (9, 42, 'PRESENT', '2025-12-16'),
(10, 42, 'PRESENT', '2025-12-16'), (11, 42, 'PRESENT', '2025-12-16'), (12, 42, 'PRESENT', '2025-12-16'),
(13, 42, 'ABSENT', '2025-12-16'), (23, 42, 'PRESENT', '2025-12-16'), (31, 42, 'PRESENT', '2025-12-16'),
(32, 42, 'PRESENT', '2025-12-16'), (33, 42, 'PRESENT', '2025-12-16'), (34, 42, 'PRESENT', '2025-12-16'),
(23, 43, 'PRESENT', '2025-10-08'), (24, 43, 'PRESENT', '2025-10-08'), (25, 43, 'LATE', '2025-10-08'),
(26, 43, 'PRESENT', '2025-10-08'), (27, 43, 'PRESENT', '2025-10-08'), (28, 43, 'PRESENT', '2025-10-08'),
(29, 43, 'PRESENT', '2025-10-08'), (30, 43, 'PRESENT', '2025-10-08'), (23, 44, 'PRESENT', '2025-10-22'),
(24, 44, 'PRESENT', '2025-10-22'), (25, 44, 'PRESENT', '2025-10-22'), (26, 44, 'PRESENT', '2025-10-22'),
(27, 44, 'PRESENT', '2025-10-22'), (28, 44, 'PRESENT', '2025-10-22'), (29, 44, 'PRESENT', '2025-10-22'),
(30, 44, 'PRESENT', '2025-10-22'), (23, 45, 'PRESENT', '2025-11-07'), (24, 45, 'PRESENT', '2025-11-07'),
(25, 45, 'PRESENT', '2025-11-07'), (26, 45, 'PRESENT', '2025-11-07'), (27, 45, 'PRESENT', '2025-11-07'),
(28, 45, 'PRESENT', '2025-11-07'), (29, 45, 'PRESENT', '2025-11-07'), (30, 45, 'PRESENT', '2025-11-07'),
(23, 46, 'PRESENT', '2025-11-19'), (24, 46, 'PRESENT', '2025-11-19'), (25, 46, 'PRESENT', '2025-11-19'),
(26, 46, 'PRESENT', '2025-11-19'), (27, 46, 'PRESENT', '2025-11-19'), (28, 46, 'PRESENT', '2025-11-19'),
(29, 46, 'PRESENT', '2025-11-19'), (30, 46, 'PRESENT', '2025-11-19'), (23, 47, 'PRESENT', '2025-12-03'),
(24, 47, 'PRESENT', '2025-12-03'), (25, 47, 'PRESENT', '2025-12-03'), (26, 47, 'PRESENT', '2025-12-03'),
(27, 47, 'PRESENT', '2025-12-03'), (28, 47, 'PRESENT', '2025-12-03'), (29, 47, 'LATE', '2025-12-03'),
(30, 47, 'LATE', '2025-12-03'), (23, 48, 'PRESENT', '2025-12-19'), (24, 48, 'PRESENT', '2025-12-19'),
(25, 48, 'PRESENT', '2025-12-19'), (26, 48, 'PRESENT', '2025-12-19'), (27, 48, 'PRESENT', '2025-12-19'),
(28, 48, 'PRESENT', '2025-12-19'), (29, 48, 'PRESENT', '2025-12-19'), (30, 48, 'PRESENT', '2025-12-19'),
(23, 49, 'PRESENT', '2025-10-09'), (24, 49, 'PRESENT', '2025-10-09'), (25, 49, 'PRESENT', '2025-10-09'),
(26, 49, 'PRESENT', '2025-10-09'), (27, 49, 'PRESENT', '2025-10-09'), (28, 49, 'PRESENT', '2025-10-09'),
(29, 49, 'PRESENT', '2025-10-09'), (30, 49, 'PRESENT', '2025-10-09'), (23, 50, 'PRESENT', '2025-10-23'),
(24, 50, 'LATE', '2025-10-23'), (25, 50, 'LATE', '2025-10-23'), (26, 50, 'LATE', '2025-10-23'),
(27, 50, 'PRESENT', '2025-10-23'), (28, 50, 'PRESENT', '2025-10-23'), (29, 50, 'PRESENT', '2025-10-23'),
(30, 50, 'PRESENT', '2025-10-23'), (23, 51, 'PRESENT', '2025-11-03'), (24, 51, 'PRESENT', '2025-11-03'),
(25, 51, 'PRESENT', '2025-11-03'), (26, 51, 'PRESENT', '2025-11-03'), (27, 51, 'PRESENT', '2025-11-03'),
(28, 51, 'PRESENT', '2025-11-03'), (29, 51, 'PRESENT', '2025-11-03'), (30, 51, 'PRESENT', '2025-11-03'),
(23, 52, 'PRESENT', '2025-11-20'), (24, 52, 'PRESENT', '2025-11-20'), (25, 52, 'ABSENT', '2025-11-20'),
(26, 52, 'PRESENT', '2025-11-20'), (27, 52, 'PRESENT', '2025-11-20'), (28, 52, 'PRESENT', '2025-11-20'),
(29, 52, 'PRESENT', '2025-11-20'), (30, 52, 'PRESENT', '2025-11-20'), (23, 53, 'PRESENT', '2025-12-04'),
(24, 53, 'PRESENT', '2025-12-04'), (25, 53, 'PRESENT', '2025-12-04'), (26, 53, 'PRESENT', '2025-12-04'),
(27, 53, 'PRESENT', '2025-12-04'), (28, 53, 'PRESENT', '2025-12-04'), (29, 53, 'ABSENT', '2025-12-04'),
(30, 53, 'PRESENT', '2025-12-04'), (23, 54, 'PRESENT', '2025-12-15'), (24, 54, 'LATE', '2025-12-15'),
(25, 54, 'PRESENT', '2025-12-15'), (26, 54, 'PRESENT', '2025-12-15'), (27, 54, 'PRESENT', '2025-12-15'),
(28, 54, 'PRESENT', '2025-12-15'), (29, 54, 'PRESENT', '2025-12-15'), (30, 54, 'PRESENT', '2025-12-15');

INSERT INTO attend (student_id, session_id, attend_status, recorded_date) VALUES
(14, 55, 'PRESENT', '2025-10-10'), (15, 55, 'PRESENT', '2025-10-10'), (35, 55, 'PRESENT', '2025-10-10'),
(36, 55, 'PRESENT', '2025-10-10'), (37, 55, 'PRESENT', '2025-10-10'), (38, 55, 'PRESENT', '2025-10-10'),
(14, 56, 'PRESENT', '2025-10-24'), (15, 56, 'PRESENT', '2025-10-24'), (35, 56, 'PRESENT', '2025-10-24'),
(36, 56, 'PRESENT', '2025-10-24'), (37, 56, 'PRESENT', '2025-10-24'), (38, 56, 'LATE', '2025-10-24'),
(14, 57, 'PRESENT', '2025-11-07'), (15, 57, 'PRESENT', '2025-11-07'), (35, 57, 'PRESENT', '2025-11-07'),
(36, 57, 'PRESENT', '2025-11-07'), (37, 57, 'PRESENT', '2025-11-07'), (38, 57, 'PRESENT', '2025-11-07'),
(14, 58, 'PRESENT', '2025-11-21'), (15, 58, 'PRESENT', '2025-11-21'), (35, 58, 'PRESENT', '2025-11-21'),
(36, 58, 'PRESENT', '2025-11-21'), (37, 58, 'PRESENT', '2025-11-21'), (38, 58, 'PRESENT', '2025-11-21'),
(14, 59, 'MEDICAL', '2025-12-05'), (15, 59, 'PRESENT', '2025-12-05'), (35, 59, 'PRESENT', '2025-12-05'),
(36, 59, 'PRESENT', '2025-12-05'), (37, 59, 'PRESENT', '2025-12-05'), (38, 59, 'PRESENT', '2025-12-05'),
(14, 60, 'PRESENT', '2025-12-19'), (15, 60, 'PRESENT', '2025-12-19'), (35, 60, 'PRESENT', '2025-12-19'),
(36, 60, 'PRESENT', '2025-12-19'), (37, 60, 'PRESENT', '2025-12-19'), (38, 60, 'PRESENT', '2025-12-19'),
(14, 61, 'PRESENT', '2025-10-06'), (15, 61, 'PRESENT', '2025-10-06'), (35, 61, 'LATE', '2025-10-06'),
(36, 61, 'PRESENT', '2025-10-06'), (37, 61, 'PRESENT', '2025-10-06'), (38, 61, 'PRESENT', '2025-10-06'),
(14, 62, 'PRESENT', '2025-10-20'), (15, 62, 'PRESENT', '2025-10-20'), (35, 62, 'PRESENT', '2025-10-20'),
(36, 62, 'PRESENT', '2025-10-20'), (37, 62, 'PRESENT', '2025-10-20'), (38, 62, 'PRESENT', '2025-10-20'),
(14, 63, 'MEDICAL', '2025-11-03'), (15, 63, 'PRESENT', '2025-11-03'), (35, 63, 'PRESENT', '2025-11-03'),
(36, 63, 'PRESENT', '2025-11-03'), (37, 63, 'PRESENT', '2025-11-03'), (38, 63, 'PRESENT', '2025-11-03'),
(14, 64, 'PRESENT', '2025-11-17'), (15, 64, 'PRESENT', '2025-11-17'), (35, 64, 'PRESENT', '2025-11-17'),
(36, 64, 'PRESENT', '2025-11-17'), (37, 64, 'PRESENT', '2025-11-17'), (38, 64, 'LATE', '2025-11-17'),
(14, 65, 'PRESENT', '2025-12-01'), (15, 65, 'PRESENT', '2025-12-01'), (35, 65, 'PRESENT', '2025-12-01'),
(36, 65, 'PRESENT', '2025-12-01'), (37, 65, 'PRESENT', '2025-12-01'), (38, 65, 'PRESENT', '2025-12-01'),
(14, 66, 'PRESENT', '2025-12-15'), (15, 66, 'PRESENT', '2025-12-15'), (35, 66, 'PRESENT', '2025-12-15'),
(36, 66, 'PRESENT', '2025-12-15'), (37, 66, 'PRESENT', '2025-12-15'), (38, 66, 'PRESENT', '2025-12-15'),
(39, 67, 'PRESENT', '2025-10-07'), (40, 67, 'PRESENT', '2025-10-07'), (41, 67, 'PRESENT', '2025-10-07'),
(42, 67, 'PRESENT', '2025-10-07'), (43, 67, 'PRESENT', '2025-10-07'), (44, 67, 'PRESENT', '2025-10-07'),
(39, 68, 'LATE', '2025-10-21'), (40, 68, 'PRESENT', '2025-10-21'), (41, 68, 'PRESENT', '2025-10-21'),
(42, 68, 'PRESENT', '2025-10-21'), (43, 68, 'PRESENT', '2025-10-21'), (44, 68, 'PRESENT', '2025-10-21'),
(39, 69, 'ABSENT', '2025-11-06'), (40, 69, 'PRESENT', '2025-11-06'), (41, 69, 'PRESENT', '2025-11-06'),
(42, 69, 'PRESENT', '2025-11-06'), (43, 69, 'ABSENT', '2025-11-06'), (44, 69, 'PRESENT', '2025-11-06'),
(39, 70, 'PRESENT', '2025-11-18'), (40, 70, 'PRESENT', '2025-11-18'), (41, 70, 'PRESENT', '2025-11-18'),
(42, 70, 'PRESENT', '2025-11-18'), (43, 70, 'PRESENT', '2025-11-18'), (44, 70, 'PRESENT', '2025-11-18'),
(39, 71, 'PRESENT', '2025-12-02'), (40, 71, 'PRESENT', '2025-12-02'), (41, 71, 'MEDICAL', '2025-12-02'),
(42, 71, 'PRESENT', '2025-12-02'), (43, 71, 'PRESENT', '2025-12-02'), (44, 71, 'PRESENT', '2025-12-02'),
(39, 72, 'PRESENT', '2025-12-18'), (40, 72, 'PRESENT', '2025-12-18'), (41, 72, 'PRESENT', '2025-12-18'),
(42, 72, 'PRESENT', '2025-12-18'), (43, 72, 'PRESENT', '2025-12-18'), (44, 72, 'ABSENT', '2025-12-18'),
(39, 73, 'PRESENT', '2025-10-08'), (40, 73, 'LATE', '2025-10-08'), (41, 73, 'LATE', '2025-10-08'),
(42, 73, 'PRESENT', '2025-10-08'), (43, 73, 'PRESENT', '2025-10-08'), (44, 73, 'PRESENT', '2025-10-08'),
(39, 74, 'PRESENT', '2025-10-22'), (40, 74, 'PRESENT', '2025-10-22'), (41, 74, 'PRESENT', '2025-10-22'),
(42, 74, 'PRESENT', '2025-10-22'), (43, 74, 'PRESENT', '2025-10-22'), (44, 74, 'PRESENT', '2025-10-22'),
(39, 75, 'PRESENT', '2025-11-05'), (40, 75, 'PRESENT', '2025-11-05'), (41, 75, 'PRESENT', '2025-11-05'),
(42, 75, 'PRESENT', '2025-11-05'), (43, 75, 'PRESENT', '2025-11-05'), (44, 75, 'PRESENT', '2025-11-05'),
(39, 76, 'PRESENT', '2025-11-19'), (40, 76, 'PRESENT', '2025-11-19'), (41, 76, 'PRESENT', '2025-11-19'),
(42, 76, 'PRESENT', '2025-11-19'), (43, 76, 'PRESENT', '2025-11-19'), (44, 76, 'PRESENT', '2025-11-19'),
(39, 77, 'PRESENT', '2025-12-03'), (40, 77, 'PRESENT', '2025-12-03'), (41, 77, 'PRESENT', '2025-12-03'),
(42, 77, 'PRESENT', '2025-12-03'), (43, 77, 'PRESENT', '2025-12-03'), (44, 77, 'LATE', '2025-12-03'),
(39, 78, 'PRESENT', '2025-12-17'), (40, 78, 'PRESENT', '2025-12-17'), (41, 78, 'PRESENT', '2025-12-17'),
(42, 78, 'PRESENT', '2025-12-17'), (43, 78, 'PRESENT', '2025-12-17'), (44, 78, 'PRESENT', '2025-12-17'),
(9, 79, 'PRESENT', '2026-03-02'), (10, 79, 'PRESENT', '2026-03-02'), (11, 79, 'PRESENT', '2026-03-02'),
(12, 79, 'PRESENT', '2026-03-02'), (13, 79, 'PRESENT', '2026-03-02'), (25, 79, 'PRESENT', '2026-03-02');

INSERT INTO attend (student_id, session_id, attend_status, recorded_date) VALUES
(31, 79, 'PRESENT', '2026-03-02'), (32, 79, 'PRESENT', '2026-03-02'), (33, 79, 'PRESENT', '2026-03-02'),
(34, 79, 'PRESENT', '2026-03-02'), (9, 80, 'ABSENT', '2026-03-16'), (10, 80, 'PRESENT', '2026-03-16'),
(11, 80, 'PRESENT', '2026-03-16'), (12, 80, 'PRESENT', '2026-03-16'), (13, 80, 'ABSENT', '2026-03-16'),
(25, 80, 'PRESENT', '2026-03-16'), (31, 80, 'PRESENT', '2026-03-16'), (32, 80, 'PRESENT', '2026-03-16'),
(33, 80, 'LATE', '2026-03-16'), (34, 80, 'PRESENT', '2026-03-16'), (9, 81, 'PRESENT', '2026-04-01'),
(10, 81, 'PRESENT', '2026-04-01'), (11, 81, 'PRESENT', '2026-04-01'), (12, 81, 'PRESENT', '2026-04-01'),
(13, 81, 'PRESENT', '2026-04-01'), (25, 81, 'PRESENT', '2026-04-01'), (31, 81, 'LATE', '2026-04-01'),
(32, 81, 'PRESENT', '2026-04-01'), (33, 81, 'PRESENT', '2026-04-01'), (34, 81, 'PRESENT', '2026-04-01'),
(9, 82, 'PRESENT', '2026-04-13'), (10, 82, 'PRESENT', '2026-04-13'), (11, 82, 'PRESENT', '2026-04-13'),
(12, 82, 'PRESENT', '2026-04-13'), (13, 82, 'PRESENT', '2026-04-13'), (25, 82, 'PRESENT', '2026-04-13'),
(31, 82, 'PRESENT', '2026-04-13'), (32, 82, 'PRESENT', '2026-04-13'), (33, 82, 'LATE', '2026-04-13'),
(34, 82, 'PRESENT', '2026-04-13'), (9, 83, 'PRESENT', '2026-04-27'), (10, 83, 'PRESENT', '2026-04-27'),
(11, 83, 'PRESENT', '2026-04-27'), (12, 83, 'PRESENT', '2026-04-27'), (13, 83, 'PRESENT', '2026-04-27'),
(25, 83, 'PRESENT', '2026-04-27'), (31, 83, 'PRESENT', '2026-04-27'), (32, 83, 'PRESENT', '2026-04-27'),
(33, 83, 'PRESENT', '2026-04-27'), (34, 83, 'ABSENT', '2026-04-27'), (9, 84, 'PRESENT', '2026-05-13'),
(10, 84, 'ABSENT', '2026-05-13'), (11, 84, 'PRESENT', '2026-05-13'), (12, 84, 'PRESENT', '2026-05-13'),
(13, 84, 'PRESENT', '2026-05-13'), (25, 84, 'PRESENT', '2026-05-13'), (31, 84, 'PRESENT', '2026-05-13'),
(32, 84, 'PRESENT', '2026-05-13'), (33, 84, 'PRESENT', '2026-05-13'), (34, 84, 'PRESENT', '2026-05-13'),
(9, 85, 'PRESENT', '2026-03-03'), (10, 85, 'PRESENT', '2026-03-03'), (11, 85, 'ABSENT', '2026-03-03'),
(12, 85, 'PRESENT', '2026-03-03'), (13, 85, 'PRESENT', '2026-03-03'), (31, 85, 'PRESENT', '2026-03-03'),
(32, 85, 'PRESENT', '2026-03-03'), (33, 85, 'PRESENT', '2026-03-03'), (34, 85, 'PRESENT', '2026-03-03'),
(9, 86, 'PRESENT', '2026-03-17'), (10, 86, 'LATE', '2026-03-17'), (11, 86, 'PRESENT', '2026-03-17'),
(12, 86, 'PRESENT', '2026-03-17'), (13, 86, 'PRESENT', '2026-03-17'), (31, 86, 'PRESENT', '2026-03-17'),
(32, 86, 'PRESENT', '2026-03-17'), (33, 86, 'PRESENT', '2026-03-17'), (34, 86, 'PRESENT', '2026-03-17'),
(9, 87, 'PRESENT', '2026-04-02'), (10, 87, 'ABSENT', '2026-04-02'), (11, 87, 'PRESENT', '2026-04-02'),
(12, 87, 'PRESENT', '2026-04-02'), (13, 87, 'PRESENT', '2026-04-02'), (31, 87, 'PRESENT', '2026-04-02'),
(32, 87, 'PRESENT', '2026-04-02'), (33, 87, 'PRESENT', '2026-04-02'), (34, 87, 'PRESENT', '2026-04-02'),
(9, 88, 'PRESENT', '2026-04-14'), (10, 88, 'PRESENT', '2026-04-14'), (11, 88, 'ABSENT', '2026-04-14'),
(12, 88, 'PRESENT', '2026-04-14'), (13, 88, 'PRESENT', '2026-04-14'), (31, 88, 'PRESENT', '2026-04-14'),
(32, 88, 'PRESENT', '2026-04-14'), (33, 88, 'PRESENT', '2026-04-14'), (34, 88, 'PRESENT', '2026-04-14'),
(9, 89, 'PRESENT', '2026-04-28'), (10, 89, 'PRESENT', '2026-04-28'), (11, 89, 'PRESENT', '2026-04-28'),
(12, 89, 'LATE', '2026-04-28'), (13, 89, 'PRESENT', '2026-04-28'), (31, 89, 'PRESENT', '2026-04-28'),
(32, 89, 'PRESENT', '2026-04-28'), (33, 89, 'PRESENT', '2026-04-28'), (34, 89, 'PRESENT', '2026-04-28'),
(9, 90, 'PRESENT', '2026-05-14'), (10, 90, 'PRESENT', '2026-05-14'), (11, 90, 'ABSENT', '2026-05-14'),
(12, 90, 'LATE', '2026-05-14'), (13, 90, 'PRESENT', '2026-05-14'), (31, 90, 'LATE', '2026-05-14'),
(32, 90, 'LATE', '2026-05-14'), (33, 90, 'LATE', '2026-05-14'), (34, 90, 'PRESENT', '2026-05-14'),
(9, 91, 'LATE', '2026-03-04'), (10, 91, 'PRESENT', '2026-03-04'), (11, 91, 'PRESENT', '2026-03-04'),
(12, 91, 'PRESENT', '2026-03-04'), (13, 91, 'PRESENT', '2026-03-04'), (31, 91, 'PRESENT', '2026-03-04'),
(32, 91, 'PRESENT', '2026-03-04'), (33, 91, 'PRESENT', '2026-03-04'), (34, 91, 'PRESENT', '2026-03-04'),
(9, 92, 'PRESENT', '2026-03-18'), (10, 92, 'PRESENT', '2026-03-18'), (11, 92, 'PRESENT', '2026-03-18'),
(12, 92, 'PRESENT', '2026-03-18'), (13, 92, 'PRESENT', '2026-03-18'), (31, 92, 'PRESENT', '2026-03-18'),
(32, 92, 'PRESENT', '2026-03-18'), (33, 92, 'PRESENT', '2026-03-18'), (34, 92, 'PRESENT', '2026-03-18'),
(9, 93, 'PRESENT', '2026-04-03'), (10, 93, 'PRESENT', '2026-04-03'), (11, 93, 'PRESENT', '2026-04-03'),
(12, 93, 'LATE', '2026-04-03'), (13, 93, 'PRESENT', '2026-04-03'), (31, 93, 'PRESENT', '2026-04-03'),
(32, 93, 'PRESENT', '2026-04-03'), (33, 93, 'PRESENT', '2026-04-03'), (34, 93, 'MEDICAL', '2026-04-03'),
(9, 94, 'PRESENT', '2026-04-15'), (10, 94, 'PRESENT', '2026-04-15'), (11, 94, 'PRESENT', '2026-04-15'),
(12, 94, 'PRESENT', '2026-04-15'), (13, 94, 'PRESENT', '2026-04-15'), (31, 94, 'PRESENT', '2026-04-15'),
(32, 94, 'PRESENT', '2026-04-15'), (33, 94, 'PRESENT', '2026-04-15'), (34, 94, 'PRESENT', '2026-04-15'),
(9, 95, 'ABSENT', '2026-04-29'), (10, 95, 'PRESENT', '2026-04-29'), (11, 95, 'LATE', '2026-04-29'),
(12, 95, 'PRESENT', '2026-04-29'), (13, 95, 'MEDICAL', '2026-04-29'), (31, 95, 'PRESENT', '2026-04-29');

INSERT INTO attend (student_id, session_id, attend_status, recorded_date) VALUES
(32, 95, 'PRESENT', '2026-04-29'), (33, 95, 'PRESENT', '2026-04-29'), (34, 95, 'PRESENT', '2026-04-29'),
(9, 96, 'PRESENT', '2026-05-15'), (10, 96, 'PRESENT', '2026-05-15'), (11, 96, 'PRESENT', '2026-05-15'),
(12, 96, 'PRESENT', '2026-05-15'), (13, 96, 'PRESENT', '2026-05-15'), (31, 96, 'PRESENT', '2026-05-15'),
(32, 96, 'PRESENT', '2026-05-15'), (33, 96, 'PRESENT', '2026-05-15'), (34, 96, 'PRESENT', '2026-05-15'),
(23, 97, 'PRESENT', '2026-03-05'), (24, 97, 'LATE', '2026-03-05'), (25, 97, 'PRESENT', '2026-03-05'),
(26, 97, 'PRESENT', '2026-03-05'), (27, 97, 'PRESENT', '2026-03-05'), (28, 97, 'PRESENT', '2026-03-05'),
(29, 97, 'PRESENT', '2026-03-05'), (30, 97, 'PRESENT', '2026-03-05'), (23, 98, 'PRESENT', '2026-03-19'),
(24, 98, 'PRESENT', '2026-03-19'), (25, 98, 'PRESENT', '2026-03-19'), (26, 98, 'PRESENT', '2026-03-19'),
(27, 98, 'PRESENT', '2026-03-19'), (28, 98, 'LATE', '2026-03-19'), (29, 98, 'PRESENT', '2026-03-19'),
(30, 98, 'PRESENT', '2026-03-19'), (23, 99, 'PRESENT', '2026-04-02'), (24, 99, 'PRESENT', '2026-04-02'),
(25, 99, 'PRESENT', '2026-04-02'), (26, 99, 'PRESENT', '2026-04-02'), (27, 99, 'PRESENT', '2026-04-02'),
(28, 99, 'PRESENT', '2026-04-02'), (29, 99, 'PRESENT', '2026-04-02'), (30, 99, 'PRESENT', '2026-04-02'),
(23, 100, 'ABSENT', '2026-04-16'), (24, 100, 'PRESENT', '2026-04-16'), (25, 100, 'PRESENT', '2026-04-16'),
(26, 100, 'PRESENT', '2026-04-16'), (27, 100, 'PRESENT', '2026-04-16'), (28, 100, 'LATE', '2026-04-16'),
(29, 100, 'PRESENT', '2026-04-16'), (30, 100, 'PRESENT', '2026-04-16'), (23, 101, 'PRESENT', '2026-04-30'),
(24, 101, 'ABSENT', '2026-04-30'), (25, 101, 'PRESENT', '2026-04-30'), (26, 101, 'PRESENT', '2026-04-30'),
(27, 101, 'PRESENT', '2026-04-30'), (28, 101, 'PRESENT', '2026-04-30'), (29, 101, 'ABSENT', '2026-04-30'),
(30, 101, 'PRESENT', '2026-04-30'), (23, 102, 'PRESENT', '2026-05-14'), (24, 102, 'PRESENT', '2026-05-14'),
(25, 102, 'PRESENT', '2026-05-14'), (26, 102, 'PRESENT', '2026-05-14'), (27, 102, 'PRESENT', '2026-05-14'),
(28, 102, 'PRESENT', '2026-05-14'), (29, 102, 'PRESENT', '2026-05-14'), (30, 102, 'PRESENT', '2026-05-14'),
(23, 103, 'PRESENT', '2026-03-06'), (24, 103, 'PRESENT', '2026-03-06'), (25, 103, 'PRESENT', '2026-03-06'),
(26, 103, 'PRESENT', '2026-03-06'), (27, 103, 'PRESENT', '2026-03-06'), (28, 103, 'LATE', '2026-03-06'),
(29, 103, 'PRESENT', '2026-03-06'), (30, 103, 'PRESENT', '2026-03-06'), (23, 104, 'PRESENT', '2026-03-20'),
(24, 104, 'PRESENT', '2026-03-20'), (25, 104, 'PRESENT', '2026-03-20'), (26, 104, 'PRESENT', '2026-03-20'),
(27, 104, 'PRESENT', '2026-03-20'), (28, 104, 'PRESENT', '2026-03-20'), (29, 104, 'PRESENT', '2026-03-20'),
(30, 104, 'PRESENT', '2026-03-20'), (23, 105, 'PRESENT', '2026-04-03'), (24, 105, 'PRESENT', '2026-04-03'),
(25, 105, 'PRESENT', '2026-04-03'), (26, 105, 'ABSENT', '2026-04-03'), (27, 105, 'PRESENT', '2026-04-03'),
(28, 105, 'PRESENT', '2026-04-03'), (29, 105, 'PRESENT', '2026-04-03'), (30, 105, 'PRESENT', '2026-04-03'),
(23, 106, 'PRESENT', '2026-04-17'), (24, 106, 'PRESENT', '2026-04-17'), (25, 106, 'PRESENT', '2026-04-17'),
(26, 106, 'PRESENT', '2026-04-17'), (27, 106, 'PRESENT', '2026-04-17'), (28, 106, 'PRESENT', '2026-04-17'),
(29, 106, 'PRESENT', '2026-04-17'), (30, 106, 'PRESENT', '2026-04-17'), (23, 107, 'PRESENT', '2026-05-01'),
(24, 107, 'PRESENT', '2026-05-01'), (25, 107, 'PRESENT', '2026-05-01'), (26, 107, 'PRESENT', '2026-05-01'),
(27, 107, 'PRESENT', '2026-05-01'), (28, 107, 'PRESENT', '2026-05-01'), (29, 107, 'ABSENT', '2026-05-01'),
(30, 107, 'PRESENT', '2026-05-01'), (23, 108, 'PRESENT', '2026-05-15'), (24, 108, 'PRESENT', '2026-05-15'),
(25, 108, 'PRESENT', '2026-05-15'), (26, 108, 'PRESENT', '2026-05-15'), (27, 108, 'PRESENT', '2026-05-15'),
(28, 108, 'PRESENT', '2026-05-15'), (29, 108, 'PRESENT', '2026-05-15'), (30, 108, 'PRESENT', '2026-05-15'),
(14, 109, 'PRESENT', '2026-03-02'), (15, 109, 'ABSENT', '2026-03-02'), (35, 109, 'ABSENT', '2026-03-02'),
(36, 109, 'PRESENT', '2026-03-02'), (37, 109, 'PRESENT', '2026-03-02'), (38, 109, 'PRESENT', '2026-03-02'),
(14, 110, 'PRESENT', '2026-03-16'), (15, 110, 'PRESENT', '2026-03-16'), (35, 110, 'PRESENT', '2026-03-16'),
(36, 110, 'PRESENT', '2026-03-16'), (37, 110, 'PRESENT', '2026-03-16'), (38, 110, 'PRESENT', '2026-03-16'),
(14, 111, 'PRESENT', '2026-04-01'), (15, 111, 'PRESENT', '2026-04-01'), (35, 111, 'PRESENT', '2026-04-01'),
(36, 111, 'PRESENT', '2026-04-01'), (37, 111, 'PRESENT', '2026-04-01'), (38, 111, 'PRESENT', '2026-04-01'),
(14, 112, 'PRESENT', '2026-04-13'), (15, 112, 'ABSENT', '2026-04-13'), (35, 112, 'PRESENT', '2026-04-13'),
(36, 112, 'PRESENT', '2026-04-13'), (37, 112, 'PRESENT', '2026-04-13'), (38, 112, 'PRESENT', '2026-04-13'),
(14, 113, 'PRESENT', '2026-04-27'), (15, 113, 'PRESENT', '2026-04-27'), (35, 113, 'PRESENT', '2026-04-27'),
(36, 113, 'PRESENT', '2026-04-27'), (37, 113, 'PRESENT', '2026-04-27'), (38, 113, 'PRESENT', '2026-04-27'),
(14, 114, 'PRESENT', '2026-05-13'), (15, 114, 'PRESENT', '2026-05-13'), (35, 114, 'PRESENT', '2026-05-13'),
(36, 114, 'LATE', '2026-05-13'), (37, 114, 'PRESENT', '2026-05-13'), (38, 114, 'PRESENT', '2026-05-13'),
(39, 115, 'PRESENT', '2026-03-03'), (40, 115, 'ABSENT', '2026-03-03'), (41, 115, 'PRESENT', '2026-03-03'),
(42, 115, 'PRESENT', '2026-03-03'), (43, 115, 'PRESENT', '2026-03-03'), (44, 115, 'PRESENT', '2026-03-03');

INSERT INTO attend (student_id, session_id, attend_status, recorded_date) VALUES
(39, 116, 'PRESENT', '2026-03-17'), (40, 116, 'PRESENT', '2026-03-17'), (41, 116, 'PRESENT', '2026-03-17'),
(42, 116, 'PRESENT', '2026-03-17'), (43, 116, 'PRESENT', '2026-03-17'), (44, 116, 'ABSENT', '2026-03-17'),
(39, 117, 'PRESENT', '2026-03-31'), (40, 117, 'PRESENT', '2026-03-31'), (41, 117, 'PRESENT', '2026-03-31'),
(42, 117, 'PRESENT', '2026-03-31'), (43, 117, 'LATE', '2026-03-31'), (44, 117, 'PRESENT', '2026-03-31'),
(39, 118, 'PRESENT', '2026-04-14'), (40, 118, 'PRESENT', '2026-04-14'), (41, 118, 'LATE', '2026-04-14'),
(42, 118, 'PRESENT', '2026-04-14'), (43, 118, 'PRESENT', '2026-04-14'), (44, 118, 'PRESENT', '2026-04-14'),
(39, 119, 'PRESENT', '2026-04-28'), (40, 119, 'PRESENT', '2026-04-28'), (41, 119, 'PRESENT', '2026-04-28'),
(42, 119, 'PRESENT', '2026-04-28'), (43, 119, 'PRESENT', '2026-04-28'), (44, 119, 'PRESENT', '2026-04-28'),
(39, 120, 'PRESENT', '2026-05-12'), (40, 120, 'ABSENT', '2026-05-12'), (41, 120, 'MEDICAL', '2026-05-12'),
(42, 120, 'PRESENT', '2026-05-12'), (43, 120, 'PRESENT', '2026-05-12'), (44, 120, 'PRESENT', '2026-05-12'),
(9, 121, 'PRESENT', '2026-10-05'), (10, 121, 'PRESENT', '2026-10-05'), (11, 121, 'PRESENT', '2026-10-05'),
(12, 121, 'PRESENT', '2026-10-05'), (13, 121, 'PRESENT', '2026-10-05'), (31, 121, 'PRESENT', '2026-10-05'),
(32, 121, 'LATE', '2026-10-05'), (33, 121, 'PRESENT', '2026-10-05'), (34, 121, 'PRESENT', '2026-10-05'),
(9, 122, 'PRESENT', '2026-10-12'), (10, 122, 'PRESENT', '2026-10-12'), (11, 122, 'LATE', '2026-10-12'),
(12, 122, 'PRESENT', '2026-10-12'), (13, 122, 'ABSENT', '2026-10-12'), (31, 122, 'PRESENT', '2026-10-12'),
(32, 122, 'PRESENT', '2026-10-12'), (33, 122, 'PRESENT', '2026-10-12'), (34, 122, 'PRESENT', '2026-10-12'),
(9, 123, 'PRESENT', '2026-10-21'), (10, 123, 'PRESENT', '2026-10-21'), (11, 123, 'PRESENT', '2026-10-21'),
(12, 123, 'PRESENT', '2026-10-21'), (13, 123, 'PRESENT', '2026-10-21'), (31, 123, 'PRESENT', '2026-10-21'),
(32, 123, 'PRESENT', '2026-10-21'), (33, 123, 'PRESENT', '2026-10-21'), (34, 123, 'PRESENT', '2026-10-21'),
(9, 124, 'PRESENT', '2026-10-26'), (10, 124, 'PRESENT', '2026-10-26'), (11, 124, 'ABSENT', '2026-10-26'),
(12, 124, 'PRESENT', '2026-10-26'), (13, 124, 'PRESENT', '2026-10-26'), (31, 124, 'PRESENT', '2026-10-26'),
(32, 124, 'PRESENT', '2026-10-26'), (33, 124, 'PRESENT', '2026-10-26'), (34, 124, 'PRESENT', '2026-10-26'),
(9, 125, 'ABSENT', '2026-10-06'), (10, 125, 'PRESENT', '2026-10-06'), (11, 125, 'PRESENT', '2026-10-06'),
(12, 125, 'PRESENT', '2026-10-06'), (13, 125, 'ABSENT', '2026-10-06'), (31, 125, 'PRESENT', '2026-10-06'),
(32, 125, 'PRESENT', '2026-10-06'), (33, 125, 'PRESENT', '2026-10-06'), (34, 125, 'PRESENT', '2026-10-06'),
(9, 126, 'PRESENT', '2026-10-13'), (10, 126, 'PRESENT', '2026-10-13'), (11, 126, 'PRESENT', '2026-10-13'),
(12, 126, 'PRESENT', '2026-10-13'), (13, 126, 'LATE', '2026-10-13'), (31, 126, 'PRESENT', '2026-10-13'),
(32, 126, 'PRESENT', '2026-10-13'), (33, 126, 'PRESENT', '2026-10-13'), (34, 126, 'LATE', '2026-10-13'),
(9, 127, 'PRESENT', '2026-10-22'), (10, 127, 'PRESENT', '2026-10-22'), (11, 127, 'PRESENT', '2026-10-22'),
(12, 127, 'PRESENT', '2026-10-22'), (13, 127, 'PRESENT', '2026-10-22'), (31, 127, 'PRESENT', '2026-10-22'),
(32, 127, 'PRESENT', '2026-10-22'), (33, 127, 'PRESENT', '2026-10-22'), (34, 127, 'PRESENT', '2026-10-22'),
(9, 128, 'PRESENT', '2026-10-27'), (10, 128, 'PRESENT', '2026-10-27'), (11, 128, 'PRESENT', '2026-10-27'),
(12, 128, 'PRESENT', '2026-10-27'), (13, 128, 'PRESENT', '2026-10-27'), (31, 128, 'PRESENT', '2026-10-27'),
(32, 128, 'PRESENT', '2026-10-27'), (33, 128, 'PRESENT', '2026-10-27'), (34, 128, 'PRESENT', '2026-10-27'),
(23, 129, 'PRESENT', '2026-10-07'), (24, 129, 'PRESENT', '2026-10-07'), (25, 129, 'PRESENT', '2026-10-07'),
(26, 129, 'PRESENT', '2026-10-07'), (27, 129, 'PRESENT', '2026-10-07'), (28, 129, 'PRESENT', '2026-10-07'),
(29, 129, 'ABSENT', '2026-10-07'), (30, 129, 'LATE', '2026-10-07'), (23, 130, 'PRESENT', '2026-10-14'),
(24, 130, 'PRESENT', '2026-10-14'), (25, 130, 'PRESENT', '2026-10-14'), (26, 130, 'PRESENT', '2026-10-14'),
(27, 130, 'ABSENT', '2026-10-14'), (28, 130, 'PRESENT', '2026-10-14'), (29, 130, 'PRESENT', '2026-10-14'),
(30, 130, 'PRESENT', '2026-10-14'), (23, 131, 'PRESENT', '2026-10-23'), (24, 131, 'PRESENT', '2026-10-23'),
(25, 131, 'PRESENT', '2026-10-23'), (26, 131, 'LATE', '2026-10-23'), (27, 131, 'PRESENT', '2026-10-23'),
(28, 131, 'PRESENT', '2026-10-23'), (29, 131, 'PRESENT', '2026-10-23'), (30, 131, 'PRESENT', '2026-10-23'),
(23, 132, 'PRESENT', '2026-10-28'), (24, 132, 'PRESENT', '2026-10-28'), (25, 132, 'PRESENT', '2026-10-28'),
(26, 132, 'PRESENT', '2026-10-28'), (27, 132, 'PRESENT', '2026-10-28'), (28, 132, 'PRESENT', '2026-10-28'),
(29, 132, 'PRESENT', '2026-10-28'), (30, 132, 'PRESENT', '2026-10-28'), (23, 133, 'PRESENT', '2026-10-08'),
(24, 133, 'PRESENT', '2026-10-08'), (25, 133, 'PRESENT', '2026-10-08'), (26, 133, 'PRESENT', '2026-10-08'),
(27, 133, 'LATE', '2026-10-08'), (28, 133, 'LATE', '2026-10-08'), (29, 133, 'PRESENT', '2026-10-08'),
(30, 133, 'PRESENT', '2026-10-08'), (23, 134, 'PRESENT', '2026-10-15'), (24, 134, 'PRESENT', '2026-10-15'),
(25, 134, 'PRESENT', '2026-10-15'), (26, 134, 'PRESENT', '2026-10-15'), (27, 134, 'PRESENT', '2026-10-15'),
(28, 134, 'PRESENT', '2026-10-15'), (29, 134, 'ABSENT', '2026-10-15'), (30, 134, 'PRESENT', '2026-10-15');

INSERT INTO attend (student_id, session_id, attend_status, recorded_date) VALUES
(23, 135, 'PRESENT', '2026-10-22'), (24, 135, 'PRESENT', '2026-10-22'), (25, 135, 'PRESENT', '2026-10-22'),
(26, 135, 'PRESENT', '2026-10-22'), (27, 135, 'ABSENT', '2026-10-22'), (28, 135, 'PRESENT', '2026-10-22'),
(29, 135, 'PRESENT', '2026-10-22'), (30, 135, 'LATE', '2026-10-22'), (23, 136, 'PRESENT', '2026-10-29'),
(24, 136, 'PRESENT', '2026-10-29'), (25, 136, 'PRESENT', '2026-10-29'), (26, 136, 'PRESENT', '2026-10-29'),
(27, 136, 'PRESENT', '2026-10-29'), (28, 136, 'LATE', '2026-10-29'), (29, 136, 'LATE', '2026-10-29'),
(30, 136, 'PRESENT', '2026-10-29'), (14, 137, 'PRESENT', '2026-10-09'), (15, 137, 'PRESENT', '2026-10-09'),
(35, 137, 'PRESENT', '2026-10-09'), (36, 137, 'PRESENT', '2026-10-09'), (37, 137, 'PRESENT', '2026-10-09'),
(38, 137, 'PRESENT', '2026-10-09'), (14, 138, 'PRESENT', '2026-10-16'), (15, 138, 'PRESENT', '2026-10-16'),
(35, 138, 'PRESENT', '2026-10-16'), (36, 138, 'PRESENT', '2026-10-16'), (37, 138, 'PRESENT', '2026-10-16'),
(38, 138, 'PRESENT', '2026-10-16'), (14, 139, 'PRESENT', '2026-10-23'), (15, 139, 'PRESENT', '2026-10-23'),
(35, 139, 'PRESENT', '2026-10-23'), (36, 139, 'PRESENT', '2026-10-23'), (37, 139, 'LATE', '2026-10-23'),
(38, 139, 'PRESENT', '2026-10-23'), (14, 140, 'PRESENT', '2026-10-30'), (15, 140, 'PRESENT', '2026-10-30'),
(35, 140, 'PRESENT', '2026-10-30'), (36, 140, 'PRESENT', '2026-10-30'), (37, 140, 'PRESENT', '2026-10-30'),
(38, 140, 'PRESENT', '2026-10-30'), (39, 141, 'PRESENT', '2026-10-05'), (40, 141, 'PRESENT', '2026-10-05'),
(41, 141, 'ABSENT', '2026-10-05'), (42, 141, 'PRESENT', '2026-10-05'), (43, 141, 'PRESENT', '2026-10-05'),
(44, 141, 'PRESENT', '2026-10-05'), (39, 142, 'PRESENT', '2026-10-12'), (40, 142, 'PRESENT', '2026-10-12'),
(41, 142, 'PRESENT', '2026-10-12'), (42, 142, 'PRESENT', '2026-10-12'), (43, 142, 'PRESENT', '2026-10-12'),
(44, 142, 'PRESENT', '2026-10-12'), (39, 143, 'PRESENT', '2026-10-19'), (40, 143, 'PRESENT', '2026-10-19'),
(41, 143, 'PRESENT', '2026-10-19'), (42, 143, 'PRESENT', '2026-10-19'), (43, 143, 'PRESENT', '2026-10-19'),
(44, 143, 'ABSENT', '2026-10-19'), (39, 144, 'PRESENT', '2026-10-26'), (40, 144, 'PRESENT', '2026-10-26'),
(41, 144, 'LATE', '2026-10-26'), (42, 144, 'PRESENT', '2026-10-26'), (43, 144, 'PRESENT', '2026-10-26'),
(44, 144, 'PRESENT', '2026-10-26'), (13, 145, 'PRESENT', '2026-10-06'), (13, 146, 'PRESENT', '2026-10-13'),
(13, 147, 'ABSENT', '2026-10-22'), (13, 148, 'PRESENT', '2026-10-27');

-- assessment

INSERT INTO assessment (assessment_id, offering_id, assessment_name, assessment_type, submitted_date, reference_number, assessment_date) VALUES
(1, 1, 'Quiz 1', 'QUIZ', '2024-11-07', 'ICT1113-QZ-01', '2024-10-31'),
(2, 1, 'Assignment 1', 'ASSIGNMENT', '2024-12-05', 'ICT1113-AS-01', '2024-11-28'),
(3, 1, 'Lab Test', 'PRACTICAL', '2024-12-26', 'ICT1113-LT-01', '2024-12-19'),
(4, 2, 'Quiz 1', 'QUIZ', '2024-11-07', 'ICT1123-QZ-01', '2024-10-31'),
(5, 2, 'Assignment 1', 'ASSIGNMENT', '2024-12-05', 'ICT1123-AS-01', '2024-11-28'),
(6, 2, 'Mid Exam', 'MID_EXAM', '2024-12-26', 'ICT1123-ME-01', '2024-12-19'),
(7, 3, 'Quiz 1', 'QUIZ', '2025-04-03', 'ICT1233-QZ-01', '2025-03-27'),
(8, 3, 'Assignment 1', 'ASSIGNMENT', '2025-05-01', 'ICT1233-AS-01', '2025-04-24'),
(9, 3, 'Lab Test', 'PRACTICAL', '2025-05-22', 'ICT1233-LT-01', '2025-05-15'),
(10, 4, 'Quiz 1', 'QUIZ', '2025-04-03', 'ICT1223-QZ-01', '2025-03-27'),
(11, 4, 'Assignment 1', 'ASSIGNMENT', '2025-05-01', 'ICT1223-AS-01', '2025-04-24'),
(12, 4, 'Mid Exam', 'MID_EXAM', '2025-05-22', 'ICT1223-ME-01', '2025-05-15'),
(13, 5, 'Quiz 1', 'QUIZ', '2025-04-03', 'ICT1213-QZ-01', '2025-03-27'),
(14, 5, 'Assignment 1', 'ASSIGNMENT', '2025-05-01', 'ICT1213-AS-01', '2025-04-24'),
(15, 5, 'Lab Test', 'PRACTICAL', '2025-05-22', 'ICT1213-LT-01', '2025-05-15'),
(16, 6, 'Quiz 1', 'QUIZ', '2025-11-06', 'ICT1113-QZ-01', '2025-10-30'),
(17, 6, 'Assignment 1', 'ASSIGNMENT', '2025-12-04', 'ICT1113-AS-01', '2025-11-27'),
(18, 6, 'Mid Exam', 'MID_EXAM', '2025-12-25', 'ICT1113-ME-01', '2025-12-18'),
(19, 7, 'Quiz 1', 'QUIZ', '2025-11-06', 'ICT1123-QZ-01', '2025-10-30'),
(20, 7, 'Assignment 1', 'ASSIGNMENT', '2025-12-04', 'ICT1123-AS-01', '2025-11-27'),
(21, 7, 'Mini Project', 'PROJECT', '2025-12-25', 'ICT1123-PR-01', '2025-12-18'),
(22, 8, 'Quiz 1', 'QUIZ', '2025-11-06', 'ICT2113-QZ-01', '2025-10-30'),
(23, 8, 'Assignment 1', 'ASSIGNMENT', '2025-12-04', 'ICT2113-AS-01', '2025-11-27'),
(24, 8, 'Mid Exam', 'MID_EXAM', '2025-12-25', 'ICT2113-ME-01', '2025-12-18'),
(25, 9, 'Quiz 1', 'QUIZ', '2025-11-06', 'ICT2123-QZ-01', '2025-10-30'),
(26, 9, 'Assignment 1', 'ASSIGNMENT', '2025-12-04', 'ICT2123-AS-01', '2025-11-27'),
(27, 9, 'Lab Test', 'PRACTICAL', '2025-12-25', 'ICT2123-LT-01', '2025-12-18'),
(28, 10, 'Quiz 1', 'QUIZ', '2025-11-06', 'ET1113-QZ-01', '2025-10-30'),
(29, 10, 'Assignment 1', 'ASSIGNMENT', '2025-12-04', 'ET1113-AS-01', '2025-11-27'),
(30, 10, 'Mid Exam', 'MID_EXAM', '2025-12-25', 'ET1113-ME-01', '2025-12-18'),
(31, 11, 'Quiz 1', 'QUIZ', '2025-11-06', 'ET1123-QZ-01', '2025-10-30'),
(32, 11, 'Assignment 1', 'ASSIGNMENT', '2025-12-04', 'ET1123-AS-01', '2025-11-27'),
(33, 11, 'Mini Project', 'PROJECT', '2025-12-25', 'ET1123-PR-01', '2025-12-18'),
(34, 12, 'Quiz 1', 'QUIZ', '2025-11-06', 'BST1113-QZ-01', '2025-10-30'),
(35, 12, 'Assignment 1', 'ASSIGNMENT', '2025-12-04', 'BST1113-AS-01', '2025-11-27'),
(36, 12, 'Mid Exam', 'MID_EXAM', '2025-12-25', 'BST1113-ME-01', '2025-12-18'),
(37, 13, 'Quiz 1', 'QUIZ', '2025-11-06', 'BST1123-QZ-01', '2025-10-30'),
(38, 13, 'Assignment 1', 'ASSIGNMENT', '2025-12-04', 'BST1123-AS-01', '2025-11-27'),
(39, 13, 'Mini Project', 'PROJECT', '2025-12-25', 'BST1123-PR-01', '2025-12-18'),
(40, 14, 'Quiz 1', 'QUIZ', '2026-04-02', 'ICT1233-QZ-01', '2026-03-26'),
(41, 14, 'Assignment 1', 'ASSIGNMENT', '2026-04-30', 'ICT1233-AS-01', '2026-04-23'),
(42, 14, 'Mid Exam', 'MID_EXAM', '2026-05-21', 'ICT1233-ME-01', '2026-05-14'),
(43, 15, 'Quiz 1', 'QUIZ', '2026-04-02', 'ICT1223-QZ-01', '2026-03-26'),
(44, 15, 'Assignment 1', 'ASSIGNMENT', '2026-04-30', 'ICT1223-AS-01', '2026-04-23'),
(45, 15, 'Lab Test', 'PRACTICAL', '2026-05-21', 'ICT1223-LT-01', '2026-05-14'),
(46, 16, 'Quiz 1', 'QUIZ', '2026-04-02', 'ICT1213-QZ-01', '2026-03-26'),
(47, 16, 'Assignment 1', 'ASSIGNMENT', '2026-04-30', 'ICT1213-AS-01', '2026-04-23'),
(48, 16, 'Mid Exam', 'MID_EXAM', '2026-05-21', 'ICT1213-ME-01', '2026-05-14'),
(49, 17, 'Quiz 1', 'QUIZ', '2026-04-02', 'ICT2213-QZ-01', '2026-03-26'),
(50, 17, 'Assignment 1', 'ASSIGNMENT', '2026-04-30', 'ICT2213-AS-01', '2026-04-23'),
(51, 17, 'Mini Project', 'PROJECT', '2026-05-21', 'ICT2213-PR-01', '2026-05-14'),
(52, 18, 'Quiz 1', 'QUIZ', '2026-04-02', 'ICT2223-QZ-01', '2026-03-26'),
(53, 18, 'Assignment 1', 'ASSIGNMENT', '2026-04-30', 'ICT2223-AS-01', '2026-04-23'),
(54, 18, 'Mid Exam', 'MID_EXAM', '2026-05-21', 'ICT2223-ME-01', '2026-05-14'),
(55, 19, 'Quiz 1', 'QUIZ', '2026-04-02', 'ET1213-QZ-01', '2026-03-26'),
(56, 19, 'Assignment 1', 'ASSIGNMENT', '2026-04-30', 'ET1213-AS-01', '2026-04-23'),
(57, 19, 'Lab Test', 'PRACTICAL', '2026-05-21', 'ET1213-LT-01', '2026-05-14'),
(58, 20, 'Quiz 1', 'QUIZ', '2026-04-02', 'BST1213-QZ-01', '2026-03-26'),
(59, 20, 'Assignment 1', 'ASSIGNMENT', '2026-04-30', 'BST1213-AS-01', '2026-04-23'),
(60, 20, 'Mid Exam', 'MID_EXAM', '2026-05-21', 'BST1213-ME-01', '2026-05-14'),
(61, 21, 'Quiz 1', 'QUIZ', '2026-10-29', 'ICT2113-QZ-01', '2026-10-22'),
(62, 21, 'Assignment 1', 'ASSIGNMENT', NULL, 'ICT2113-AS-01', '2026-11-19'),
(63, 21, 'Lab Test', 'PRACTICAL', NULL, 'ICT2113-LT-01', '2026-12-10'),
(64, 22, 'Quiz 1', 'QUIZ', '2026-10-29', 'ICT2123-QZ-01', '2026-10-22'),
(65, 22, 'Assignment 1', 'ASSIGNMENT', NULL, 'ICT2123-AS-01', '2026-11-19'),
(66, 22, 'Mid Exam', 'MID_EXAM', NULL, 'ICT2123-ME-01', '2026-12-10'),
(67, 23, 'Quiz 1', 'QUIZ', '2026-10-29', 'ICT3113-QZ-01', '2026-10-22'),
(68, 23, 'Assignment 1', 'ASSIGNMENT', NULL, 'ICT3113-AS-01', '2026-11-19'),
(69, 23, 'Lab Test', 'PRACTICAL', NULL, 'ICT3113-LT-01', '2026-12-10'),
(70, 24, 'Quiz 1', 'QUIZ', '2026-10-29', 'ICT3123-QZ-01', '2026-10-22'),
(71, 24, 'Assignment 1', 'ASSIGNMENT', NULL, 'ICT3123-AS-01', '2026-11-19'),
(72, 24, 'Mid Exam', 'MID_EXAM', NULL, 'ICT3123-ME-01', '2026-12-10'),
(73, 25, 'Quiz 1', 'QUIZ', '2026-10-29', 'ET2113-QZ-01', '2026-10-22'),
(74, 25, 'Assignment 1', 'ASSIGNMENT', NULL, 'ET2113-AS-01', '2026-11-19'),
(75, 25, 'Mini Project', 'PROJECT', NULL, 'ET2113-PR-01', '2026-12-10'),
(76, 26, 'Quiz 1', 'QUIZ', '2026-10-29', 'BST2113-QZ-01', '2026-10-22'),
(77, 26, 'Assignment 1', 'ASSIGNMENT', NULL, 'BST2113-AS-01', '2026-11-19'),
(78, 26, 'Mid Exam', 'MID_EXAM', NULL, 'BST2113-ME-01', '2026-12-10'),
(79, 27, 'Quiz 1', 'QUIZ', '2026-10-29', 'ICT1113-QZ-01', '2026-10-22'),
(80, 27, 'Assignment 1', 'ASSIGNMENT', NULL, 'ICT1113-AS-01', '2026-11-19'),
(81, 27, 'Lab Test', 'PRACTICAL', NULL, 'ICT1113-LT-01', '2026-12-10');

-- enrollment

INSERT INTO enrollment (student_id, offering_id, enrollment_date, enrollment_status) VALUES
(23, 1, '2024-10-04', 'COMPLETED'), (24, 1, '2024-10-04', 'COMPLETED'),
(25, 1, '2024-10-04', 'COMPLETED'), (26, 1, '2024-10-04', 'COMPLETED'),
(27, 1, '2024-10-04', 'COMPLETED'), (28, 1, '2024-10-04', 'COMPLETED'),
(29, 1, '2024-10-04', 'COMPLETED'), (30, 1, '2024-10-04', 'COMPLETED'),
(23, 2, '2024-10-04', 'COMPLETED'), (24, 2, '2024-10-04', 'COMPLETED'),
(25, 2, '2024-10-04', 'COMPLETED'), (26, 2, '2024-10-04', 'COMPLETED'),
(27, 2, '2024-10-04', 'COMPLETED'), (28, 2, '2024-10-04', 'COMPLETED'),
(29, 2, '2024-10-04', 'COMPLETED'), (30, 2, '2024-10-04', 'COMPLETED'),
(23, 3, '2025-02-28', 'COMPLETED'), (24, 3, '2025-02-28', 'COMPLETED'),
(25, 3, '2025-02-28', 'COMPLETED'), (26, 3, '2025-02-28', 'COMPLETED'),
(27, 3, '2025-02-28', 'COMPLETED'), (28, 3, '2025-02-28', 'COMPLETED'),
(29, 3, '2025-02-28', 'COMPLETED'), (30, 3, '2025-02-28', 'COMPLETED'),
(23, 4, '2025-02-28', 'COMPLETED'), (24, 4, '2025-02-28', 'COMPLETED'),
(25, 4, '2025-02-28', 'COMPLETED'), (26, 4, '2025-02-28', 'COMPLETED'),
(27, 4, '2025-02-28', 'COMPLETED'), (28, 4, '2025-02-28', 'COMPLETED'),
(29, 4, '2025-02-28', 'COMPLETED'), (30, 4, '2025-02-28', 'COMPLETED'),
(23, 5, '2025-02-28', 'COMPLETED'), (24, 5, '2025-02-28', 'COMPLETED'),
(25, 5, '2025-02-28', 'COMPLETED'), (26, 5, '2025-02-28', 'COMPLETED'),
(27, 5, '2025-02-28', 'COMPLETED'), (28, 5, '2025-02-28', 'COMPLETED'),
(29, 5, '2025-02-28', 'COMPLETED'), (30, 5, '2025-02-28', 'COMPLETED'),
(9, 6, '2025-10-03', 'COMPLETED'), (10, 6, '2025-10-03', 'COMPLETED'),
(11, 6, '2025-10-03', 'COMPLETED'), (12, 6, '2025-10-03', 'COMPLETED'),
(13, 6, '2025-10-03', 'COMPLETED'), (31, 6, '2025-10-03', 'COMPLETED'),
(32, 6, '2025-10-03', 'COMPLETED'), (33, 6, '2025-10-03', 'COMPLETED'),
(34, 6, '2025-10-03', 'COMPLETED'), (9, 7, '2025-10-03', 'COMPLETED'),
(10, 7, '2025-10-03', 'COMPLETED'), (11, 7, '2025-10-03', 'COMPLETED'),
(12, 7, '2025-10-03', 'COMPLETED'), (13, 7, '2025-10-03', 'COMPLETED'),
(23, 7, '2025-10-03', 'COMPLETED'), (31, 7, '2025-10-03', 'COMPLETED'),
(32, 7, '2025-10-03', 'COMPLETED'), (33, 7, '2025-10-03', 'COMPLETED'),
(34, 7, '2025-10-03', 'COMPLETED'), (23, 8, '2025-10-03', 'COMPLETED'),
(24, 8, '2025-10-03', 'COMPLETED'), (25, 8, '2025-10-03', 'COMPLETED'),
(26, 8, '2025-10-03', 'COMPLETED'), (27, 8, '2025-10-03', 'COMPLETED'),
(28, 8, '2025-10-03', 'COMPLETED'), (29, 8, '2025-10-03', 'COMPLETED'),
(30, 8, '2025-10-03', 'COMPLETED'), (23, 9, '2025-10-03', 'COMPLETED'),
(24, 9, '2025-10-03', 'COMPLETED'), (25, 9, '2025-10-03', 'COMPLETED'),
(26, 9, '2025-10-03', 'COMPLETED'), (27, 9, '2025-10-03', 'COMPLETED'),
(28, 9, '2025-10-03', 'COMPLETED'), (29, 9, '2025-10-03', 'COMPLETED'),
(30, 9, '2025-10-03', 'COMPLETED'), (14, 10, '2025-10-03', 'COMPLETED'),
(15, 10, '2025-10-03', 'COMPLETED'), (35, 10, '2025-10-03', 'COMPLETED'),
(36, 10, '2025-10-03', 'COMPLETED'), (37, 10, '2025-10-03', 'COMPLETED'),
(38, 10, '2025-10-03', 'COMPLETED'), (14, 11, '2025-10-03', 'COMPLETED'),
(15, 11, '2025-10-03', 'COMPLETED'), (35, 11, '2025-10-03', 'COMPLETED'),
(36, 11, '2025-10-03', 'COMPLETED'), (37, 11, '2025-10-03', 'COMPLETED'),
(38, 11, '2025-10-03', 'COMPLETED'), (39, 12, '2025-10-03', 'COMPLETED'),
(40, 12, '2025-10-03', 'COMPLETED'), (41, 12, '2025-10-03', 'COMPLETED'),
(42, 12, '2025-10-03', 'COMPLETED'), (43, 12, '2025-10-03', 'COMPLETED'),
(44, 12, '2025-10-03', 'COMPLETED'), (39, 13, '2025-10-03', 'COMPLETED'),
(40, 13, '2025-10-03', 'COMPLETED'), (41, 13, '2025-10-03', 'COMPLETED'),
(42, 13, '2025-10-03', 'COMPLETED'), (43, 13, '2025-10-03', 'COMPLETED'),
(44, 13, '2025-10-03', 'COMPLETED'), (9, 14, '2026-02-27', 'COMPLETED'),
(10, 14, '2026-02-27', 'COMPLETED'), (11, 14, '2026-02-27', 'COMPLETED'),
(12, 14, '2026-02-27', 'COMPLETED'), (13, 14, '2026-02-27', 'COMPLETED'),
(25, 14, '2026-02-27', 'COMPLETED'), (31, 14, '2026-02-27', 'COMPLETED'),
(32, 14, '2026-02-27', 'COMPLETED'), (33, 14, '2026-02-27', 'COMPLETED'),
(34, 14, '2026-02-27', 'COMPLETED'), (9, 15, '2026-02-27', 'COMPLETED'),
(10, 15, '2026-02-27', 'COMPLETED'), (11, 15, '2026-02-27', 'COMPLETED'),
(12, 15, '2026-02-27', 'COMPLETED'), (13, 15, '2026-02-27', 'COMPLETED'),
(31, 15, '2026-02-27', 'COMPLETED'), (32, 15, '2026-02-27', 'COMPLETED'),
(33, 15, '2026-02-27', 'COMPLETED'), (34, 15, '2026-02-27', 'COMPLETED'),
(9, 16, '2026-02-27', 'COMPLETED'), (10, 16, '2026-02-27', 'COMPLETED'),
(11, 16, '2026-02-27', 'COMPLETED'), (12, 16, '2026-02-27', 'COMPLETED'),
(13, 16, '2026-02-27', 'COMPLETED'), (31, 16, '2026-02-27', 'COMPLETED'),
(32, 16, '2026-02-27', 'COMPLETED'), (33, 16, '2026-02-27', 'COMPLETED'),
(34, 16, '2026-02-27', 'COMPLETED'), (23, 17, '2026-02-27', 'COMPLETED'),
(24, 17, '2026-02-27', 'COMPLETED'), (25, 17, '2026-02-27', 'COMPLETED'),
(26, 17, '2026-02-27', 'COMPLETED'), (27, 17, '2026-02-27', 'COMPLETED'),
(28, 17, '2026-02-27', 'COMPLETED'), (29, 17, '2026-02-27', 'COMPLETED'),
(30, 17, '2026-02-27', 'COMPLETED'), (23, 18, '2026-02-27', 'COMPLETED'),
(24, 18, '2026-02-27', 'COMPLETED'), (25, 18, '2026-02-27', 'COMPLETED'),
(26, 18, '2026-02-27', 'COMPLETED'), (27, 18, '2026-02-27', 'COMPLETED'),
(28, 18, '2026-02-27', 'COMPLETED'), (29, 18, '2026-02-27', 'COMPLETED'),
(30, 18, '2026-02-27', 'COMPLETED'), (14, 19, '2026-02-27', 'COMPLETED'),
(15, 19, '2026-02-27', 'COMPLETED'), (35, 19, '2026-02-27', 'COMPLETED'),
(36, 19, '2026-02-27', 'COMPLETED'), (37, 19, '2026-02-27', 'COMPLETED'),
(38, 19, '2026-02-27', 'COMPLETED'), (39, 20, '2026-02-27', 'COMPLETED');

INSERT INTO enrollment (student_id, offering_id, enrollment_date, enrollment_status) VALUES
(40, 20, '2026-02-27', 'COMPLETED'), (41, 20, '2026-02-27', 'COMPLETED'),
(42, 20, '2026-02-27', 'COMPLETED'), (43, 20, '2026-02-27', 'COMPLETED'),
(44, 20, '2026-02-27', 'COMPLETED'), (9, 21, '2026-10-02', 'ENROLLED'),
(10, 21, '2026-10-02', 'ENROLLED'), (11, 21, '2026-10-02', 'ENROLLED'),
(12, 21, '2026-10-02', 'ENROLLED'), (13, 21, '2026-10-02', 'ENROLLED'),
(31, 21, '2026-10-02', 'ENROLLED'), (32, 21, '2026-10-02', 'ENROLLED'),
(33, 21, '2026-10-02', 'ENROLLED'), (34, 21, '2026-10-02', 'ENROLLED'),
(9, 22, '2026-10-02', 'ENROLLED'), (10, 22, '2026-10-02', 'ENROLLED'),
(11, 22, '2026-10-02', 'ENROLLED'), (12, 22, '2026-10-02', 'ENROLLED'),
(13, 22, '2026-10-02', 'ENROLLED'), (31, 22, '2026-10-02', 'ENROLLED'),
(32, 22, '2026-10-02', 'ENROLLED'), (33, 22, '2026-10-02', 'ENROLLED'),
(34, 22, '2026-10-02', 'ENROLLED'), (23, 23, '2026-10-02', 'ENROLLED'),
(24, 23, '2026-10-02', 'ENROLLED'), (25, 23, '2026-10-02', 'ENROLLED'),
(26, 23, '2026-10-02', 'ENROLLED'), (27, 23, '2026-10-02', 'ENROLLED'),
(28, 23, '2026-10-02', 'ENROLLED'), (29, 23, '2026-10-02', 'ENROLLED'),
(30, 23, '2026-10-02', 'ENROLLED'), (23, 24, '2026-10-02', 'ENROLLED'),
(24, 24, '2026-10-02', 'ENROLLED'), (25, 24, '2026-10-02', 'ENROLLED'),
(26, 24, '2026-10-02', 'ENROLLED'), (27, 24, '2026-10-02', 'ENROLLED'),
(28, 24, '2026-10-02', 'ENROLLED'), (29, 24, '2026-10-02', 'ENROLLED'),
(30, 24, '2026-10-02', 'ENROLLED'), (14, 25, '2026-10-02', 'ENROLLED'),
(15, 25, '2026-10-02', 'ENROLLED'), (35, 25, '2026-10-02', 'ENROLLED'),
(36, 25, '2026-10-02', 'ENROLLED'), (37, 25, '2026-10-02', 'ENROLLED'),
(38, 25, '2026-10-02', 'ENROLLED'), (39, 26, '2026-10-02', 'ENROLLED'),
(40, 26, '2026-10-02', 'ENROLLED'), (41, 26, '2026-10-02', 'ENROLLED'),
(42, 26, '2026-10-02', 'ENROLLED'), (43, 26, '2026-10-02', 'ENROLLED'),
(44, 26, '2026-10-02', 'ENROLLED'), (13, 27, '2026-10-02', 'ENROLLED');

-- eligibility

INSERT INTO eligibility (eligibility_id, student_id, offering_id, ca_average, attendance_percentage, eligibility_status) VALUES
(1, 23, 1, 77.50, 100.00, 'ELIGIBLE'), (2, 24, 1, 77.50, 100.00, 'ELIGIBLE'),
(3, 25, 1, 92.50, 100.00, 'ELIGIBLE'), (4, 26, 1, 70.00, 83.33, 'ELIGIBLE'),
(5, 27, 1, 76.25, 100.00, 'ELIGIBLE'), (6, 28, 1, 70.00, 100.00, 'ELIGIBLE'),
(7, 29, 1, 70.00, 100.00, 'ELIGIBLE'), (8, 30, 1, 71.25, 100.00, 'ELIGIBLE'),
(9, 23, 2, 37.50, 100.00, 'ELIGIBLE'), (10, 24, 2, 76.25, 100.00, 'ELIGIBLE'),
(11, 25, 2, 97.50, 100.00, 'ELIGIBLE'), (12, 26, 2, 72.50, 100.00, 'ELIGIBLE'),
(13, 27, 2, 83.75, 100.00, 'ELIGIBLE'), (14, 28, 2, 82.50, 83.33, 'ELIGIBLE'),
(15, 29, 2, 73.75, 83.33, 'ELIGIBLE'), (16, 30, 2, 71.25, 100.00, 'ELIGIBLE'),
(17, 23, 3, 75.00, 100.00, 'ELIGIBLE'), (18, 24, 3, 75.00, 100.00, 'ELIGIBLE'),
(19, 25, 3, 35.00, 83.33, 'ELIGIBLE'), (20, 26, 3, 77.50, 100.00, 'ELIGIBLE'),
(21, 27, 3, 81.25, 100.00, 'ELIGIBLE'), (22, 28, 3, 68.75, 100.00, 'ELIGIBLE'),
(23, 29, 3, 68.75, 66.67, 'NOT_ELIGIBLE'), (24, 30, 3, 73.75, 83.33, 'ELIGIBLE'),
(25, 23, 4, 72.50, 100.00, 'ELIGIBLE'), (26, 24, 4, 75.00, 100.00, 'ELIGIBLE'),
(27, 25, 4, 96.25, 100.00, 'ELIGIBLE'), (28, 26, 4, 67.50, 100.00, 'ELIGIBLE'),
(29, 27, 4, 70.00, 100.00, 'ELIGIBLE'), (30, 28, 4, 72.50, 83.33, 'ELIGIBLE'),
(31, 29, 4, 73.75, 66.67, 'NOT_ELIGIBLE'), (32, 30, 4, 78.75, 100.00, 'ELIGIBLE'),
(33, 23, 5, 73.75, 83.33, 'ELIGIBLE'), (34, 24, 5, 70.00, 100.00, 'ELIGIBLE'),
(35, 25, 5, 98.75, 66.67, 'NOT_ELIGIBLE'), (36, 26, 5, 71.25, 83.33, 'ELIGIBLE'),
(37, 27, 5, 71.25, 100.00, 'ELIGIBLE'), (38, 28, 5, 77.50, 100.00, 'ELIGIBLE'),
(39, 29, 5, 70.00, 83.33, 'ELIGIBLE'), (40, 30, 5, 83.75, 100.00, 'ELIGIBLE'),
(41, 9, 6, 78.75, 83.33, 'ELIGIBLE'), (42, 10, 6, 76.25, 100.00, 'ELIGIBLE'),
(43, 11, 6, 67.50, 100.00, 'ELIGIBLE'), (44, 12, 6, 53.75, 100.00, 'ELIGIBLE'),
(45, 13, 6, 35.00, 83.33, 'ELIGIBLE'), (46, 31, 6, 82.50, 100.00, 'ELIGIBLE'),
(47, 32, 6, 82.50, 100.00, 'ELIGIBLE'), (48, 33, 6, 50.00, 100.00, 'ELIGIBLE'),
(49, 34, 6, 75.00, 100.00, 'ELIGIBLE'), (50, 9, 7, 82.50, 100.00, 'ELIGIBLE'),
(51, 10, 7, 85.00, 83.33, 'ELIGIBLE'), (52, 11, 7, 61.25, 83.33, 'ELIGIBLE'),
(53, 12, 7, 47.50, 100.00, 'ELIGIBLE'), (54, 13, 7, 43.75, 83.33, 'ELIGIBLE'),
(55, 23, 7, 60.00, 100.00, 'ELIGIBLE'), (56, 31, 7, 76.25, 83.33, 'ELIGIBLE'),
(57, 32, 7, 82.50, 100.00, 'ELIGIBLE'), (58, 33, 7, 48.75, 100.00, 'ELIGIBLE'),
(59, 34, 7, 77.50, 100.00, 'ELIGIBLE'), (60, 23, 8, 66.25, 100.00, 'ELIGIBLE'),
(61, 24, 8, 75.00, 100.00, 'ELIGIBLE'), (62, 25, 8, 100.00, 100.00, 'ELIGIBLE'),
(63, 26, 8, 76.25, 100.00, 'ELIGIBLE'), (64, 27, 8, 87.50, 100.00, 'ELIGIBLE'),
(65, 28, 8, 78.75, 100.00, 'ELIGIBLE'), (66, 29, 8, 72.50, 100.00, 'ELIGIBLE'),
(67, 30, 8, 77.50, 100.00, 'ELIGIBLE'), (68, 23, 9, 81.25, 100.00, 'ELIGIBLE'),
(69, 24, 9, 63.75, 100.00, 'ELIGIBLE'), (70, 25, 9, 95.00, 83.33, 'ELIGIBLE'),
(71, 26, 9, 65.00, 100.00, 'ELIGIBLE'), (72, 27, 9, 76.25, 100.00, 'ELIGIBLE'),
(73, 28, 9, 72.50, 100.00, 'ELIGIBLE'), (74, 29, 9, 76.25, 83.33, 'ELIGIBLE'),
(75, 30, 9, 87.50, 100.00, 'ELIGIBLE'), (76, 14, 10, 77.50, 100.00, 'ELIGIBLE'),
(77, 15, 10, 55.00, 100.00, 'ELIGIBLE'), (78, 35, 10, 65.00, 100.00, 'ELIGIBLE'),
(79, 36, 10, 83.75, 100.00, 'ELIGIBLE'), (80, 37, 10, 76.25, 100.00, 'ELIGIBLE'),
(81, 38, 10, 81.25, 100.00, 'ELIGIBLE'), (82, 14, 11, 87.50, 100.00, 'ELIGIBLE'),
(83, 15, 11, 62.50, 100.00, 'ELIGIBLE'), (84, 35, 11, 65.00, 100.00, 'ELIGIBLE'),
(85, 36, 11, 76.25, 100.00, 'ELIGIBLE'), (86, 37, 11, 83.75, 100.00, 'ELIGIBLE'),
(87, 38, 11, 78.75, 100.00, 'ELIGIBLE'), (88, 39, 12, 81.25, 83.33, 'ELIGIBLE'),
(89, 40, 12, 62.50, 100.00, 'ELIGIBLE'), (90, 41, 12, 58.75, 100.00, 'ELIGIBLE'),
(91, 42, 12, 68.75, 100.00, 'ELIGIBLE'), (92, 43, 12, 73.75, 83.33, 'ELIGIBLE'),
(93, 44, 12, 77.50, 83.33, 'ELIGIBLE'), (94, 39, 13, 83.75, 100.00, 'ELIGIBLE'),
(95, 40, 13, 51.25, 100.00, 'ELIGIBLE'), (96, 41, 13, 55.00, 100.00, 'ELIGIBLE'),
(97, 42, 13, 78.75, 100.00, 'ELIGIBLE'), (98, 43, 13, 78.75, 100.00, 'ELIGIBLE'),
(99, 44, 13, 93.75, 100.00, 'ELIGIBLE'), (100, 9, 14, 91.25, 83.33, 'ELIGIBLE'),
(101, 10, 14, 81.25, 83.33, 'ELIGIBLE'), (102, 11, 14, 68.75, 100.00, 'ELIGIBLE'),
(103, 12, 14, 53.75, 100.00, 'ELIGIBLE'), (104, 13, 14, 45.00, 83.33, 'ELIGIBLE'),
(105, 25, 14, 55.00, 100.00, 'ELIGIBLE'), (106, 31, 14, 91.25, 100.00, 'ELIGIBLE'),
(107, 32, 14, 91.25, 100.00, 'ELIGIBLE'), (108, 33, 14, 51.25, 100.00, 'ELIGIBLE'),
(109, 34, 14, 76.25, 83.33, 'ELIGIBLE'), (110, 9, 15, 87.50, 100.00, 'ELIGIBLE'),
(111, 10, 15, 86.25, 83.33, 'ELIGIBLE'), (112, 11, 15, 71.25, 50.00, 'NOT_ELIGIBLE'),
(113, 12, 15, 65.00, 100.00, 'ELIGIBLE'), (114, 13, 15, 40.00, 100.00, 'ELIGIBLE'),
(115, 31, 15, 72.50, 100.00, 'ELIGIBLE'), (116, 32, 15, 91.25, 100.00, 'ELIGIBLE'),
(117, 33, 15, 46.25, 100.00, 'ELIGIBLE'), (118, 34, 15, 71.25, 100.00, 'ELIGIBLE'),
(119, 9, 16, 77.50, 83.33, 'ELIGIBLE'), (120, 10, 16, 67.50, 100.00, 'ELIGIBLE'),
(121, 11, 16, 58.75, 100.00, 'ELIGIBLE'), (122, 12, 16, 53.75, 100.00, 'ELIGIBLE'),
(123, 13, 16, 45.00, 100.00, 'ELIGIBLE'), (124, 31, 16, 77.50, 100.00, 'ELIGIBLE'),
(125, 32, 16, 85.00, 100.00, 'ELIGIBLE'), (126, 33, 16, 43.75, 100.00, 'ELIGIBLE'),
(127, 34, 16, 87.50, 100.00, 'ELIGIBLE'), (128, 23, 17, 68.75, 83.33, 'ELIGIBLE'),
(129, 24, 17, 73.75, 83.33, 'ELIGIBLE'), (130, 25, 17, 93.75, 100.00, 'ELIGIBLE'),
(131, 26, 17, 71.25, 100.00, 'ELIGIBLE'), (132, 27, 17, 77.50, 100.00, 'ELIGIBLE'),
(133, 28, 17, 65.00, 100.00, 'ELIGIBLE'), (134, 29, 17, 67.50, 83.33, 'ELIGIBLE'),
(135, 30, 17, 81.25, 100.00, 'ELIGIBLE'), (136, 23, 18, 80.00, 100.00, 'ELIGIBLE'),
(137, 24, 18, 67.50, 100.00, 'ELIGIBLE'), (138, 25, 18, 86.25, 100.00, 'ELIGIBLE'),
(139, 26, 18, 70.00, 83.33, 'ELIGIBLE'), (140, 27, 18, 78.75, 100.00, 'ELIGIBLE'),
(141, 28, 18, 81.25, 100.00, 'ELIGIBLE'), (142, 29, 18, 72.50, 83.33, 'ELIGIBLE'),
(143, 30, 18, 77.50, 100.00, 'ELIGIBLE'), (144, 14, 19, 73.75, 100.00, 'ELIGIBLE'),
(145, 15, 19, 57.50, 66.67, 'NOT_ELIGIBLE'), (146, 35, 19, 55.00, 83.33, 'ELIGIBLE'),
(147, 36, 19, 78.75, 100.00, 'ELIGIBLE'), (148, 37, 19, 73.75, 100.00, 'ELIGIBLE'),
(149, 38, 19, 83.75, 100.00, 'ELIGIBLE'), (150, 39, 20, 76.25, 100.00, 'ELIGIBLE');

INSERT INTO eligibility (eligibility_id, student_id, offering_id, ca_average, attendance_percentage, eligibility_status) VALUES
(151, 40, 20, 63.75, 66.67, 'NOT_ELIGIBLE'), (152, 41, 20, 65.00, 100.00, 'ELIGIBLE'),
(153, 42, 20, 71.25, 100.00, 'ELIGIBLE'), (154, 43, 20, 83.75, 100.00, 'ELIGIBLE'),
(155, 44, 20, 76.25, 83.33, 'ELIGIBLE'), (156, 9, 21, NULL, 100.00, 'PENDING'),
(157, 10, 21, NULL, 100.00, 'PENDING'), (158, 11, 21, NULL, 75.00, 'PENDING'),
(159, 12, 21, NULL, 100.00, 'PENDING'), (160, 13, 21, NULL, 75.00, 'PENDING'),
(161, 31, 21, NULL, 100.00, 'PENDING'), (162, 32, 21, NULL, 100.00, 'PENDING'),
(163, 33, 21, NULL, 100.00, 'PENDING'), (164, 34, 21, NULL, 100.00, 'PENDING'),
(165, 9, 22, NULL, 75.00, 'PENDING'), (166, 10, 22, NULL, 100.00, 'PENDING'),
(167, 11, 22, NULL, 100.00, 'PENDING'), (168, 12, 22, NULL, 100.00, 'PENDING'),
(169, 13, 22, NULL, 75.00, 'PENDING'), (170, 31, 22, NULL, 100.00, 'PENDING'),
(171, 32, 22, NULL, 100.00, 'PENDING'), (172, 33, 22, NULL, 100.00, 'PENDING'),
(173, 34, 22, NULL, 100.00, 'PENDING'), (174, 23, 23, NULL, 100.00, 'PENDING'),
(175, 24, 23, NULL, 100.00, 'PENDING'), (176, 25, 23, NULL, 100.00, 'PENDING'),
(177, 26, 23, NULL, 100.00, 'PENDING'), (178, 27, 23, NULL, 75.00, 'PENDING'),
(179, 28, 23, NULL, 100.00, 'PENDING'), (180, 29, 23, NULL, 75.00, 'PENDING'),
(181, 30, 23, NULL, 100.00, 'PENDING'), (182, 23, 24, NULL, 100.00, 'PENDING'),
(183, 24, 24, NULL, 100.00, 'PENDING'), (184, 25, 24, NULL, 100.00, 'PENDING'),
(185, 26, 24, NULL, 100.00, 'PENDING'), (186, 27, 24, NULL, 75.00, 'PENDING'),
(187, 28, 24, NULL, 100.00, 'PENDING'), (188, 29, 24, NULL, 75.00, 'PENDING'),
(189, 30, 24, NULL, 100.00, 'PENDING'), (190, 14, 25, NULL, 100.00, 'PENDING'),
(191, 15, 25, NULL, 100.00, 'PENDING'), (192, 35, 25, NULL, 100.00, 'PENDING'),
(193, 36, 25, NULL, 100.00, 'PENDING'), (194, 37, 25, NULL, 100.00, 'PENDING'),
(195, 38, 25, NULL, 100.00, 'PENDING'), (196, 39, 26, NULL, 100.00, 'PENDING'),
(197, 40, 26, NULL, 100.00, 'PENDING'), (198, 41, 26, NULL, 75.00, 'PENDING'),
(199, 42, 26, NULL, 100.00, 'PENDING'), (200, 43, 26, NULL, 100.00, 'PENDING'),
(201, 44, 26, NULL, 75.00, 'PENDING'), (202, 13, 27, NULL, 75.00, 'PENDING');

-- exam

INSERT INTO exam (exam_id, offering_id, type) VALUES
(1, 1, 'FINAL'), (2, 2, 'MID'), (3, 2, 'FINAL'),
(4, 3, 'FINAL'), (5, 4, 'MID'), (6, 4, 'FINAL'),
(7, 5, 'FINAL'), (8, 6, 'MID'), (9, 6, 'FINAL'),
(10, 7, 'FINAL'), (11, 8, 'MID'), (12, 8, 'FINAL'),
(13, 9, 'FINAL'), (14, 10, 'MID'), (15, 10, 'FINAL'),
(16, 11, 'FINAL'), (17, 12, 'MID'), (18, 12, 'FINAL'),
(19, 13, 'FINAL'), (20, 14, 'MID'), (21, 14, 'FINAL'),
(22, 15, 'FINAL'), (23, 16, 'MID'), (24, 16, 'FINAL'),
(25, 17, 'FINAL'), (26, 18, 'MID'), (27, 18, 'FINAL'),
(28, 19, 'FINAL'), (29, 20, 'MID'), (30, 20, 'FINAL');

-- result

INSERT INTO result (student_id, exam_id, ca_result, final_exam_mark, total_mark, sgpa, result_status, published_date, cgpa) VALUES
(23, 1, 31.00, 40.00, 71.00, 1.65, 'PASS', '2025-03-28', 1.65),
(24, 1, 31.00, 42.00, 73.00, 3.30, 'PASS', '2025-03-28', 3.30),
(25, 1, 37.00, 60.00, 97.00, 4.00, 'PASS', '2025-03-28', 4.00),
(26, 1, 28.00, 43.00, 71.00, 3.30, 'PASS', '2025-03-28', 3.30),
(27, 1, 30.50, 51.50, 82.00, 3.85, 'PASS', '2025-03-28', 3.85),
(28, 1, 28.00, 41.00, 69.00, 3.35, 'PASS', '2025-03-28', 3.35),
(29, 1, 28.00, 36.50, 64.50, 3.00, 'PASS', '2025-03-28', 3.00),
(30, 1, 28.50, 50.00, 78.50, 3.50, 'PASS', '2025-03-28', 3.50),
(23, 3, 15.00, 22.00, 37.00, 1.65, 'FAIL', '2025-03-28', 1.65),
(24, 3, 30.50, 44.00, 74.50, 3.30, 'PASS', '2025-03-28', 3.30),
(25, 3, 39.00, 60.00, 99.00, 4.00, 'PASS', '2025-03-28', 4.00),
(26, 3, 29.00, 43.50, 72.50, 3.30, 'PASS', '2025-03-28', 3.30),
(27, 3, 33.50, 42.50, 76.00, 3.85, 'PASS', '2025-03-28', 3.85),
(28, 3, 33.00, 46.50, 79.50, 3.35, 'PASS', '2025-03-28', 3.35),
(29, 3, 29.50, 40.50, 70.00, 3.00, 'PASS', '2025-03-28', 3.00),
(30, 3, 28.50, 45.00, 73.50, 3.50, 'PASS', '2025-03-28', 3.50),
(23, 4, 30.00, 45.00, 75.00, 3.70, 'PASS', '2025-09-05', 2.82),
(24, 4, 30.00, 46.50, 76.50, 3.45, 'PASS', '2025-09-05', 3.39),
(25, 4, 14.00, 20.00, 34.00, 2.00, 'FAIL', '2025-09-05', 3.00),
(26, 4, 31.00, 51.50, 82.50, 3.66, 'PASS', '2025-09-05', 3.51),
(27, 4, 32.50, 49.00, 81.50, 3.71, 'PASS', '2025-09-05', 3.77),
(28, 4, 27.50, 49.00, 76.50, 3.62, 'PASS', '2025-09-05', 3.51),
(30, 4, 29.50, 47.50, 77.00, 3.78, 'PASS', '2025-09-05', 3.66),
(23, 6, 29.00, 50.00, 79.00, 3.70, 'PASS', '2025-09-05', 2.82),
(24, 6, 30.00, 42.50, 72.50, 3.45, 'PASS', '2025-09-05', 3.39),
(25, 6, 38.50, 60.00, 98.50, 2.00, 'PASS', '2025-09-05', 3.00),
(26, 6, 27.00, 45.00, 72.00, 3.66, 'PASS', '2025-09-05', 3.51),
(27, 6, 28.00, 47.50, 75.50, 3.71, 'PASS', '2025-09-05', 3.77),
(28, 6, 29.00, 41.50, 70.50, 3.62, 'PASS', '2025-09-05', 3.51),
(30, 6, 31.50, 47.00, 78.50, 3.78, 'PASS', '2025-09-05', 3.66),
(23, 7, 29.50, 48.50, 78.00, 3.70, 'PASS', '2025-09-05', 2.82),
(24, 7, 28.00, 42.00, 70.00, 3.45, 'PASS', '2025-09-05', 3.39),
(26, 7, 28.50, 49.00, 77.50, 3.66, 'PASS', '2025-09-05', 3.51),
(27, 7, 28.50, 46.00, 74.50, 3.71, 'PASS', '2025-09-05', 3.77),
(28, 7, 31.00, 54.50, 85.50, 3.62, 'PASS', '2025-09-05', 3.51),
(29, 7, 28.00, 43.50, 71.50, 3.30, 'PASS', '2025-09-05', 3.08),
(30, 7, 33.50, 47.00, 80.50, 3.78, 'PASS', '2025-09-05', 3.66),
(9, 9, 31.50, 56.50, 88.00, 4.00, 'PASS', '2026-03-27', 4.00),
(10, 9, 30.50, 51.50, 82.00, 4.00, 'PASS', '2026-03-27', 4.00),
(11, 9, 27.00, 40.50, 67.50, 3.00, 'PASS', '2026-03-27', 3.00),
(12, 9, 21.50, 32.00, 53.50, 2.00, 'PASS', '2026-03-27', 2.00),
(13, 9, 14.00, 18.00, 32.00, 1.00, 'FAIL', '2026-03-27', 1.00),
(31, 9, 33.00, 51.00, 84.00, 4.00, 'PASS', '2026-03-27', 4.00),
(32, 9, 33.00, 51.50, 84.50, 3.65, 'PASS', '2026-03-27', 3.65),
(33, 9, 20.00, 27.00, 47.00, 1.70, 'PASS', '2026-03-27', 1.70),
(34, 9, 30.00, 47.00, 77.00, 3.85, 'PASS', '2026-03-27', 3.85),
(9, 10, 33.00, 57.50, 90.50, 4.00, 'PASS', '2026-03-27', 4.00),
(10, 10, 34.00, 58.00, 92.00, 4.00, 'PASS', '2026-03-27', 4.00),
(11, 10, 24.50, 41.50, 66.00, 3.00, 'PASS', '2026-03-27', 3.00),
(12, 10, 19.00, 34.50, 53.50, 2.00, 'PASS', '2026-03-27', 2.00),
(13, 10, 17.50, 33.00, 50.50, 1.00, 'PASS', '2026-03-27', 1.00),
(23, 10, 24.00, 28.00, 52.00, 3.23, 'PASS', '2026-03-27', 3.43),
(31, 10, 30.50, 54.00, 84.50, 4.00, 'PASS', '2026-03-27', 4.00),
(32, 10, 33.00, 41.00, 74.00, 3.65, 'PASS', '2026-03-27', 3.65),
(33, 10, 19.50, 26.00, 45.50, 1.70, 'PASS', '2026-03-27', 1.70),
(34, 10, 31.00, 49.50, 80.50, 3.85, 'PASS', '2026-03-27', 3.85),
(23, 12, 26.50, 49.50, 76.00, 3.23, 'PASS', '2026-03-27', 3.43),
(24, 12, 30.00, 48.50, 78.50, 3.50, 'PASS', '2026-03-27', 3.42),
(25, 12, 40.00, 59.50, 99.50, 4.00, 'PASS', '2026-03-27', 3.33),
(26, 12, 30.50, 43.50, 74.00, 3.00, 'PASS', '2026-03-27', 3.35),
(27, 12, 35.00, 50.00, 85.00, 4.00, 'PASS', '2026-03-27', 3.84),
(28, 12, 31.50, 39.50, 71.00, 3.50, 'PASS', '2026-03-27', 3.50),
(29, 12, 29.00, 44.50, 73.50, 3.50, 'PASS', '2026-03-27', 3.26),
(30, 12, 31.00, 45.00, 76.00, 3.85, 'PASS', '2026-03-27', 3.72),
(23, 13, 32.50, 51.50, 84.00, 3.23, 'PASS', '2026-03-27', 3.43),
(24, 13, 25.50, 48.50, 74.00, 3.50, 'PASS', '2026-03-27', 3.42),
(25, 13, 38.00, 58.50, 96.50, 4.00, 'PASS', '2026-03-27', 3.33),
(26, 13, 26.00, 34.00, 60.00, 3.00, 'PASS', '2026-03-27', 3.35),
(27, 13, 30.50, 49.50, 80.00, 4.00, 'PASS', '2026-03-27', 3.84),
(28, 13, 29.00, 46.00, 75.00, 3.50, 'PASS', '2026-03-27', 3.50),
(29, 13, 30.50, 46.00, 76.50, 3.50, 'PASS', '2026-03-27', 3.26),
(30, 13, 35.00, 46.50, 81.50, 3.85, 'PASS', '2026-03-27', 3.72),
(14, 15, 31.00, 39.50, 70.50, 3.58, 'PASS', '2026-03-27', 3.58),
(15, 15, 22.00, 31.00, 53.00, 2.12, 'PASS', '2026-03-27', 2.12),
(35, 15, 26.00, 40.00, 66.00, 2.88, 'PASS', '2026-03-27', 2.88),
(36, 15, 33.50, 49.50, 83.00, 3.88, 'PASS', '2026-03-27', 3.88),
(37, 15, 30.50, 50.00, 80.50, 3.88, 'PASS', '2026-03-27', 3.88),
(38, 15, 32.50, 46.50, 79.00, 3.82, 'PASS', '2026-03-27', 3.82),
(14, 16, 35.00, 49.50, 84.50, 3.58, 'PASS', '2026-03-27', 3.58),
(15, 16, 25.00, 34.00, 59.00, 2.12, 'PASS', '2026-03-27', 2.12),
(35, 16, 26.00, 37.00, 63.00, 2.88, 'PASS', '2026-03-27', 2.88),
(36, 16, 30.50, 48.50, 79.00, 3.88, 'PASS', '2026-03-27', 3.88),
(37, 16, 33.50, 43.00, 76.50, 3.88, 'PASS', '2026-03-27', 3.88),
(38, 16, 31.50, 49.00, 80.50, 3.82, 'PASS', '2026-03-27', 3.82),
(39, 18, 32.50, 60.00, 92.50, 4.00, 'PASS', '2026-03-27', 4.00),
(40, 18, 25.00, 36.50, 61.50, 2.70, 'PASS', '2026-03-27', 2.70),
(41, 18, 23.50, 38.50, 62.00, 2.54, 'PASS', '2026-03-27', 2.54),
(42, 18, 27.50, 36.00, 63.50, 3.10, 'PASS', '2026-03-27', 3.10),
(43, 18, 29.50, 45.00, 74.50, 3.46, 'PASS', '2026-03-27', 3.46),
(44, 18, 31.00, 48.50, 79.50, 3.82, 'PASS', '2026-03-27', 3.82),
(39, 19, 33.50, 51.00, 84.50, 4.00, 'PASS', '2026-03-27', 4.00),
(40, 19, 20.50, 42.50, 63.00, 2.70, 'PASS', '2026-03-27', 2.70),
(41, 19, 22.00, 37.50, 59.50, 2.54, 'PASS', '2026-03-27', 2.54),
(42, 19, 31.50, 44.50, 76.00, 3.10, 'PASS', '2026-03-27', 3.10),
(43, 19, 31.50, 46.50, 78.00, 3.46, 'PASS', '2026-03-27', 3.46),
(44, 19, 37.50, 52.50, 90.00, 3.82, 'PASS', '2026-03-27', 3.82),
(9, 21, 36.50, 51.00, 87.50, 3.89, 'PASS', '2026-09-04', 3.94),
(10, 21, 32.50, 42.00, 74.50, 3.45, 'PASS', '2026-09-04', 3.69),
(11, 21, 27.50, 45.50, 73.00, 3.06, 'PASS', '2026-09-04', 3.03),
(12, 21, 21.50, 24.00, 45.50, 1.82, 'PASS', '2026-09-04', 1.90),
(13, 21, 18.00, 26.50, 44.50, 1.45, 'PASS', '2026-09-04', 1.26),
(25, 21, 22.00, 26.00, 48.00, 3.23, 'PASS', '2026-09-04', 3.71),
(31, 21, 36.50, 55.00, 91.50, 4.00, 'PASS', '2026-09-04', 4.00),
(32, 21, 36.50, 52.00, 88.50, 4.00, 'PASS', '2026-09-04', 3.85),
(33, 21, 20.50, 31.00, 51.50, 1.71, 'PASS', '2026-09-04', 1.71),
(34, 21, 30.50, 44.50, 75.00, 3.78, 'PASS', '2026-09-04', 3.81),
(9, 22, 35.00, 43.00, 78.00, 3.89, 'PASS', '2026-09-04', 3.94),
(10, 22, 34.50, 45.00, 79.50, 3.45, 'PASS', '2026-09-04', 3.69),
(12, 22, 26.00, 31.50, 57.50, 1.82, 'PASS', '2026-09-04', 1.90),
(13, 22, 16.00, 29.00, 45.00, 1.45, 'PASS', '2026-09-04', 1.26),
(31, 22, 29.00, 53.50, 82.50, 4.00, 'PASS', '2026-09-04', 4.00),
(32, 22, 36.50, 55.50, 92.00, 4.00, 'PASS', '2026-09-04', 3.85),
(33, 22, 18.50, 28.00, 46.50, 1.71, 'PASS', '2026-09-04', 1.71),
(34, 22, 28.50, 49.50, 78.00, 3.78, 'PASS', '2026-09-04', 3.81),
(9, 24, 31.00, 52.50, 83.50, 3.89, 'PASS', '2026-09-04', 3.94),
(10, 24, 27.00, 44.50, 71.50, 3.45, 'PASS', '2026-09-04', 3.69),
(11, 24, 23.50, 41.00, 64.50, 3.06, 'PASS', '2026-09-04', 3.03),
(12, 24, 21.50, 23.00, 44.50, 1.82, 'PASS', '2026-09-04', 1.90),
(13, 24, 18.00, 24.00, 42.00, 1.45, 'PASS', '2026-09-04', 1.26),
(31, 24, 31.00, 50.00, 81.00, 4.00, 'PASS', '2026-09-04', 4.00),
(32, 24, 34.00, 46.50, 80.50, 4.00, 'PASS', '2026-09-04', 3.85),
(33, 24, 17.50, 24.50, 42.00, 1.71, 'PASS', '2026-09-04', 1.71),
(34, 24, 35.00, 50.00, 85.00, 3.78, 'PASS', '2026-09-04', 3.81),
(23, 25, 27.50, 42.50, 70.00, 3.50, 'PASS', '2026-09-04', 3.45),
(24, 25, 29.50, 44.50, 74.00, 3.30, 'PASS', '2026-09-04', 3.39),
(25, 25, 37.50, 58.50, 96.00, 3.23, 'PASS', '2026-09-04', 3.71),
(26, 25, 28.50, 36.50, 65.00, 3.00, 'PASS', '2026-09-04', 3.27),
(27, 25, 31.00, 54.00, 85.00, 3.85, 'PASS', '2026-09-04', 3.84),
(28, 25, 26.00, 48.50, 74.50, 3.65, 'PASS', '2026-09-04', 3.54),
(29, 25, 27.00, 50.00, 77.00, 3.85, 'PASS', '2026-09-04', 3.44),
(30, 25, 32.50, 52.00, 84.50, 3.85, 'PASS', '2026-09-04', 3.75),
(23, 27, 32.00, 43.00, 75.00, 3.50, 'PASS', '2026-09-04', 3.45),
(24, 27, 27.00, 45.00, 72.00, 3.30, 'PASS', '2026-09-04', 3.39),
(25, 27, 34.50, 59.00, 93.50, 3.23, 'PASS', '2026-09-04', 3.71),
(26, 27, 28.00, 40.00, 68.00, 3.00, 'PASS', '2026-09-04', 3.27),
(27, 27, 31.50, 44.00, 75.50, 3.85, 'PASS', '2026-09-04', 3.84),
(28, 27, 32.50, 49.50, 82.00, 3.65, 'PASS', '2026-09-04', 3.54),
(29, 27, 29.00, 52.00, 81.00, 3.85, 'PASS', '2026-09-04', 3.44),
(30, 27, 31.00, 47.00, 78.00, 3.85, 'PASS', '2026-09-04', 3.75),
(14, 28, 29.50, 52.50, 82.00, 4.00, 'PASS', '2026-09-04', 3.74),
(35, 28, 22.00, 42.50, 64.50, 2.70, 'PASS', '2026-09-04', 2.81),
(36, 28, 31.50, 51.50, 83.00, 4.00, 'PASS', '2026-09-04', 3.92),
(37, 28, 29.50, 42.50, 72.00, 3.30, 'PASS', '2026-09-04', 3.66),
(38, 28, 33.50, 45.50, 79.00, 3.70, 'PASS', '2026-09-04', 3.78),
(39, 30, 30.50, 51.00, 81.50, 4.00, 'PASS', '2026-09-04', 4.00),
(41, 30, 26.00, 46.00, 72.00, 3.30, 'PASS', '2026-09-04', 2.83),
(42, 30, 28.50, 51.50, 80.00, 4.00, 'PASS', '2026-09-04', 3.44),
(43, 30, 33.50, 48.00, 81.50, 4.00, 'PASS', '2026-09-04', 3.66),
(44, 30, 30.50, 50.00, 80.50, 4.00, 'PASS', '2026-09-04', 3.89);

-- repeat_student

INSERT INTO repeat_student (student_id, subject_id, repeat_year, repeat_shy, review) VALUES
(23, 2, 2025, 'Second attempt', 'Failed the final exam in the first attempt. Registered to repeat the course.'),
(25, 3, 2026, 'Second attempt', 'Failed the final exam in the first attempt. Registered to repeat the course.'),
(13, 1, 2026, 'Second attempt', 'Failed the final exam in the first attempt. Registered to repeat the course.');

-- timetable_entity

INSERT INTO timetable_entity (timetable_entity_id, timetable_id, offering_id, day, start_time, end_time, lecture_hall, session_type) VALUES
(1, 1, 1, 'MONDAY', '08:00:00', '10:00:00', 'LH-01', 'LECTURE'),
(2, 1, 1, 'WEDNESDAY', '13:00:00', '16:00:00', 'ICT Lab 1', 'PRACTICAL'),
(3, 1, 2, 'TUESDAY', '08:00:00', '10:00:00', 'LH-02', 'LECTURE'),
(4, 1, 3, 'MONDAY', '08:00:00', '10:00:00', 'LH-01', 'LECTURE'),
(5, 1, 3, 'WEDNESDAY', '13:00:00', '16:00:00', 'ICT Lab 1', 'PRACTICAL'),
(6, 1, 4, 'TUESDAY', '08:00:00', '10:00:00', 'LH-02', 'LECTURE'),
(7, 1, 4, 'THURSDAY', '13:00:00', '16:00:00', 'ICT Lab 2', 'PRACTICAL'),
(8, 1, 5, 'WEDNESDAY', '08:00:00', '10:00:00', 'LH-03', 'LECTURE'),
(9, 1, 5, 'FRIDAY', '13:00:00', '16:00:00', 'ICT Lab 1', 'PRACTICAL'),
(10, 2, 6, 'MONDAY', '08:00:00', '10:00:00', 'LH-01', 'LECTURE'),
(11, 2, 6, 'WEDNESDAY', '13:00:00', '16:00:00', 'ICT Lab 1', 'PRACTICAL'),
(12, 2, 7, 'TUESDAY', '08:00:00', '10:00:00', 'LH-02', 'LECTURE'),
(13, 2, 8, 'WEDNESDAY', '08:00:00', '10:00:00', 'LH-03', 'LECTURE'),
(14, 2, 8, 'FRIDAY', '13:00:00', '16:00:00', 'ICT Lab 1', 'PRACTICAL'),
(15, 2, 9, 'THURSDAY', '08:00:00', '10:00:00', 'LH-01', 'LECTURE'),
(16, 2, 9, 'MONDAY', '13:00:00', '16:00:00', 'ICT Lab 2', 'PRACTICAL'),
(17, 2, 10, 'FRIDAY', '08:00:00', '10:00:00', 'ET-LH-01', 'LECTURE'),
(18, 2, 11, 'MONDAY', '10:00:00', '12:00:00', 'ET-LH-01', 'LECTURE'),
(19, 2, 12, 'TUESDAY', '10:00:00', '12:00:00', 'BST-LH-01', 'LECTURE'),
(20, 2, 12, 'THURSDAY', '13:00:00', '16:00:00', 'BST Lab 1', 'PRACTICAL'),
(21, 2, 13, 'WEDNESDAY', '10:00:00', '12:00:00', 'BST-LH-01', 'LECTURE'),
(22, 2, 14, 'MONDAY', '08:00:00', '10:00:00', 'LH-01', 'LECTURE'),
(23, 2, 14, 'WEDNESDAY', '13:00:00', '16:00:00', 'ICT Lab 1', 'PRACTICAL'),
(24, 2, 15, 'TUESDAY', '08:00:00', '10:00:00', 'LH-02', 'LECTURE'),
(25, 2, 15, 'THURSDAY', '13:00:00', '16:00:00', 'ICT Lab 2', 'PRACTICAL'),
(26, 2, 16, 'WEDNESDAY', '08:00:00', '10:00:00', 'LH-03', 'LECTURE'),
(27, 2, 16, 'FRIDAY', '13:00:00', '16:00:00', 'ICT Lab 1', 'PRACTICAL'),
(28, 2, 17, 'THURSDAY', '08:00:00', '10:00:00', 'LH-01', 'LECTURE'),
(29, 2, 18, 'FRIDAY', '08:00:00', '10:00:00', 'LH-02', 'LECTURE'),
(30, 2, 19, 'MONDAY', '10:00:00', '12:00:00', 'ET-LH-01', 'LECTURE'),
(31, 2, 19, 'WEDNESDAY', '13:00:00', '16:00:00', 'ET Lab 1', 'PRACTICAL'),
(32, 2, 20, 'TUESDAY', '10:00:00', '12:00:00', 'BST-LH-01', 'LECTURE'),
(33, 3, 21, 'MONDAY', '08:00:00', '10:00:00', 'LH-01', 'LECTURE'),
(34, 3, 21, 'WEDNESDAY', '13:00:00', '16:00:00', 'ICT Lab 1', 'PRACTICAL'),
(35, 3, 22, 'TUESDAY', '08:00:00', '10:00:00', 'LH-02', 'LECTURE'),
(36, 3, 22, 'THURSDAY', '13:00:00', '16:00:00', 'ICT Lab 2', 'PRACTICAL'),
(37, 3, 23, 'WEDNESDAY', '08:00:00', '10:00:00', 'LH-03', 'LECTURE'),
(38, 3, 23, 'FRIDAY', '13:00:00', '16:00:00', 'ICT Lab 1', 'PRACTICAL'),
(39, 3, 24, 'THURSDAY', '08:00:00', '10:00:00', 'LH-01', 'LECTURE'),
(40, 3, 25, 'FRIDAY', '08:00:00', '10:00:00', 'ET-LH-01', 'LECTURE'),
(41, 3, 26, 'MONDAY', '10:00:00', '12:00:00', 'BST-LH-01', 'LECTURE'),
(42, 3, 27, 'TUESDAY', '10:00:00', '12:00:00', 'LH-01', 'LECTURE'),
(43, 3, 27, 'THURSDAY', '13:00:00', '16:00:00', 'ICT Lab 1', 'PRACTICAL');

-- medical

INSERT INTO medical (medical_id, student_id, medical_date, reason, certificate_path, status, reviewer_note, reviewed_date) VALUES
(1, 26, '2025-03-18', 'Dengue recovery', '/uploads/medical/26_2025-03-18.pdf', 'APPROVED', 'Certificate verified.', '2025-03-23'),
(2, 28, '2025-04-28', 'Dental surgery', '/uploads/medical/28_2025-04-28.pdf', 'APPROVED', 'Certificate verified.', '2025-05-02'),
(3, 25, '2025-04-30', 'Family emergency', '/uploads/medical/25_2025-04-30.pdf', 'REJECTED', 'Certificate not issued by a registered practitioner.', '2025-05-04'),
(4, 34, '2025-10-20', 'Eye infection', '/uploads/medical/34_2025-10-20.pdf', 'APPROVED', 'Certificate verified.', '2025-10-22'),
(5, 14, '2025-11-03', 'Migraine', '/uploads/medical/14_2025-11-03.pdf', 'APPROVED', 'Certificate verified.', '2025-11-07'),
(6, 43, '2025-11-06', 'Family emergency', '/uploads/medical/43_2025-11-06.pdf', 'REJECTED', 'Certificate not issued by a registered practitioner.', '2025-11-11'),
(7, 31, '2025-11-17', 'Back pain', '/uploads/medical/31_2025-11-17.pdf', 'APPROVED', 'Certificate verified.', '2025-11-20'),
(8, 11, '2025-11-18', 'Minor accident', '/uploads/medical/11_2025-11-18.pdf', 'APPROVED', 'Certificate verified.', '2025-11-20'),
(9, 12, '2025-12-02', 'Food poisoning', '/uploads/medical/12_2025-12-02.pdf', 'APPROVED', 'Certificate verified.', '2025-12-05'),
(10, 41, '2025-12-02', 'Food poisoning', '/uploads/medical/41_2025-12-02.pdf', 'APPROVED', 'Certificate verified.', '2025-12-06'),
(11, 14, '2025-12-05', 'Dengue recovery', '/uploads/medical/14_2025-12-05.pdf', 'APPROVED', 'Certificate verified.', '2025-12-10'),
(12, 11, '2026-03-03', 'Respiratory infection', '/uploads/medical/11_2026-03-03.pdf', 'REJECTED', 'Certificate submitted after the deadline.', '2026-03-12'),
(13, 34, '2026-04-03', 'Dental surgery', '/uploads/medical/34_2026-04-03.pdf', 'APPROVED', 'Certificate verified.', '2026-04-06'),
(14, 13, '2026-04-29', 'Dengue recovery', '/uploads/medical/13_2026-04-29.pdf', 'APPROVED', 'Certificate verified.', '2026-05-04'),
(15, 41, '2026-05-12', 'Dengue recovery', '/uploads/medical/41_2026-05-12.pdf', 'APPROVED', 'Certificate verified.', '2026-05-16'),
(16, 9, '2026-10-06', 'Dengue recovery', '/uploads/medical/9_2026-10-06.pdf', 'PENDING', NULL, NULL),
(17, 13, '2026-10-12', 'Food poisoning', '/uploads/medical/13_2026-10-12.pdf', 'PENDING', NULL, NULL),
(18, 27, '2026-10-14', 'Dental surgery', '/uploads/medical/27_2026-10-14.pdf', 'PENDING', NULL, NULL),
(19, 29, '2026-10-15', 'Viral fever', '/uploads/medical/29_2026-10-15.pdf', 'PENDING', NULL, NULL),
(20, 13, '2026-10-22', 'Family emergency', '/uploads/medical/13_2026-10-22.pdf', 'PENDING', NULL, NULL);

-- notice

INSERT INTO notice (notice_id, created_by, title, description, created_date, audience, expiry_date, status) VALUES
(1, 1, 'Semester 1 Course Registration', 'Complete your course registration before the deadline.', '2026-09-28 09:00:00', 'UNDERGRADUATE', '2026-10-20', 'EXPIRED'),
(2, 1, 'Semester 2 Results Released', 'Results for 2025/2026 Semester 2 are now available.', '2026-09-04 10:30:00', 'UNDERGRADUATE', '2026-09-30', 'EXPIRED'),
(3, 2, 'Lecturer Meeting', 'Department meeting on Friday at 2.00 PM in the conference room.', '2026-10-05 08:45:00', 'LECTURER', '2026-10-16', 'EXPIRED'),
(4, 2, 'Lab Maintenance', 'ICT labs will be closed for maintenance this Saturday.', '2026-10-06 14:00:00', 'TECHNICAL_OFFICER', '2026-10-17', 'EXPIRED'),
(5, 1, 'Public Holiday', 'The university will be closed on the upcoming public holiday.', '2026-10-08 11:15:00', 'ALL', '2026-10-25', 'EXPIRED'),
(6, 1, 'Mid Semester Exam Schedule', 'Draft schedule for mid semester exams.', '2026-10-09 16:00:00', 'UNDERGRADUATE', NULL, 'DRAFT'),
(7, 1, 'Semester 1 Course Registration 2025/2026', 'Complete your course registration before the deadline.', '2025-09-29 09:00:00', 'UNDERGRADUATE', '2025-10-17', 'EXPIRED'),
(8, 2, 'Orientation Programme for the 2025 Intake', 'The orientation programme starts at 9.00 AM in the main auditorium.', '2025-09-22 10:00:00', 'UNDERGRADUATE', '2025-10-03', 'EXPIRED'),
(9, 2, 'Semester 2 Results Released', 'Results for 2024/2025 Semester 2 are now available.', '2025-09-05 10:30:00', 'UNDERGRADUATE', '2025-09-30', 'EXPIRED'),
(10, 1, 'Semester 1 Results Released', 'Results for 2025/2026 Semester 1 are now available.', '2026-03-27 10:00:00', 'UNDERGRADUATE', '2026-04-15', 'EXPIRED'),
(11, 16, 'Semester 2 Course Registration', 'Register for Semester 2 courses through the portal.', '2026-02-20 09:00:00', 'UNDERGRADUATE', '2026-03-06', 'EXPIRED'),
(12, 1, 'Annual Sports Meet', 'The annual inter-faculty sports meet will be held at the main ground.', '2026-05-04 08:00:00', 'ALL', '2026-05-30', 'EXPIRED'),
(13, 16, 'Medical Certificate Submission', 'Submit medical certificates within 7 days of the absence.', '2026-10-26 09:30:00', 'UNDERGRADUATE', '2026-11-13', 'ACTIVE'),
(14, 1, 'Library Opening Hours Extended', 'The library will remain open until 8.00 PM on weekdays during the exam period.', '2026-10-27 13:00:00', 'ALL', '2026-12-18', 'ACTIVE'),
(15, 2, 'ICT Lab Equipment Audit', 'Technical officers are requested to complete the annual equipment audit.', '2026-10-29 09:15:00', 'TECHNICAL_OFFICER', '2026-11-06', 'ACTIVE'),
(16, 16, 'Mid Semester Marks Submission', 'Lecturers must submit continuous assessment marks before the deadline.', '2026-10-30 11:00:00', 'LECTURER', '2026-11-20', 'ACTIVE'),
(17, 2, 'Deepavali Holiday', 'The university will remain closed on the Deepavali public holiday.', '2026-10-30 15:30:00', 'ALL', '2026-11-09', 'ACTIVE');

-- =========================================================
-- Step 1: Clean up existing users
-- =========================================================

DROP USER IF EXISTS 'afms_login'@'%';
DROP USER IF EXISTS 'afms_admin'@'%';
DROP USER IF EXISTS 'afms_lecturer'@'%';
DROP USER IF EXISTS 'afms_officer'@'%';
DROP USER IF EXISTS 'afms_student'@'%';

-- =========================================================
-- Step 2: Create users with passwords
-- =========================================================

CREATE USER 'afms_login'@'%' IDENTIFIED BY 'login_dev_pw';
CREATE USER 'afms_admin'@'%' IDENTIFIED BY 'admin_dev_pw';
CREATE USER 'afms_lecturer'@'%' IDENTIFIED BY 'lecturer_dev_pw';
CREATE USER 'afms_officer'@'%' IDENTIFIED BY 'officer_dev_pw';
CREATE USER 'afms_student'@'%' IDENTIFIED BY 'student_dev_pw';

-- =========================================================
-- Step 3: Grant privileges
-- =========================================================

-- ---------------------------------------------------------
-- afms_login: Login screen only (read users table)
-- ---------------------------------------------------------
GRANT SELECT ON `afms_db`.`users` TO 'afms_login'@'%';

-- ---------------------------------------------------------
-- afms_admin: Full CRUD on all tables (no schema changes)
-- ---------------------------------------------------------
GRANT SELECT, INSERT, UPDATE, DELETE ON `afms_db`.* TO 'afms_admin'@'%';

-- ---------------------------------------------------------
-- afms_lecturer: Teaching operations
-- ---------------------------------------------------------

-- Read-only on reference tables
GRANT SELECT ON `afms_db`.`users` TO 'afms_lecturer'@'%';
GRANT SELECT ON `afms_db`.`department` TO 'afms_lecturer'@'%';
GRANT SELECT ON `afms_db`.`batch` TO 'afms_lecturer'@'%';
GRANT SELECT ON `afms_db`.`semester` TO 'afms_lecturer'@'%';
GRANT SELECT ON `afms_db`.`course` TO 'afms_lecturer'@'%';
GRANT SELECT ON `afms_db`.`course_offering` TO 'afms_lecturer'@'%';
GRANT SELECT ON `afms_db`.`undergraduate` TO 'afms_lecturer'@'%';
GRANT SELECT ON `afms_db`.`lecturer` TO 'afms_lecturer'@'%';
GRANT SELECT ON `afms_db`.`enrollment` TO 'afms_lecturer'@'%';
GRANT SELECT ON `afms_db`.`timetable` TO 'afms_lecturer'@'%';
GRANT SELECT ON `afms_db`.`timetable_entity` TO 'afms_lecturer'@'%';
GRANT SELECT ON `afms_db`.`repeat_student` TO 'afms_lecturer'@'%';

-- Full CRUD on teaching tables
GRANT SELECT, INSERT, UPDATE, DELETE ON `afms_db`.`course_material` TO 'afms_lecturer'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON `afms_db`.`assessment` TO 'afms_lecturer'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON `afms_db`.`session` TO 'afms_lecturer'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON `afms_db`.`attend` TO 'afms_lecturer'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON `afms_db`.`exam` TO 'afms_lecturer'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON `afms_db`.`result` TO 'afms_lecturer'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON `afms_db`.`eligibility` TO 'afms_lecturer'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON `afms_db`.`notice` TO 'afms_lecturer'@'%';
GRANT SELECT, UPDATE ON `afms_db`.`medical` TO 'afms_lecturer'@'%';

-- Column-level updates (own profile only)
GRANT UPDATE (`name`, `email`, `phone`, `address`, `dob`, `password`) ON `afms_db`.`users` TO 'afms_lecturer'@'%';
GRANT UPDATE (`qualification`) ON `afms_db`.`lecturer` TO 'afms_lecturer'@'%';

-- ---------------------------------------------------------
-- afms_officer: Attendance and medical operations
-- ---------------------------------------------------------

-- Read-only on reference tables
GRANT SELECT ON `afms_db`.`users` TO 'afms_officer'@'%';
GRANT SELECT ON `afms_db`.`department` TO 'afms_officer'@'%';
GRANT SELECT ON `afms_db`.`batch` TO 'afms_officer'@'%';
GRANT SELECT ON `afms_db`.`semester` TO 'afms_officer'@'%';
GRANT SELECT ON `afms_db`.`course` TO 'afms_officer'@'%';
GRANT SELECT ON `afms_db`.`course_offering` TO 'afms_officer'@'%';
GRANT SELECT ON `afms_db`.`undergraduate` TO 'afms_officer'@'%';
GRANT SELECT ON `afms_db`.`technical_officer` TO 'afms_officer'@'%';
GRANT SELECT ON `afms_db`.`enrollment` TO 'afms_officer'@'%';
GRANT SELECT ON `afms_db`.`session` TO 'afms_officer'@'%';
GRANT SELECT ON `afms_db`.`timetable` TO 'afms_officer'@'%';
GRANT SELECT ON `afms_db`.`timetable_entity` TO 'afms_officer'@'%';
GRANT SELECT ON `afms_db`.`notice` TO 'afms_officer'@'%';

-- Full CRUD on attendance, medical
GRANT SELECT, INSERT, UPDATE, DELETE ON `afms_db`.`attend` TO 'afms_officer'@'%';
GRANT SELECT, INSERT, UPDATE ON `afms_db`.`medical` TO 'afms_officer'@'%';

-- Column-level updates (own profile only)
GRANT UPDATE (`name`, `email`, `phone`, `address`, `dob`, `password`) ON `afms_db`.`users` TO 'afms_officer'@'%';

-- ---------------------------------------------------------
-- afms_student: View grades, submit medicals
-- ---------------------------------------------------------

-- Read-only on all academic tables
GRANT SELECT ON `afms_db`.`users` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`department` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`batch` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`semester` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`course` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`course_offering` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`course_material` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`undergraduate` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`lecturer` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`enrollment` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`session` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`attend` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`assessment` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`exam` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`result` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`eligibility` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`repeat_student` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`timetable` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`timetable_entity` TO 'afms_student'@'%';
GRANT SELECT ON `afms_db`.`notice` TO 'afms_student'@'%';

-- Submit medical applications
GRANT SELECT, INSERT ON `afms_db`.`medical` TO 'afms_student'@'%';

-- Column-level updates (own contact info only)
GRANT UPDATE (`phone`, `address`, `password`) ON `afms_db`.`users` TO 'afms_student'@'%';
GRANT UPDATE (`profile_picture`) ON `afms_db`.`undergraduate` TO 'afms_student'@'%';

-- =========================================================
-- Finalize
-- =========================================================

FLUSH PRIVILEGES;

-- =========================================================
-- Verification queries (optional - uncomment to run)
-- =========================================================
-- SELECT u.user_id, u.name, ug.student_number, ug.batch_id FROM undergraduate ug JOIN users u USING (user_id) ORDER BY ug.student_number;
-- SELECT user_id, name, gender, photo FROM users ORDER BY gender, user_id;
-- SELECT COUNT(*) AS materials FROM course_material;   -- expect 100
-- SHOW TRIGGERS;

-- =========================================================
-- Setup complete
-- =========================================================
