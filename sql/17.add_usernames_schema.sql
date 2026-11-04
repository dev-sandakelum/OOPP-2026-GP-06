-- =========================================================
-- Migration 17: Staff numbers + usernames  (TABLE / TRIGGER changes only)
-- Run once against afms_db AFTER afms_final_full.sql (and 9-16).
-- Then run 18.fix_user_numbers_and_usernames.sql to fill existing rows.
-- Existing SQL files are NOT modified.
--
-- WHAT THIS ADDS
-- --------------
-- * admin.staff_number, lecturer.staff_number, technical_officer.staff_number
--     Same format as student numbers:  <PREFIX>/<year>/<number>
--       AD = admin, LE = lecturer, TO = technical officer, TG = student
-- * users.username   e.g. TG/2024/1001 -> tg1001,  LE/2026/1003 -> le1003
--     Stored lowercase. The DB collation (utf8mb4_unicode_ci) is
--     case-insensitive, so "TG1001" and "tg1001" both match at login.
--
-- NUMBERING RULE (all prefixes)
-- -----------------------------
-- A number is NEVER reused, even in a later year. Each prefix has one
-- counter that keeps growing: next = highest number ever used + 1
-- (starting at 1001). The number therefore shows how many people of
-- that type have ever been registered. The year part is:
--   students -> intake year of the batch (current year if no batch)
--   staff    -> year the account was created
--
-- username stays NULL-able: the users row is inserted first, then the
-- subtype row (undergraduate / lecturer / ...) whose trigger fills it.
-- =========================================================

USE afms_db;

-- ---------------------------------------------------------
-- 1. New columns  (each step is skipped if it already exists,
--    so the file is safe to re-run after a partial run)
-- ---------------------------------------------------------
DROP PROCEDURE IF EXISTS _add_column_if_missing;
DROP PROCEDURE IF EXISTS _add_constraint_if_missing;

DELIMITER $$

-- ALTER TABLE <tbl> ADD COLUMN <col> <definition>   ... only if <col> is missing
CREATE PROCEDURE _add_column_if_missing(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_definition TEXT)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_column) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

-- ALTER TABLE <tbl> ADD CONSTRAINT <name> <definition>   ... only if <name> is missing
CREATE PROCEDURE _add_constraint_if_missing(IN p_table VARCHAR(64), IN p_name VARCHAR(64), IN p_definition TEXT)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND CONSTRAINT_NAME = p_name) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD CONSTRAINT `', p_name, '` ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

-- users.username
CALL _add_column_if_missing('users', 'username',
    'VARCHAR(20) DEFAULT NULL COMMENT ''Login name, e.g. tg1001 / le1003 (auto-filled by trigger)'' AFTER email');
CALL _add_constraint_if_missing('users', 'uq_users_username', 'UNIQUE (username)');

-- admin.staff_number
CALL _add_column_if_missing('admin', 'staff_number',
    'VARCHAR(20) DEFAULT NULL COMMENT ''AD/<year>/<number> (auto-filled by trigger)''');
CALL _add_constraint_if_missing('admin', 'uq_admin_staff_number', 'UNIQUE (staff_number)');
CALL _add_constraint_if_missing('admin', 'chk_admin_staff_number',
    'CHECK (staff_number REGEXP ''^AD/[0-9]{4}/[0-9]{4}$'')');

-- lecturer.staff_number
CALL _add_column_if_missing('lecturer', 'staff_number',
    'VARCHAR(20) DEFAULT NULL COMMENT ''LE/<year>/<number> (auto-filled by trigger)'' AFTER user_id');
CALL _add_constraint_if_missing('lecturer', 'uq_lecturer_staff_number', 'UNIQUE (staff_number)');
CALL _add_constraint_if_missing('lecturer', 'chk_lecturer_staff_number',
    'CHECK (staff_number REGEXP ''^LE/[0-9]{4}/[0-9]{4}$'')');

-- technical_officer.staff_number
CALL _add_column_if_missing('technical_officer', 'staff_number',
    'VARCHAR(20) DEFAULT NULL COMMENT ''TO/<year>/<number> (auto-filled by trigger)'' AFTER user_id');
CALL _add_constraint_if_missing('technical_officer', 'uq_to_staff_number', 'UNIQUE (staff_number)');
CALL _add_constraint_if_missing('technical_officer', 'chk_to_staff_number',
    'CHECK (staff_number REGEXP ''^TO/[0-9]{4}/[0-9]{4}$'')');

DROP PROCEDURE IF EXISTS _add_column_if_missing;
DROP PROCEDURE IF EXISTS _add_constraint_if_missing;

-- ---------------------------------------------------------
-- 2. Triggers
-- ---------------------------------------------------------
DROP TRIGGER IF EXISTS trg_undergraduate_student_number;
DROP TRIGGER IF EXISTS trg_undergraduate_username_ins;
DROP TRIGGER IF EXISTS trg_undergraduate_username_upd;
DROP TRIGGER IF EXISTS trg_admin_staff_number;
DROP TRIGGER IF EXISTS trg_admin_username_ins;
DROP TRIGGER IF EXISTS trg_admin_username_upd;
DROP TRIGGER IF EXISTS trg_lecturer_staff_number;
DROP TRIGGER IF EXISTS trg_lecturer_username_ins;
DROP TRIGGER IF EXISTS trg_lecturer_username_upd;
DROP TRIGGER IF EXISTS trg_to_staff_number;
DROP TRIGGER IF EXISTS trg_to_username_ins;
DROP TRIGGER IF EXISTS trg_to_username_upd;

