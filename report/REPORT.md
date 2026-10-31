# AFMS – Academic & Faculty Management System

**Project report**

| | |
|---|---|
| Project | AFMS (Academic & Faculty Management System) |
| Type | Desktop application – Java Swing + JDBC |
| Database | MySQL / MariaDB (`afms_db`) |
| Team | _add names and student IDs_ |
| Date | _add date_ |

---

## 1. Introduction

AFMS is a desktop system for managing the academic side of a faculty: users, courses, attendance, medical submissions, assessments, results and notices. Four kinds of people use it, and each gets their own dashboard and their own level of database access:

- **Admin** – manages users, courses, notices and timetables.
- **Lecturer** – manages course material, assessments, sessions, attendance, exams, results and notices, and reviews medicals.
- **Technical Officer** – records attendance and processes medical submissions.
- **Undergraduate (student)** – views courses, attendance, marks, timetable and notices, and submits medicals.

## 2. Objectives

- Replace paper/spreadsheet handling of attendance, medicals and results with one system.
- Give each role only the screens and the database permissions its job needs.
- Keep a clear separation between the UI, the data-access layer and the database.

## 3. Technology

| Area | Choice |
|---|---|
| Language | Java 17 |
| UI | Swing (custom "Bright Glass Tech" light-blue glass theme) |
| Database access | JDBC with `mysql-connector-j` 9.5.0 |
| Database | MySQL 8 / MariaDB 10.4 (XAMPP in development) |
| Build | Maven (`pom.xml`) / IntelliJ IDEA |

## 4. System architecture

```
UI (src/UI)  ->  Data layer (src/data)  ->  Database (src/database)  ->  MySQL
```

| Package | Responsibility |
|---|---|
| `UI.Pages` | Login window and the four role dashboards (`AdminPages`, `LecturerPage`, `OfficerPages`, `StudentPages`), with one page class per tab in the `admin`, `lecturer`, `Officer` and `Student` sub-packages |
| `UI.Pages.Components` | Reusable Swing components (sidebar, cards, KPI cards, chips, tables, buttons) |
| `UI.Kit` | Theme: palette, backdrop, assets |
| `data` | One class per table group (`UserData`, `AdminData`, `LecturerData`, `AttendanceData`, `MedicalData`, `ResultData`, ...), all using prepared statements |
| `model` | Plain Java objects matching the tables (`User`, `Lecturer`, `Course`, `Medical`, ...) |
| `database` | `Database` – hands out connections |
| `Exception` | `DataAccessException` – wraps SQL errors |

**Login flow.** `Main` opens `LoginPage`. The user signs in with email and password, `UserData.login` finds the user, and `LoginPage.openDashboard` opens the dashboard for the user's `user_type`.

![Login page](screenshots/login.png)

## 5. Database design

The database `afms_db` is created by `sql/schema.sql`. Sample data is in `sql/mockData.sql`.

**Users and roles.** `users` holds what every account shares (name, email, password, phone, address, date of birth, `user_type`). Each role has a subtype table that shares its primary key with `users`: `admin`, `undergraduate`, `lecturer`, `technical_officer`.

**Academic structure.** `department`, `batch`, `course`, `semester`, `course_offering` (a course in a semester, taught by a lecturer), `enrollment`.

**Teaching and assessment.** `course_material`, `session`, `attend` (attendance per student per session), `assessment`, `exam`, `result`, `eligibility`, `repeat_student`.

**Other.** `timetable`, `timetable_entity`, `medical`, `notice`.

Relationships use foreign keys with `ON UPDATE CASCADE`, and `ON DELETE CASCADE` or `SET NULL` as appropriate. Check constraints guard dates, times and attendance percentage.

> _ER diagram: export one from IntelliJ (Database tool → Diagrams) or MySQL Workbench and save it as `screenshots/er-diagram.png`._

## 6. Security: one database account per role

Originally every action in the app connected to MySQL as `root`, which has full administrator rights. A bug or a misused screen could therefore damage any table or any other database on the server.

The app now connects with a separate least-privilege account for each role. `sql/db_users.sql` creates them, and `database/Database.java` chooses the account.

| Account | Used by | What it may do |
|---|---|---|
| `afms_login` | Login screen (before the user is known) | `SELECT` on `users` only |
| `afms_admin` | Admin | Select, insert, update, delete on all `afms_db` tables |
| `afms_lecturer` | Lecturer | Full CRUD on course material, assessments, sessions, attendance, exams, results, eligibility, notices. Select and update on medicals. Read-only on the rest. |
| `afms_officer` | Technical Officer | Full CRUD on attendance. Select, insert, update on medicals. Read-only on the rest. |
| `afms_student` | Student | Read-only on course and result data. Select and insert on medicals. |

Further rules:

- No account can drop, alter or create tables, grant privileges, or access another database.
- Lecturers, officers and students may edit only their own profile columns. `user_type` is excluded, so nobody can promote themselves to admin.
- The app starts on `afms_login`. After a successful login `Database.useRoleFor(userType)` switches to the matching account. Logging out opens a new `LoginPage`, which switches back to `afms_login`.
- Passwords in `db_users.sql` and `Database.java` are **development defaults**. They can be overridden with the environment variables `AFMS_DB_LOGIN_PASSWORD`, `AFMS_DB_ADMIN_PASSWORD`, `AFMS_DB_LECTURER_PASSWORD`, `AFMS_DB_OFFICER_PASSWORD` and `AFMS_DB_STUDENT_PASSWORD`.

