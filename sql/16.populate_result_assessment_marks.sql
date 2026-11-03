-- =========================================================
-- Migration 11: Back-fill per-assessment marks for existing results
-- Run once against afms_db AFTER 10.add_result_assessment_marks.sql.
-- Existing SQL files are NOT modified; this is UPDATE-only.
--
-- HOW THE MOCK VALUES ARE GENERATED
-- ---------------------------------
-- * Base score  = ca_result * 2, capped at 100  (ca_result is out of 50,
--                 new columns are out of 100), so each student's
--                 components stay in line with their existing CA.
-- * Variation   = a deterministic offset of -10..+10 derived from
--                 student_id, exam_id and a per-column seed, so every
--                 component differs but re-running gives the same data.
-- * Rounded to the nearest 0.5 and clamped to 0..100.
-- * Only rows where the column IS NULL and ca_result IS NOT NULL are
--   touched, so the script is safe to re-run and never overwrites
--   marks entered later by a lecturer.
-- * ca_result, final_exam_mark ("end" mark) and total_mark are NOT changed.
-- =========================================================

USE afms_db;

UPDATE result
SET
    quiz1_mark       = COALESCE(quiz1_mark,
        LEAST(100, GREATEST(0, ROUND((LEAST(100, ca_result * 2) + (MOD(student_id * 7 + exam_id * 3 + 1, 21) - 10)) * 2) / 2))),
    quiz2_mark       = COALESCE(quiz2_mark,
        LEAST(100, GREATEST(0, ROUND((LEAST(100, ca_result * 2) + (MOD(student_id * 7 + exam_id * 3 + 2, 21) - 10)) * 2) / 2))),
    quiz3_mark       = COALESCE(quiz3_mark,
        LEAST(100, GREATEST(0, ROUND((LEAST(100, ca_result * 2) + (MOD(student_id * 7 + exam_id * 3 + 3, 21) - 10)) * 2) / 2))),
    assignment1_mark = COALESCE(assignment1_mark,
        LEAST(100, GREATEST(0, ROUND((LEAST(100, ca_result * 2) + (MOD(student_id * 7 + exam_id * 3 + 4, 21) - 10)) * 2) / 2))),
    assignment2_mark = COALESCE(assignment2_mark,
        LEAST(100, GREATEST(0, ROUND((LEAST(100, ca_result * 2) + (MOD(student_id * 7 + exam_id * 3 + 5, 21) - 10)) * 2) / 2))),
    project_mark     = COALESCE(project_mark,
        LEAST(100, GREATEST(0, ROUND((LEAST(100, ca_result * 2) + (MOD(student_id * 7 + exam_id * 3 + 6, 21) - 10)) * 2) / 2))),
    mid_mark         = COALESCE(mid_mark,
        LEAST(100, GREATEST(0, ROUND((LEAST(100, ca_result * 2) + (MOD(student_id * 7 + exam_id * 3 + 7, 21) - 10)) * 2) / 2)))
WHERE ca_result IS NOT NULL;

COMMIT;

-- ---------------------------------------------------------
-- Sanity check (read-only): expect 0 rows still missing marks
-- ---------------------------------------------------------
-- SELECT COUNT(*) AS rows_missing_marks
-- FROM result
-- WHERE ca_result IS NOT NULL
--   AND (quiz1_mark IS NULL OR quiz2_mark IS NULL OR quiz3_mark IS NULL
--     OR assignment1_mark IS NULL OR assignment2_mark IS NULL
--     OR project_mark IS NULL OR mid_mark IS NULL);

-- =========================================================
-- Migration 11 complete
-- Table updated : result (quiz1-3, assignment1-2, project, mid)
-- =========================================================
