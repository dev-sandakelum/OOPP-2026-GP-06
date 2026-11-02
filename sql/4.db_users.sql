-- =========================================================
-- AFMS Database User Accounts Setup
-- Compatible with MySQL 5.x / MariaDB (XAMPP default)
-- 
-- Run this AFTER importing schema.sql and mockData.sql
-- Execute as root user in phpMyAdmin SQL tab
--
-- Default passwords (for development only):
--   afms_login    : login_dev_pw
--   afms_admin    : admin_dev_pw
--   afms_lecturer : lecturer_dev_pw
--   afms_officer  : officer_dev_pw
--   afms_student  : student_dev_pw
-- =========================================================

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

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
COMMIT;

-- =========================================================
-- Setup complete
-- All 5 user accounts created with appropriate permissions
-- =========================================================
