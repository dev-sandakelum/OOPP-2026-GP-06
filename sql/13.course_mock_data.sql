-- ============================================================================
-- FILE: 13.course_mock_data.sql
-- PURPOSE: Add mock data to the new columns in the course table
-- NOTES: Each UPDATE fills course_type, lecturer_id, batch, gpa_type,
--        end_weight, ca_weight, and CA component weights.
--        Validation: end_weight + ca_weight = 100
--                    quiz_weight + assignment_weight + project_weight + mid_weight = ca_weight
-- ============================================================================

-- Course 1: ICT1113 - Introduction to Programming
UPDATE course SET
    course_type = 'Theory',
    lecturer_id = 3,  -- Dr. Ruwan Jayasinghe (ICT)
    batch = '2025/26',
    gpa_type = 'GPA',
    end_weight = 60,
    ca_weight = 40,
    quiz_weight = 10,
    assignment_weight = 15,
    project_weight = 15,
    mid_weight = 0
WHERE course_id = 1;

-- Course 2: ICT1123 - Discrete Mathematics
UPDATE course SET
    course_type = 'Theory',
    lecturer_id = 4,  -- Dr. Sanduni Rajapaksha (ICT)
    batch = '2025/26',
    gpa_type = 'GPA',
    end_weight = 70,
    ca_weight = 30,
    quiz_weight = 10,
    assignment_weight = 10,
    project_weight = 0,
    mid_weight = 10
WHERE course_id = 2;

-- Course 3: ICT1233 - Database Management Systems
UPDATE course SET
    course_type = 'Theory',
    lecturer_id = 5,  -- Chamara Gunawardena (ICT)
    batch = '2025/26',
    gpa_type = 'GPA',
    end_weight = 50,
    ca_weight = 50,
    quiz_weight = 10,
    assignment_weight = 15,
    project_weight = 20,
    mid_weight = 5
WHERE course_id = 3;

-- Course 4: ICT1223 - Object Oriented Programming
UPDATE course SET
    course_type = 'Theory',
    lecturer_id = 17,  -- Dr. Asela Ratnayake (ICT)
    batch = '2025/26',
    gpa_type = 'GPA',
    end_weight = 60,
    ca_weight = 40,
    quiz_weight = 10,
    assignment_weight = 20,
    project_weight = 0,
    mid_weight = 10
WHERE course_id = 4;

-- Course 5: ET1113 - Engineering Mathematics
UPDATE course SET
    course_type = 'Theory',
    lecturer_id = 6,  -- Nadeesha Silva (ET)
    batch = '2025/26',
    gpa_type = 'GPA',
    end_weight = 70,
    ca_weight = 30,
    quiz_weight = 15,
    assignment_weight = 10,
    project_weight = 0,
    mid_weight = 5
WHERE course_id = 5;

-- Course 6: ICT2113 - Computer Networks
UPDATE course SET
    course_type = 'Theory',
    lecturer_id = 18,  -- Dr. Kumari Abeysekara (ICT)
    batch = '2024/25',
    gpa_type = 'GPA',
    end_weight = 60,
    ca_weight = 40,
    quiz_weight = 10,
    assignment_weight = 15,
    project_weight = 10,
    mid_weight = 5
WHERE course_id = 6;

-- Course 7: ICT1213 - Web Technologies (Practical, 2 credits = NGPA)
UPDATE course SET
    course_type = 'Practical',
    lecturer_id = 3,  -- Dr. Ruwan Jayasinghe (ICT)
    batch = '2025/26',
    gpa_type = 'NGPA',
    end_weight = 30,
    ca_weight = 70,
    quiz_weight = 0,
    assignment_weight = 30,
    project_weight = 40,
    mid_weight = 0
WHERE course_id = 7;

-- Course 8: ICT2123 - Data Structures and Algorithms (Practical)
UPDATE course SET
    course_type = 'Practical',
    lecturer_id = 4,  -- Dr. Sanduni Rajapaksha (ICT)
    batch = '2024/25',
    gpa_type = 'GPA',
    end_weight = 40,
    ca_weight = 60,
    quiz_weight = 15,
    assignment_weight = 25,
    project_weight = 20,
    mid_weight = 0
WHERE course_id = 8;

-- Course 9: ICT2213 - Software Engineering
UPDATE course SET
    course_type = 'Theory',
    lecturer_id = 5,  -- Chamara Gunawardena (ICT)
    batch = '2024/25',
    gpa_type = 'GPA',
    end_weight = 50,
    ca_weight = 50,
    quiz_weight = 10,
    assignment_weight = 10,
    project_weight = 25,
    mid_weight = 5
WHERE course_id = 9;

