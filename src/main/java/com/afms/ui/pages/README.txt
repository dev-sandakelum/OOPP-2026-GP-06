FOLDER: ui/pages/   Package: com.afms.ui.pages
One class per role plus shared pages. Each method returns a JComponent.

FILES TO CREATE
[41] CommonPages.java   notices, weekly timetable grid, profile + change password.
[42] AdminPages.java    users, courses, offerings, enrollments, semesters,
                        timetable slots, notices, repeat students (CRUD).
[43] OfficerPages.java  sessions and attendance roll call, medical review
                        (approve/reject) and record medical.
[44] LecturerPages.java my courses (eligibility table), marks & results
                        (enter marks, publish), materials.
[45] StudentPages.java  my courses, attendance, grades & GPA, medicals
                        (submit), materials.
[46] Dash.java          the four dashboards + shared layout helpers.
[47] Pages.java         dispatcher: build(user, menuName, navigator) returns
                        the right page for role + menu item.
                        (Create an empty placeholder version earlier so
                        MainFrame [40] compiles.)