**Limitations.**
- MySQL grants work per table and column, not per row. "A student sees only their own marks" is enforced by the queries in the application, not by the database.
- User passwords in `users.password` are compared as plain text. A hashing step is still to be done (`sql/fix_plaintext_passwords.sql` exists for the dev data).

## 7. Features by role

Describe the final state of each dashboard here and add a screenshot per tab.

### 7.1 Admin

| Tab | Purpose | Status |
|---|---|---|
| Overview | Live KPIs from the database (user counts, offerings, notices, medicals, attendance, timetable) | Connected to `AdminData` |
| User Profiles | Add, edit and delete users | Uses `UserData` |
| Courses | Course management | _update status_ |
| Notices | Notice management | _update status_ |
| Timetables | Timetable grid | _update status_ |
| My Profile | Edit own details | Uses `UserData` |

![Admin – Overview](screenshots/admin-overview.png)
![Admin – User Profiles](screenshots/admin-user-profiles.png)
![Admin – Courses](screenshots/admin-courses.png)
![Admin – Notices](screenshots/admin-notices.png)
![Admin – Timetables](screenshots/admin-timetables.png)
![Admin – My Profile](screenshots/admin-my-profile.png)

### 7.2 Lecturer

Tabs: Overview, My Courses, Marks, Undergraduates, Attendance, Grades & GPA, Medicals, Notices, My Profile.
The data layer (`LecturerData`) is complete; the pages are being connected to it. _Update status._

![Lecturer – Overview](screenshots/lecturer-overview.png)
![Lecturer – My Courses](screenshots/lecturer-my-courses.png)
![Lecturer – Marks](screenshots/lecturer-marks.png)
![Lecturer – Undergraduates](screenshots/lecturer-undergraduates.png)
![Lecturer – Attendance](screenshots/lecturer-attendance.png)
![Lecturer – Grades & GPA](screenshots/lecturer-grades-gpa.png)
![Lecturer – Medicals](screenshots/lecturer-medicals.png)
![Lecturer – Notices](screenshots/lecturer-notices.png)
![Lecturer – My Profile](screenshots/lecturer-my-profile.png)

### 7.3 Technical Officer

Tabs: Overview, Attendance Register, Medicals, Timetables, Notices, My Profile. _Update status._

![Officer – Overview](screenshots/officer-overview.png)
![Officer – Attendance Register](screenshots/officer-attendance-register.png)
![Officer – Medicals](screenshots/officer-medicals.png)
![Officer – Notices](screenshots/officer-notices.png)
![Officer – Timetables](screenshots/officer-timetables.png)
![Officer – My Profile](screenshots/officer-my-profile.png)

### 7.4 Student

Tabs: Overview, My Courses, My Attendance, Marks & GPA, Medicals, Notices, Timetables, My Profile. _Update status._

![Student – Overview](screenshots/student-overview.png)
![Student – My Courses](screenshots/student-my-courses.png)
![Student – My Attendance](screenshots/student-my-attendance.png)
![Student – Marks & GPA](screenshots/student-marks-gpa.png)
![Student – Medicals](screenshots/student-medicals.png)
![Student – Notices](screenshots/student-notices.png)
![Student – Timetables](screenshots/student-timetables.png)
![Student – My Profile](screenshots/student-my-profile.png)

## 8. User interface design

The UI uses a "Bright Glass Tech" theme: a light-blue glass look with a world-map backdrop, rounded cards, KPI tiles, chips for status values, and a left sidebar with the account card and logout button. Shared components live in `UI.Pages.Components` so every role looks consistent.

![Dashboard layout – sidebar, KPI cards and content card](screenshots/admin-overview.png)

## 9. Setup and running

1. Install JDK 17 and XAMPP (or MySQL 8).
2. Start MySQL.
3. Run `sql/schema.sql`, then `sql/mockData.sql`, then `sql/db_users.sql` (as an administrative user such as `root`).
4. Make sure `mysql-connector-j` is on the classpath (`lib/` or Maven).
5. Run `Main`.

Sample login: any email in `mockData.sql`, for example `kasun.wijesinghe@afms.example.com`, with password `Password@123` (all sample users share it).

## 10. Testing

_Describe how you tested: login for each role, CRUD on each page, and the permission checks._

Permission checks done on the role accounts:

| Check | Result |
|---|---|
| `afms_student` tries to update `result` | Denied |
| `afms_lecturer` tries to set `users.user_type` | Denied |
| `afms_login` tries to read `result` | Denied |
| Each role account can read `users` | Allowed |

## 11. Problems faced

- **Corrupted MariaDB privilege tables.** Granting database-wide rights crashed the XAMPP server (`#2006 server has gone away`, Aria "wrong checksum" errors on `mysql.db`). Cause: system tables damaged by unclean shutdowns. Fix: restore the clean `mysql.db` and `proxies_priv` files from XAMPP's `mysql/backup` folder, then re-run `db_users.sql`. _Update this section with the final outcome._
- **User-type mismatch.** The database stores `TECHNICAL_OFFICER` and `UNDERGRADUATE`, but the dashboard routing in `LoginPage.openDashboard` matches `officer` and `student`, so those users open the admin dashboard. _Update if fixed._

## 12. Future work

- Hash passwords (for example bcrypt) instead of storing plain text.
- Enforce row-level access (a student sees only their own data) with views or stricter queries.
- Finish connecting the lecturer, officer and student pages to the data layer.
- Reuse database connections with a pool instead of opening one per query.

## 13. Conclusion

_Write a short summary of what was achieved and learned._

## Appendix

- `docs/` – per-role task descriptions for connecting each UI to the data layer, UI redesign notes and asset list.
- `sql/` – `schema.sql`, `mockData.sql`, `db_users.sql`.
