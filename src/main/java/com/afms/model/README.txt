FOLDER: model/   Package: com.afms.model

FILES TO CREATE
[6]  User.java              abstract base: id, regNo, name, email, contact, dob,
                            address, gender; abstract getRole(), getRoleLabel(),
                            getMenu(); updateProfile(); static factory fromRow().
[7]  Admin.java             extends User; menu: Dashboard, Users, Courses,
                            Offerings, Enrollments, Semesters, Timetable,
                            Notices, Repeat Students, Profile.
[8]  Lecturer.java          extends User; adds qualification; menu: Dashboard,
                            My Courses, Marks & Results, Materials, Timetable,
                            Notices, Profile.
[9]  TechnicalOfficer.java  extends User; menu: Dashboard, Sessions &
                            Attendance, Medicals, Timetable, Notices, Profile.
[10] Undergraduate.java     extends User; menu: Dashboard, My Courses,
                            Attendance, Grades & GPA, Medicals, Materials,
                            Timetable, Notices, Profile.
[11] Item.java              record (long id, String label); toString returns
                            label. Used to fill combo boxes.
