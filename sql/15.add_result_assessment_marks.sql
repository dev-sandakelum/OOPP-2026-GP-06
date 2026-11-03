-- =========================================================
-- Migration 10: Per-assessment mark columns on `result`
-- Run once against afms_db after migration 9.
-- Existing SQL files are NOT modified; this is ALTER-only.
--
-- PLAN
-- ----
-- 1. Marks live in `result` (one row per student + exam), which already
--    holds ca_result, final_exam_mark, total_mark.
-- 2. Add the individual components that make up the CA part:
--      quiz1_mark, quiz2_mark, quiz3_mark,
--      assignment1_mark, assignment2_mark,
--      project_mark, mid_mark
-- 3. "End" exam = existing `final_exam_mark` (no duplicate column added).
-- 4. All new columns are DECIMAL(5,2), NULL-able (NULL = not marked yet,
--    distinct from a real 0), CHECK-constrained to 0..100.
--    Each mark is stored as a raw score out of 100; weighting into
--    ca_result / total_mark is done by the application layer.
-- 5. Existing rows are untouched (new columns stay NULL), so current
--    ca_result / total_mark data and the Java code using SELECT * keep
--    working unchanged.
-- 6. Privileges: 4.db_users.sql already grants table-level access on
--    `result` (lecturer: CRUD, student: SELECT), which covers new
--    columns automatically. No GRANT changes needed.
--
-- FOLLOW-UP (Java, not part of this file)
-- ---------------------------------------
--   model/Result.java      add the 7 fields + getters/setters
--   data/ResultData.java   map() and INSERT ... ON DUPLICATE KEY UPDATE
--   UI Marks.java          add columns; compute ca_result from components
--   UI MarksGPA.java       show the breakdown to students
-- =========================================================

USE afms_db;

ALTER TABLE result
    ADD COLUMN quiz1_mark      DECIMAL(5,2) NULL AFTER exam_id,
    ADD COLUMN quiz2_mark      DECIMAL(5,2) NULL AFTER quiz1_mark,
    ADD COLUMN quiz3_mark      DECIMAL(5,2) NULL AFTER quiz2_mark,
    ADD COLUMN assignment1_mark DECIMAL(5,2) NULL AFTER quiz3_mark,
    ADD COLUMN assignment2_mark DECIMAL(5,2) NULL AFTER assignment1_mark,
    ADD COLUMN project_mark    DECIMAL(5,2) NULL AFTER assignment2_mark,
    ADD COLUMN mid_mark        DECIMAL(5,2) NULL AFTER project_mark;

ALTER TABLE result
    ADD CONSTRAINT chk_result_quiz1       CHECK (quiz1_mark       IS NULL OR quiz1_mark       BETWEEN 0 AND 100),
    ADD CONSTRAINT chk_result_quiz2       CHECK (quiz2_mark       IS NULL OR quiz2_mark       BETWEEN 0 AND 100),
    ADD CONSTRAINT chk_result_quiz3       CHECK (quiz3_mark       IS NULL OR quiz3_mark       BETWEEN 0 AND 100),
    ADD CONSTRAINT chk_result_assignment1 CHECK (assignment1_mark IS NULL OR assignment1_mark BETWEEN 0 AND 100),
    ADD CONSTRAINT chk_result_assignment2 CHECK (assignment2_mark IS NULL OR assignment2_mark BETWEEN 0 AND 100),
    ADD CONSTRAINT chk_result_project     CHECK (project_mark     IS NULL OR project_mark     BETWEEN 0 AND 100),
    ADD CONSTRAINT chk_result_mid         CHECK (mid_mark         IS NULL OR mid_mark         BETWEEN 0 AND 100);

COMMIT;

-- =========================================================
-- Migration 10 complete
-- Table altered : result (+7 columns, +7 CHECK constraints)
-- End exam mark : existing column final_exam_mark
-- =========================================================
