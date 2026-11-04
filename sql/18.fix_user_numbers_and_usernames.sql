-- =========================================================
-- Migration 18: Fix existing user numbers + fill usernames  (DATA only)
-- Run once against afms_db AFTER 17.add_usernames_schema.sql.
-- Existing SQL files are NOT modified. Safe to re-run.
--
-- WHY UPDATE AND NOT DELETE + RE-INSERT?
-- Deleting a users row cascades to undergraduate, enrollment, attend,
-- result, medical ... so all academic data would be lost. Rows are
-- therefore fixed in place; user_id never changes.
--
-- WHAT IS CHANGED (only rows that have a problem)
-- -----------------------------------------------
-- 1. Students whose number is a DUPLICATE of an earlier student's number.
--    The old trigger restarted at 1001 each year, so e.g.
--    TG/2024/1001 and TG/2025/1001 both exist. The earlier one (lower
--    year, then lower user_id) keeps its number; the later one gets the
--    next free number (highest ever + 1). Its year part is kept.
--    Students without a duplicate are NOT touched.
-- 2. Staff (admin / lecturer / technical officer) with no staff_number
--    get one: <PREFIX>/<current year>/<next number>, in user_id order.
-- 3. users.username is (re)built from the student / staff number for
--    every row where it is missing or out of date.
-- =========================================================

USE afms_db;

-- Helper tables (regular tables, not TEMPORARY, because MySQL cannot
-- open a temporary table twice in one query). Dropped at the end.
DROP TABLE IF EXISTS _fix_students;
DROP TABLE IF EXISTS _fix_student_new;
DROP TABLE IF EXISTS _fix_staff_new;

-- ---------------------------------------------------------
-- 1. Students: renumber duplicates only
-- ---------------------------------------------------------
CREATE TABLE _fix_students AS
SELECT user_id,
       CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(student_number, '/', 2), '/', -1) AS UNSIGNED) AS yr,
       CAST(SUBSTRING_INDEX(student_number, '/', -1) AS UNSIGNED)                         AS num
FROM undergraduate
WHERE student_number LIKE 'TG/%';

SET @max_student = (SELECT COALESCE(MAX(num), 1000) FROM _fix_students);

-- a row is a duplicate if an earlier student (lower year, or same year and
-- lower user_id) already has the same number
CREATE TABLE _fix_student_new AS
SELECT d.user_id,
       d.yr,
       CAST(@max_student + ROW_NUMBER() OVER (ORDER BY d.yr, d.user_id) AS UNSIGNED) AS new_num
FROM _fix_students d
WHERE EXISTS (SELECT 1
              FROM _fix_students e
              WHERE e.num = d.num
                AND (e.yr < d.yr OR (e.yr = d.yr AND e.user_id < d.user_id)));

-- new numbers are all above the old maximum, so no UNIQUE clash is possible
UPDATE undergraduate g
JOIN _fix_student_new n ON n.user_id = g.user_id
SET g.student_number = CONCAT('TG/', CAST(n.yr AS UNSIGNED), '/', CAST(n.new_num AS UNSIGNED));

-- ---------------------------------------------------------
-- 2. Staff numbers for rows that have none
-- ---------------------------------------------------------
-- Admins
SET @max_ad = (SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(staff_number, '/', -1) AS UNSIGNED)), 1000)
               FROM admin WHERE staff_number LIKE 'AD/%');
CREATE TABLE _fix_staff_new AS
SELECT user_id, CAST(@max_ad + ROW_NUMBER() OVER (ORDER BY user_id) AS UNSIGNED) AS new_num
FROM admin WHERE staff_number IS NULL OR staff_number = '';
UPDATE admin a JOIN _fix_staff_new n ON n.user_id = a.user_id
SET a.staff_number = CONCAT('AD/', YEAR(CURDATE()), '/', CAST(n.new_num AS UNSIGNED));
DROP TABLE _fix_staff_new;

-- Lecturers
SET @max_le = (SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(staff_number, '/', -1) AS UNSIGNED)), 1000)
               FROM lecturer WHERE staff_number LIKE 'LE/%');
