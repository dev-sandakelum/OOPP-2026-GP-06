-- =========================================================
-- AFMS – Grant notice INSERT / DELETE to the Technical
--        Officer DB account.
--
-- WHY: The officer role previously had only SELECT on
--      `notice`. The Notices UI allows officers to post
--      (INSERT) and delete their own notices, so the missing
--      write privileges caused every insert/delete to fail.
--
-- NOTE: Officers cannot UPDATE notices (edit feature not
--       implemented in the UI). If you add an edit button
--       later, add UPDATE permission here as well.
--
-- HOW TO RUN: Execute as root (or any account with GRANT
--             OPTION) against afms_db.
--             e.g. in phpMyAdmin → SQL tab, or via CLI:
--             mysql -u root -p afms_db < 9.officer_notice_permissions.sql
-- =========================================================

-- Grant INSERT and DELETE on notice to the officer account
GRANT SELECT, INSERT, DELETE ON `afms_db`.`notice` TO 'afms_officer'@'%';

-- Reload the privilege tables so the change takes effect immediately
FLUSH PRIVILEGES;

-- =========================================================
-- Note: If you want officers to be able to EDIT notices,
--       run this additional command:
--       GRANT UPDATE ON `afms_db`.`notice` TO 'afms_officer'@'%';
-- =========================================================
