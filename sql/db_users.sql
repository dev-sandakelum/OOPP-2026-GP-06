-- =========================================================
-- AFMS database accounts (least privilege)
-- DBMS: MySQL 8.x
--
-- Run ONCE as root (or any account with CREATE USER / GRANT), AFTER schema.sql:
--     mysql -u root -p < sql/db_users.sql
--
-- Re-running is safe: accounts are recreated with fresh grants.
-- schema.sql drops afms_db but not these accounts, so re-run this script
-- after every schema.sql run to be safe.
--
-- !! The passwords below are DEVELOPMENT defaults and match src/database/Database.java.
-- !! For anything beyond local use, change them here and set the matching
-- !! AFMS_DB_<ROLE>_PASSWORD environment variables for the app.
--
-- None of these accounts get DROP, ALTER, CREATE, GRANT OPTION or access to other
-- databases. MySQL grants are per table/column, not per row: "a student can only
-- see their own marks" must still be enforced by the app's WHERE clauses.
-- =========================================================

DROP USER IF EXISTS 'afms_login'@'localhost';
DROP USER IF EXISTS 'afms_admin'@'localhost';
DROP USER IF EXISTS 'afms_lecturer'@'localhost';
DROP USER IF EXISTS 'afms_officer'@'localhost';
DROP USER IF EXISTS 'afms_student'@'localhost';

CREATE USER 'afms_login'@'localhost'    IDENTIFIED BY 'login_dev_pw';
CREATE USER 'afms_admin'@'localhost'    IDENTIFIED BY 'admin_dev_pw';
CREATE USER 'afms_lecturer'@'localhost' IDENTIFIED BY 'lecturer_dev_pw';
CREATE USER 'afms_officer'@'localhost'  IDENTIFIED BY 'officer_dev_pw';
CREATE USER 'afms_student'@'localhost'  IDENTIFIED BY 'student_dev_pw';

-- ---------------------------------------------------------
-- afms_login: used only on the login screen, before we know who the user is.
-- Can look up a user by email + password and nothing else.
-- ---------------------------------------------------------
GRANT SELECT ON afms_db.users TO 'afms_login'@'localhost';

-- ---------------------------------------------------------
-- afms_admin: full data access (CRUD on every table), no schema changes.
-- ---------------------------------------------------------
GRANT SELECT, INSERT, UPDATE, DELETE ON afms_db.* TO 'afms_admin'@'localhost';

-- ---------------------------------------------------------
-- afms_lecturer: manages teaching data (materials, assessments, sessions,
-- attendance, exams, results, eligibility, notices), reviews medicals.
-- ---------------------------------------------------------
GRANT SELECT ON afms_db.users             TO 'afms_lecturer'@'localhost';
GRANT SELECT ON afms_db.department        TO 'afms_lecturer'@'localhost';
GRANT SELECT ON afms_db.batch             TO 'afms_lecturer'@'localhost';
GRANT SELECT ON afms_db.semester          TO 'afms_lecturer'@'localhost';
GRANT SELECT ON afms_db.course            TO 'afms_lecturer'@'localhost';
GRANT SELECT ON afms_db.course_offering   TO 'afms_lecturer'@'localhost';
GRANT SELECT ON afms_db.undergraduate     TO 'afms_lecturer'@'localhost';
GRANT SELECT ON afms_db.lecturer          TO 'afms_lecturer'@'localhost';
GRANT SELECT ON afms_db.enrollment        TO 'afms_lecturer'@'localhost';
GRANT SELECT ON afms_db.timetable         TO 'afms_lecturer'@'localhost';
GRANT SELECT ON afms_db.timetable_entity  TO 'afms_lecturer'@'localhost';
GRANT SELECT ON afms_db.repeat_student    TO 'afms_lecturer'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON afms_db.course_material TO 'afms_lecturer'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON afms_db.assessment      TO 'afms_lecturer'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON afms_db.`session`       TO 'afms_lecturer'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON afms_db.attend          TO 'afms_lecturer'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON afms_db.exam            TO 'afms_lecturer'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON afms_db.result          TO 'afms_lecturer'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON afms_db.eligibility     TO 'afms_lecturer'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON afms_db.notice          TO 'afms_lecturer'@'localhost';
GRANT SELECT, UPDATE                 ON afms_db.medical         TO 'afms_lecturer'@'localhost';
-- own profile only: no user_type, so a lecturer can't promote themselves to ADMIN
GRANT UPDATE (name, email, phone, address, dob, password) ON afms_db.users    TO 'afms_lecturer'@'localhost';
GRANT UPDATE (qualification)                              ON afms_db.lecturer TO 'afms_lecturer'@'localhost';

-- ---------------------------------------------------------
-- afms_officer (TECHNICAL_OFFICER): records attendance and processes medicals.
-- Read-only on everything else it needs to show.
-- ---------------------------------------------------------
GRANT SELECT ON afms_db.users             TO 'afms_officer'@'localhost';
GRANT SELECT ON afms_db.department        TO 'afms_officer'@'localhost';
GRANT SELECT ON afms_db.batch             TO 'afms_officer'@'localhost';
GRANT SELECT ON afms_db.semester          TO 'afms_officer'@'localhost';
GRANT SELECT ON afms_db.course            TO 'afms_officer'@'localhost';
GRANT SELECT ON afms_db.course_offering   TO 'afms_officer'@'localhost';
GRANT SELECT ON afms_db.undergraduate     TO 'afms_officer'@'localhost';
GRANT SELECT ON afms_db.technical_officer TO 'afms_officer'@'localhost';
GRANT SELECT ON afms_db.enrollment        TO 'afms_officer'@'localhost';
GRANT SELECT ON afms_db.`session`         TO 'afms_officer'@'localhost';
GRANT SELECT ON afms_db.timetable         TO 'afms_officer'@'localhost';
GRANT SELECT ON afms_db.timetable_entity  TO 'afms_officer'@'localhost';
GRANT SELECT ON afms_db.notice            TO 'afms_officer'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON afms_db.attend  TO 'afms_officer'@'localhost';
GRANT SELECT, INSERT, UPDATE         ON afms_db.medical TO 'afms_officer'@'localhost';
GRANT UPDATE (name, email, phone, address, dob, password) ON afms_db.users TO 'afms_officer'@'localhost';

-- ---------------------------------------------------------
-- afms_student (UNDERGRADUATE): reads course/result data, submits medicals,
-- edits own contact details. Cannot write marks, attendance or notices.
-- ---------------------------------------------------------
GRANT SELECT ON afms_db.users             TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.department        TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.batch             TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.semester          TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.course            TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.course_offering   TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.course_material   TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.undergraduate     TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.lecturer          TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.enrollment        TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.`session`         TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.attend            TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.assessment        TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.exam              TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.result            TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.eligibility       TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.repeat_student    TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.timetable         TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.timetable_entity  TO 'afms_student'@'localhost';
GRANT SELECT ON afms_db.notice            TO 'afms_student'@'localhost';
GRANT SELECT, INSERT ON afms_db.medical   TO 'afms_student'@'localhost';
GRANT UPDATE (phone, address, password) ON afms_db.users TO 'afms_student'@'localhost';
GRANT UPDATE (profile_picture)          ON afms_db.undergraduate TO 'afms_student'@'localhost';

FLUSH PRIVILEGES;