-- Course 10: ICT2223 - Operating Systems
UPDATE course SET
    course_type = 'Theory',
    lecturer_id = 17,  -- Dr. Asela Ratnayake (ICT)
    batch = '2024/25',
    gpa_type = 'GPA',
    end_weight = 60,
    ca_weight = 40,
    quiz_weight = 0,
    assignment_weight = 20,
    project_weight = 15,
    mid_weight = 5
WHERE course_id = 10;

-- Course 11: ET1123 - Engineering Mechanics (2 credits = NGPA)
UPDATE course SET
    course_type = 'Theory',
    lecturer_id = 19,  -- Janaka Rathnayake (ET)
    batch = '2025/26',
    gpa_type = 'NGPA',
    end_weight = 70,
    ca_weight = 30,
    quiz_weight = 10,
    assignment_weight = 15,
    project_weight = 0,
    mid_weight = 5
WHERE course_id = 11;

-- Course 12: ET1213 - Electrical Circuits
UPDATE course SET
    course_type = 'Theory',
    lecturer_id = 6,  -- Nadeesha Silva (ET)
    batch = '2025/26',
    gpa_type = 'GPA',
    end_weight = 70,
    ca_weight = 30,
    quiz_weight = 10,
    assignment_weight = 10,
    project_weight = 10,
    mid_weight = 0
WHERE course_id = 12;

-- Course 13: BST1113 - Introduction to Plant Science
UPDATE course SET
    course_type = 'Theory',
    lecturer_id = 20,  -- Hiruni Jayawardena (BST)
    batch = '2025/26',
    gpa_type = 'GPA',
    end_weight = 60,
    ca_weight = 40,
    quiz_weight = 10,
    assignment_weight = 15,
    project_weight = 10,
    mid_weight = 5
WHERE course_id = 13;

-- Course 14: BST1123 - Biochemistry (2 credits = NGPA)
UPDATE course SET
    course_type = 'Theory',
    lecturer_id = 20,  -- Hiruni Jayawardena (BST)
    batch = '2025/26',
    gpa_type = 'NGPA',
    end_weight = 70,
    ca_weight = 30,
    quiz_weight = 0,
    assignment_weight = 15,
    project_weight = 10,
    mid_weight = 5
WHERE course_id = 14;

-- Course 15: BST1213 - Biostatistics
UPDATE course SET
    course_type = 'Theory',
    lecturer_id = 20,  -- Hiruni Jayawardena (BST)
    batch = '2025/26',
    gpa_type = 'GPA',
    end_weight = 60,
    ca_weight = 40,
    quiz_weight = 15,
    assignment_weight = 20,
    project_weight = 0,
    mid_weight = 5
WHERE course_id = 15;

-- Course 16: ICT3113 - Mobile Application Development (Practical)
UPDATE course SET
    course_type = 'Practical',
    lecturer_id = 18,  -- Dr. Kumari Abeysekara (ICT)
    batch = '2023/24',
    gpa_type = 'GPA',
    end_weight = 30,
    ca_weight = 70,
    quiz_weight = 10,
    assignment_weight = 25,
    project_weight = 35,
    mid_weight = 0
WHERE course_id = 16;

-- Course 17: ICT3123 - Information Security (2 credits = NGPA)
UPDATE course SET
    course_type = 'Theory',
    lecturer_id = 3,  -- Dr. Ruwan Jayasinghe (ICT)
    batch = '2023/24',
    gpa_type = 'NGPA',
    end_weight = 70,
    ca_weight = 30,
    quiz_weight = 10,
    assignment_weight = 15,
    project_weight = 5,
    mid_weight = 0
WHERE course_id = 17;

-- Course 18: ET2113 - Thermodynamics
UPDATE course SET
    course_type = 'Theory',
    lecturer_id = 19,  -- Janaka Rathnayake (ET)
    batch = '2024/25',
    gpa_type = 'GPA',
    end_weight = 70,
    ca_weight = 30,
    quiz_weight = 10,
    assignment_weight = 10,
    project_weight = 5,
    mid_weight = 5
WHERE course_id = 18;

-- Course 19: BST2113 - Soil Science
UPDATE course SET
    course_type = 'Theory',
    lecturer_id = 20,  -- Hiruni Jayawardena (BST)
    batch = '2024/25',
    gpa_type = 'GPA',
    end_weight = 60,
    ca_weight = 40,
    quiz_weight = 10,
    assignment_weight = 15,
    project_weight = 15,
    mid_weight = 0
WHERE course_id = 19;

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Check 1: Verify that weights sum correctly
-- end_weight + ca_weight should = 100
-- quiz_weight + assignment_weight + project_weight + mid_weight should = ca_weight
SELECT 
    course_id,
    course_code,
    end_weight + ca_weight AS total_main,
    quiz_weight + assignment_weight + project_weight + mid_weight AS total_ca,
    ca_weight
FROM course
ORDER BY course_id;

-- Check 2: View all course data
SELECT * FROM course ORDER BY course_id;
