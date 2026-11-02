-- =========================================================
-- AFMS – Let any user (admin, lecturer, technical officer)
--        be the author of a notice.
--
-- WHY: notice.created_by had a foreign key to admin(user_id).
--      Lecturers and technical officers are not rows in the
--      admin table, so every notice they tried to post was
--      rejected with:
--        "Cannot add or update a child row: a foreign key
--         constraint fails (fk_notice_admin)"
--      The Notices UI for officers and lecturers posts with
--      created_by = their own user_id, so the key must point
--      at users(user_id) instead.
--
-- SAFE: Every existing created_by value is an admin user_id,
--       and every admin row also exists in users, so no data
--       is lost or rejected by the new constraint.
--
-- HOW TO RUN: Execute as the server admin account (one that
--             has ALTER on afms_db). afms_admin only has
--             SELECT/INSERT/UPDATE/DELETE, so it cannot run this.
--             e.g. mysql -u <root> -p afms_db < 10.notice_created_by_users.sql
-- =========================================================

-- 1. Drop the admin-only foreign key
ALTER TABLE `afms_db`.`notice` DROP FOREIGN KEY `fk_notice_admin`;

-- 2. Rename the supporting index to match the new constraint
ALTER TABLE `afms_db`.`notice` RENAME INDEX `fk_notice_admin` TO `fk_notice_user`;

-- 3. Point created_by at users(user_id)
ALTER TABLE `afms_db`.`notice`
    ADD CONSTRAINT `fk_notice_user` FOREIGN KEY (`created_by`)
        REFERENCES `afms_db`.`users` (`user_id`)
        ON UPDATE CASCADE ON DELETE SET NULL;

-- 4. Make sure the officer account can post and delete notices
--    (same as 9.officer_notice_permissions.sql; harmless to repeat)
GRANT SELECT, INSERT, DELETE ON `afms_db`.`notice` TO 'afms_officer'@'%';
FLUSH PRIVILEGES;