DELIMITER $$

-- ----- Students ---------------------------------------------------------
-- Replaces the old trigger, which restarted at 1001 every intake year.
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

        -- global counter: never reuse a number from any year
        SET v_next = (SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(student_number, '/', -1) AS UNSIGNED)), 1000) + 1
                      FROM undergraduate
                      WHERE student_number LIKE 'TG/%');

        SET NEW.student_number = CONCAT('TG/', v_year, '/', v_next);
    END IF;
END$$

CREATE TRIGGER trg_undergraduate_username_ins
AFTER INSERT ON undergraduate
FOR EACH ROW
BEGIN
    UPDATE users
    SET username = LOWER(CONCAT(SUBSTRING_INDEX(NEW.student_number, '/', 1),
                                SUBSTRING_INDEX(NEW.student_number, '/', -1)))
    WHERE user_id = NEW.user_id;
END$$

CREATE TRIGGER trg_undergraduate_username_upd
AFTER UPDATE ON undergraduate
FOR EACH ROW
BEGIN
    IF NOT (NEW.student_number <=> OLD.student_number) THEN
        UPDATE users
        SET username = LOWER(CONCAT(SUBSTRING_INDEX(NEW.student_number, '/', 1),
                                    SUBSTRING_INDEX(NEW.student_number, '/', -1)))
        WHERE user_id = NEW.user_id;
    END IF;
END$$

-- ----- Admins -----------------------------------------------------------
CREATE TRIGGER trg_admin_staff_number
BEFORE INSERT ON admin
FOR EACH ROW
BEGIN
    IF NEW.staff_number IS NULL OR NEW.staff_number = '' THEN
        SET NEW.staff_number = CONCAT('AD/', YEAR(CURDATE()), '/',
            (SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(staff_number, '/', -1) AS UNSIGNED)), 1000) + 1
             FROM admin WHERE staff_number LIKE 'AD/%'));
    END IF;
END$$

CREATE TRIGGER trg_admin_username_ins
AFTER INSERT ON admin
FOR EACH ROW
BEGIN
    UPDATE users
    SET username = LOWER(CONCAT('AD', SUBSTRING_INDEX(NEW.staff_number, '/', -1)))
    WHERE user_id = NEW.user_id;
END$$

CREATE TRIGGER trg_admin_username_upd
AFTER UPDATE ON admin
FOR EACH ROW
BEGIN
    IF NOT (NEW.staff_number <=> OLD.staff_number) THEN
        UPDATE users
        SET username = LOWER(CONCAT('AD', SUBSTRING_INDEX(NEW.staff_number, '/', -1)))
        WHERE user_id = NEW.user_id;
    END IF;
END$$

-- ----- Lecturers --------------------------------------------------------
CREATE TRIGGER trg_lecturer_staff_number
BEFORE INSERT ON lecturer
FOR EACH ROW
BEGIN
    IF NEW.staff_number IS NULL OR NEW.staff_number = '' THEN
        SET NEW.staff_number = CONCAT('LE/', YEAR(CURDATE()), '/',
            (SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(staff_number, '/', -1) AS UNSIGNED)), 1000) + 1
             FROM lecturer WHERE staff_number LIKE 'LE/%'));
    END IF;
END$$

CREATE TRIGGER trg_lecturer_username_ins
AFTER INSERT ON lecturer
FOR EACH ROW
BEGIN
    UPDATE users
    SET username = LOWER(CONCAT('LE', SUBSTRING_INDEX(NEW.staff_number, '/', -1)))
    WHERE user_id = NEW.user_id;
END$$

CREATE TRIGGER trg_lecturer_username_upd
AFTER UPDATE ON lecturer
FOR EACH ROW
BEGIN
    IF NOT (NEW.staff_number <=> OLD.staff_number) THEN
        UPDATE users
        SET username = LOWER(CONCAT('LE', SUBSTRING_INDEX(NEW.staff_number, '/', -1)))
        WHERE user_id = NEW.user_id;
    END IF;
END$$

-- ----- Technical officers -----------------------------------------------
CREATE TRIGGER trg_to_staff_number
BEFORE INSERT ON technical_officer
FOR EACH ROW
BEGIN
    IF NEW.staff_number IS NULL OR NEW.staff_number = '' THEN
        SET NEW.staff_number = CONCAT('TO/', YEAR(CURDATE()), '/',
            (SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(staff_number, '/', -1) AS UNSIGNED)), 1000) + 1
             FROM technical_officer WHERE staff_number LIKE 'TO/%'));
    END IF;
END$$

CREATE TRIGGER trg_to_username_ins
AFTER INSERT ON technical_officer
FOR EACH ROW
BEGIN
    UPDATE users
    SET username = LOWER(CONCAT('TO', SUBSTRING_INDEX(NEW.staff_number, '/', -1)))
    WHERE user_id = NEW.user_id;
END$$

CREATE TRIGGER trg_to_username_upd
AFTER UPDATE ON technical_officer
FOR EACH ROW
BEGIN
    IF NOT (NEW.staff_number <=> OLD.staff_number) THEN
        UPDATE users
        SET username = LOWER(CONCAT('TO', SUBSTRING_INDEX(NEW.staff_number, '/', -1)))
        WHERE user_id = NEW.user_id;
    END IF;
END$$

DELIMITER ;
