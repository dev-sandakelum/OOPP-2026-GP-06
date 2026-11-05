-- Lets a lecturer schedule when a lecture material is visible to students.
-- available_from  NULL = visible right away
-- available_until NULL = visible any time (no end date)

USE afms_db;

ALTER TABLE course_material
    ADD COLUMN available_from  DATE NULL,
    ADD COLUMN available_until DATE NULL;
