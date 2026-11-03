-- =========================================================
-- AFMS Profile Update Privileges Fix
-- 
-- Purpose: Grant proper UPDATE privileges for user profiles
-- Issue: Column-level UPDATE grants don't work reliably with
--        prepared statements and JDBC. Users need full UPDATE
--        on their role-specific tables.
--
-- Security: Application logic MUST enforce that users can
--           only update their OWN profile records.
--
-- Run this AFTER 4.db_users.sql
-- Execute as root user in phpMyAdmin SQL tab
-- =========================================================

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

-- =========================================================
-- Fix UPDATE privileges for all roles
-- =========================================================

-- ---------------------------------------------------------
-- ADMIN: Already has full UPDATE on all tables (no change)
-- ---------------------------------------------------------
-- Admin already has: GRANT SELECT, INSERT, UPDATE, DELETE ON `afms_db`.* 

-- ---------------------------------------------------------
-- LECTURER: Grant full UPDATE on users and lecturer tables
-- ---------------------------------------------------------

-- Revoke old column-level UPDATE grants
REVOKE UPDATE (`name`, `email`, `phone`, `address`, `dob`, `password`) 
    ON `afms_db`.`users` FROM 'afms_lecturer'@'%';
REVOKE UPDATE (`qualification`) 
    ON `afms_db`.`lecturer` FROM 'afms_lecturer'@'%';

-- Grant full UPDATE on profile tables
GRANT UPDATE ON `afms_db`.`users` TO 'afms_lecturer'@'%';
GRANT UPDATE ON `afms_db`.`lecturer` TO 'afms_lecturer'@'%';

-- ---------------------------------------------------------
-- TECHNICAL OFFICER: Grant full UPDATE on users and technical_officer tables
-- ---------------------------------------------------------

-- Revoke old column-level UPDATE grants
REVOKE UPDATE (`name`, `email`, `phone`, `address`, `dob`, `password`) 
    ON `afms_db`.`users` FROM 'afms_officer'@'%';

-- Grant full UPDATE on profile tables
GRANT UPDATE ON `afms_db`.`users` TO 'afms_officer'@'%';
GRANT UPDATE ON `afms_db`.`technical_officer` TO 'afms_officer'@'%';

-- ---------------------------------------------------------
-- STUDENT: Grant full UPDATE on users and undergraduate tables
-- ---------------------------------------------------------

-- Revoke old column-level UPDATE grants
REVOKE UPDATE (`phone`, `address`, `password`) 
    ON `afms_db`.`users` FROM 'afms_student'@'%';
REVOKE UPDATE (`profile_picture`) 
    ON `afms_db`.`undergraduate` FROM 'afms_student'@'%';

-- Grant full UPDATE on profile tables
GRANT UPDATE ON `afms_db`.`users` TO 'afms_student'@'%';
GRANT UPDATE ON `afms_db`.`undergraduate` TO 'afms_student'@'%';

-- =========================================================
-- Additional privileges to ensure full CRUD operations
-- =========================================================

-- ---------------------------------------------------------
-- LECTURER: Ensure INSERT/SELECT on their own profile
-- ---------------------------------------------------------
-- (Already has SELECT, just ensuring consistency)
GRANT INSERT ON `afms_db`.`lecturer` TO 'afms_lecturer'@'%';

-- ---------------------------------------------------------
-- TECHNICAL OFFICER: Ensure INSERT/SELECT on their own profile
-- ---------------------------------------------------------
GRANT SELECT ON `afms_db`.`technical_officer` TO 'afms_officer'@'%';
GRANT INSERT ON `afms_db`.`technical_officer` TO 'afms_officer'@'%';

-- ---------------------------------------------------------
-- STUDENT: Already has SELECT and INSERT for medical
-- Ensure they can SELECT their own undergraduate record
-- ---------------------------------------------------------
-- (Already has SELECT on undergraduate table)

-- =========================================================
-- Finalize
-- =========================================================

FLUSH PRIVILEGES;
COMMIT;

-- =========================================================
-- Setup complete
-- 
-- All roles now have proper UPDATE privileges on their
-- profile tables:
--
-- - ADMIN:    Full CRUD on all tables
-- - LECTURER: Full UPDATE on users + lecturer tables
-- - OFFICER:  Full UPDATE on users + technical_officer tables  
-- - STUDENT:  Full UPDATE on users + undergraduate tables
--
-- IMPORTANT: Your Java application MUST enforce that users
-- can only update records where user_id matches their own
-- logged-in user_id.
-- =========================================================
