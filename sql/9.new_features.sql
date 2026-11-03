-- =========================================================
-- Migration 9: Assignment Submission + Student Email Grant
-- Run once against afms_db after migration 7.
--
-- Combines:
--   A. assignment_submission table (was 8.assignment_submission.sql)
--   B. Student profile email permission (was 7.student_profile_permissions.sql)
--   C. Role grants for assignment_submission (was 9.assignment_submission_grants.sql)
--   D. Undo of the email modification made to 4.db_users.sql
--      (that file is kept at its original state; the email
--       grant lives here instead)
-- =========================================================

USE afms_db;

-- =========================================================
-- A. Create assignment_submission table
-- =========================================================

CREATE TABLE IF NOT EXISTS assignment_submission (
    submission_id   INT AUTO_INCREMENT PRIMARY KEY,
    assessment_id   INT NOT NULL,
    student_id      INT NOT NULL,
    file_path       VARCHAR(500),
    submitted_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    status          VARCHAR(30) DEFAULT 'SUBMITTED',   -- SUBMITTED, LATE, GRADED
    remarks         VARCHAR(500),
    UNIQUE (assessment_id, student_id),                -- one submission per student per assessment
    CONSTRAINT fk_submission_assessment FOREIGN KEY (assessment_id)
        REFERENCES assessment(assessment_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_submission_student FOREIGN KEY (student_id)
        REFERENCES undergraduate(user_id) ON UPDATE CASCADE ON DELETE CASCADE
);

-- =========================================================
-- B. Student profile: add email to the UPDATE column grant
--    (4.db_users.sql originally only granted phone/address/password;
--     this GRANT adds email on top — MySQL merges column grants for
--     the same user/table so no REVOKE is needed)
-- =========================================================

GRANT UPDATE (`email`, `phone`, `address`, `password`)
    ON `afms_db`.`users`
    TO 'afms_student'@'%';

-- =========================================================
-- C. Role grants for assignment_submission
-- =========================================================

-- afms_student: submit own files and view own submission status
GRANT SELECT, INSERT, UPDATE
    ON `afms_db`.`assignment_submission`
    TO 'afms_student'@'%';

-- afms_lecturer: view all submissions, update status/remarks for grading
GRANT SELECT, UPDATE
    ON `afms_db`.`assignment_submission`
    TO 'afms_lecturer'@'%';

-- afms_admin: full CRUD for administrative oversight
GRANT SELECT, INSERT, UPDATE, DELETE
    ON `afms_db`.`assignment_submission`
    TO 'afms_admin'@'%';

-- =========================================================
-- Finalize
-- =========================================================

FLUSH PRIVILEGES;
COMMIT;

-- =========================================================
-- Migration 9 complete
-- Tables created : assignment_submission
-- Roles updated  : afms_student, afms_lecturer, afms_admin
-- =========================================================
