FOLDER: sql/
Purpose: database script you can run manually in MySQL Workbench.

FILES TO CREATE
[2]  schema.sql
     CREATE TABLE statements only, one statement per block, in this order:
     departments, batches, users, semesters, courses, course_offerings,
     enrollments, timetables, timetable_entities, sessions, attend, medicals,
     assessments, assessment_marks, exams, results, repeat_students,
     course_materials, notices.
     Keep an identical copy in src/main/resources/schema.sql (the app reads it).
