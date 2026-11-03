-- ============================================================================
-- FILE: 12.add_course_columns.sql
-- PURPOSE: Add new columns to the existing course table for course type,
--          lecturer assignment, batch tracking, GPA type, and weight distribution
-- REQUIREMENTS: Table 'course' must already exist with data
-- ============================================================================

-- Add course_type column (Theory or Practical)
ALTER TABLE course
ADD COLUMN course_type ENUM('Theory', 'Practical') NULL
COMMENT 'Type of course: Theory or Practical';

-- Add lecturer_id column (foreign key to lecturer table)
ALTER TABLE course
ADD COLUMN lecturer_id INT NULL
COMMENT 'Lecturer assigned to this course';

-- Add batch column (e.g., '2023/24')
ALTER TABLE course
ADD COLUMN batch VARCHAR(10) NULL
COMMENT 'Academic batch/year for this course offering';

-- Add gpa_type column (GPA or NGPA)
ALTER TABLE course
ADD COLUMN gpa_type ENUM('GPA', 'NGPA') NULL
COMMENT 'Whether course counts toward GPA or is Non-GPA';

-- Add end_weight column (end exam percentage)
ALTER TABLE course
ADD COLUMN end_weight TINYINT UNSIGNED NULL
COMMENT 'End examination weight as percentage (0-100)';

-- Add ca_weight column (continuous assessment percentage)
ALTER TABLE course
ADD COLUMN ca_weight TINYINT UNSIGNED NULL
COMMENT 'Continuous assessment weight as percentage (0-100)';

-- Add quiz_weight column (quiz percentage, defaults to 0)
ALTER TABLE course
ADD COLUMN quiz_weight TINYINT UNSIGNED DEFAULT 0
COMMENT 'Quiz weight as percentage (0-100)';

-- Add assignment_weight column (assignment percentage, defaults to 0)
ALTER TABLE course
ADD COLUMN assignment_weight TINYINT UNSIGNED DEFAULT 0
COMMENT 'Assignment weight as percentage (0-100)';

-- Add project_weight column (project percentage, defaults to 0)
ALTER TABLE course
ADD COLUMN project_weight TINYINT UNSIGNED DEFAULT 0
COMMENT 'Project weight as percentage (0-100)';

-- Add mid_weight column (mid-term exam percentage, defaults to 0)
ALTER TABLE course
ADD COLUMN mid_weight TINYINT UNSIGNED DEFAULT 0
COMMENT 'Mid-term examination weight as percentage (0-100)';

-- Add foreign key constraint linking lecturer_id to lecturer table
ALTER TABLE course
ADD CONSTRAINT fk_course_lecturer
FOREIGN KEY (lecturer_id) REFERENCES lecturer(user_id)
ON DELETE SET NULL
ON UPDATE CASCADE;

-- ============================================================================
-- VERIFICATION: Display first 5 rows to confirm the new columns
-- ============================================================================
SELECT * FROM course LIMIT 5;
