-- Run once on an existing database.
-- Lets lecturers (and any other user) be the creator of a notice, not only admins.
ALTER TABLE notice DROP FOREIGN KEY fk_notice_admin;
ALTER TABLE notice
    ADD CONSTRAINT fk_notice_creator FOREIGN KEY (created_by)
        REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE SET NULL;
