-- =========================================================
-- AFMS Database SQL Script (cleaned)
-- DBMS: MySQL 8.x
-- WARNING: the DROP DATABASE below deletes any existing afms_db.
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
                       password  VARCHAR(255) NOT NULL,           -- store a hash, never plain text
                       phone     VARCHAR(30),                     -- shared by all user types
                       address   VARCHAR(255),
                       dob       DATE,
                       user_type ENUM('ADMIN','UNDERGRADUATE','LECTURER','TECHNICAL_OFFICER') NOT NULL
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
                               dep_id          INT,
                               batch_id        INT,
                               profile_picture VARCHAR(255),
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