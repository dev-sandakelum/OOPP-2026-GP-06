FOLDER: dao/   Package: com.afms.dao
One class per data area. SQL lives here and only here. Use Db helper and
PreparedStatement parameters.

FILES TO CREATE
[19] UserDAO.java        authRow(email), list, get, create, update, delete,
                         updateProfile, password hash get/set, combo item lists,
                         departments and batches.
[22] AcademicDAO.java    semesters, courses, course offerings, enrollments
                         (single + whole batch), repeat students.
[23] AttendanceDAO.java  sessions, roll call, save attendance, counts for a
                         student, attendance log, medicals (list, submit, review).
[24] AssessmentDAO.java  assessments, CA marks, final exam marks, CA average,
                         store/publish results, published results per student.
[25] ContentDAO.java     notices, course materials, timetable entries.
[28] StatsDAO.java       counts and chart data for the dashboards.
