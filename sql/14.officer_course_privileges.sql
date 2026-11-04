-- ============================================================================
-- FILE: 14.officer_course_privileges.sql
-- PURPOSE: Let the technical officer account schedule (add / edit / delete)
--          courses from the Officer "Courses" tab, and look up lecturers.
-- REQUIRES: 4.db_users.sql and 12.add_course_columns.sql already applied
-- ============================================================================

GRANT INSERT, UPDATE, DELETE ON `afms_db`.`course`   TO 'afms_officer'@'%';
GRANT SELECT                 ON `afms_db`.`lecturer` TO 'afms_officer'@'%';

FLUSH PRIVILEGES;
