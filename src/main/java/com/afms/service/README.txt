FOLDER: service/   Package: com.afms.service
Business logic only. No Swing, no SQL strings (call DAOs).

FILES TO CREATE
[14] EligibilityService.java
     adjustedAttendance(present, total, medicalCovered), rawAttendance(),
     isEligible(caAverage, adjustedAttendance): CA >= 40 AND attendance >= 80.
[15] GradeService.java
     validate(mark), moduleTotal(ca, final) = ca*0.4 + final*0.6,
     grade(total), gradePoint(grade), passed(grade), gpa(points, credits).
[20] AppSession.java
     Holds the logged-in User: set(), user(), id().
[21] AuthService.java
     login(email, password) returns the role-specific User, logout(),
     changePassword(id, current, new).
[26] AcademicService.java
     standing(student, offering), currentStandings(student),
     publish(offering) to calculate and store results,
     gpaSummary(student) with SGPA per semester + CGPA.
