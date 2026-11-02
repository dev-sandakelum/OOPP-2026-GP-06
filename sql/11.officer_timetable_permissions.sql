-- =========================================================
-- AFMS - Let the Technical Officer DB account change the
--        weekly timetable slots.
--
-- WHY: The officer Timetables tab lets officers add, move
--      (day / time / room / type) and remove slots in
--      `timetable_entity`. The officer account only had
--      SELECT on the timetable tables, so every change failed.
--
-- HOW TO RUN: Execute as root / server admin against afms_db.
--             mysql -u root -p afms_db < 11.officer_timetable_permissions.sql
-- =========================================================

GRANT SELECT, INSERT, UPDATE, DELETE ON `afms_db`.`timetable_entity` TO 'afms_officer'@'%';

FLUSH PRIVILEGES;