CREATE TABLE _fix_staff_new AS
SELECT user_id, CAST(@max_le + ROW_NUMBER() OVER (ORDER BY user_id) AS UNSIGNED) AS new_num
FROM lecturer WHERE staff_number IS NULL OR staff_number = '';
UPDATE lecturer l JOIN _fix_staff_new n ON n.user_id = l.user_id
SET l.staff_number = CONCAT('LE/', YEAR(CURDATE()), '/', CAST(n.new_num AS UNSIGNED));
DROP TABLE _fix_staff_new;

-- Technical officers
SET @max_to = (SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(staff_number, '/', -1) AS UNSIGNED)), 1000)
               FROM technical_officer WHERE staff_number LIKE 'TO/%');
CREATE TABLE _fix_staff_new AS
SELECT user_id, CAST(@max_to + ROW_NUMBER() OVER (ORDER BY user_id) AS UNSIGNED) AS new_num
FROM technical_officer WHERE staff_number IS NULL OR staff_number = '';
UPDATE technical_officer t JOIN _fix_staff_new n ON n.user_id = t.user_id
SET t.staff_number = CONCAT('TO/', YEAR(CURDATE()), '/', CAST(n.new_num AS UNSIGNED));
DROP TABLE _fix_staff_new;

-- ---------------------------------------------------------
-- 3. Usernames (only where missing or different)
-- ---------------------------------------------------------
UPDATE users u
JOIN undergraduate g ON g.user_id = u.user_id
SET u.username = LOWER(CONCAT(SUBSTRING_INDEX(g.student_number, '/', 1),
                              SUBSTRING_INDEX(g.student_number, '/', -1)))
WHERE NOT (u.username <=> LOWER(CONCAT(SUBSTRING_INDEX(g.student_number, '/', 1),
                                       SUBSTRING_INDEX(g.student_number, '/', -1))));

UPDATE users u
JOIN admin a ON a.user_id = u.user_id
SET u.username = LOWER(CONCAT('AD', SUBSTRING_INDEX(a.staff_number, '/', -1)))
WHERE NOT (u.username <=> LOWER(CONCAT('AD', SUBSTRING_INDEX(a.staff_number, '/', -1))));

UPDATE users u
JOIN lecturer l ON l.user_id = u.user_id
SET u.username = LOWER(CONCAT('LE', SUBSTRING_INDEX(l.staff_number, '/', -1)))
WHERE NOT (u.username <=> LOWER(CONCAT('LE', SUBSTRING_INDEX(l.staff_number, '/', -1))));

UPDATE users u
JOIN technical_officer t ON t.user_id = u.user_id
SET u.username = LOWER(CONCAT('TO', SUBSTRING_INDEX(t.staff_number, '/', -1)))
WHERE NOT (u.username <=> LOWER(CONCAT('TO', SUBSTRING_INDEX(t.staff_number, '/', -1))));

-- ---------------------------------------------------------
-- Clean up
-- ---------------------------------------------------------
DROP TABLE IF EXISTS _fix_students;
DROP TABLE IF EXISTS _fix_student_new;

-- ---------------------------------------------------------
-- Verification (all three should return 0 rows / 0)
-- ---------------------------------------------------------
-- users without a username
SELECT COUNT(*) AS users_without_username FROM users WHERE username IS NULL;

-- student numbers reused across years
SELECT SUBSTRING_INDEX(student_number, '/', -1) AS num, COUNT(*) AS times_used
FROM undergraduate
GROUP BY SUBSTRING_INDEX(student_number, '/', -1)
HAVING COUNT(*) > 1;

-- full list for a visual check
SELECT u.user_id, u.user_type, u.username,
       COALESCE(g.student_number, a.staff_number, l.staff_number, t.staff_number) AS number,
       u.name
FROM users u
LEFT JOIN undergraduate     g ON g.user_id = u.user_id
LEFT JOIN admin             a ON a.user_id = u.user_id
LEFT JOIN lecturer          l ON l.user_id = u.user_id
LEFT JOIN technical_officer t ON t.user_id = u.user_id
ORDER BY u.user_type, u.username;
