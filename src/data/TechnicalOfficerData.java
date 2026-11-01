package data;

import database.Database;
import model.Attendance;
import model.Notice;
import model.Session;
import model.TimetableEntity;
import model.TechnicalOfficer;
import Exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TechnicalOfficerData {

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    // 1. Find technical officer by user ID
    public TechnicalOfficer findById(int userId) {

        String sql = "SELECT users.user_id, users.name, users.email, " +
                "users.password, users.phone, users.address, " +
                "users.dob, users.gender, users.user_type, " +
                "technical_officer.dep_id " +
                "FROM technical_officer JOIN users USING(user_id) " +
                "WHERE technical_officer.user_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not load technical officer " + userId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 2. Find all technical officers
    public List<TechnicalOfficer> findAll() {

        String sql = "SELECT users.user_id, users.name, users.email, " +
                "users.password, users.phone, users.address, " +
                "users.dob, users.gender, users.user_type, " +
                "technical_officer.dep_id " +
                "FROM technical_officer JOIN users USING(user_id) " +
                "ORDER BY users.user_id";

        List<TechnicalOfficer> officers = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                officers.add(map(rs));
            }

            return officers;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not load technical officers", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 3. Insert technical officer
    public TechnicalOfficer insert(TechnicalOfficer t) {

        String sql = "INSERT INTO technical_officer(user_id, dep_id) " +
                "VALUES (?, ?)";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, t.getUser_id());
            ps.setInt(2, t.getDep_id());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                return t;
            }

            throw new SQLException("Technical officer insert failed.");

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not insert technical officer " + t.getUser_id(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 4. Update technical officer's own profile.
    public boolean update(TechnicalOfficer t) {

        String sql = "UPDATE users SET name = ?, email = ?, phone = ?, address = ?, dob = ? " +
                "WHERE user_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, t.getName());
            ps.setString(2, t.getEmail());
            ps.setString(3, t.getContact_number());
            ps.setString(4, t.getAddress());
            if (t.getDOB() != null) ps.setDate(5, t.getDOB());
            else ps.setNull(5, java.sql.Types.DATE);
            ps.setInt(6, t.getUser_id());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not update technical officer " + t.getUser_id() + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Alias for update(TechnicalOfficer)
    public boolean updateProfile(TechnicalOfficer officer) {
        return update(officer);
    }

    // 4b. Change the department (admin-side operation)
    public boolean updateDepartment(TechnicalOfficer t) {

        String sql = "UPDATE technical_officer SET dep_id = ? WHERE user_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, t.getDep_id());
            ps.setInt(2, t.getUser_id());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not update technical officer " + t.getUser_id(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 5. Delete technical officer
    public boolean delete(int userId) {

        String sql = "DELETE FROM technical_officer WHERE user_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not delete technical officer " + userId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // =====================================================================
    //  OVERVIEW
    // =====================================================================

    /** Count of all enrolled students. */
    public int countTotalStudents() {
        return scalarInt("SELECT COUNT(*) FROM undergraduate");
    }

    /** Count of sessions recorded today. */
    public int countSessionsToday() {
        return scalarInt("SELECT COUNT(*) FROM `session` WHERE session_date = CURDATE()");
    }

    /** Count of pending medicals. */
    public int countPendingMedicals() {
        return scalarInt("SELECT COUNT(*) FROM medical WHERE status = 'PENDING'");
    }

    /** Courses offered in the ONGOING semester (falls back to the latest semester that has offerings). */
    public int countCurrentCourses() {
        return scalarInt("SELECT COUNT(*) FROM course_offering WHERE semester_id = COALESCE("
                + "(SELECT MAX(semester_id) FROM semester WHERE status = 'ONGOING'), "
                + "(SELECT MAX(semester_id) FROM course_offering))");
    }

    /** {offering_id, course_code, course_name, sessions_count, avg_attendance%}  ("-" if none recorded) */
    public List<String[]> getCourseAttendanceSummary() {

        String sql = "SELECT o.offering_id, c.course_code, c.course_name, " +
                "(SELECT COUNT(*) FROM `session` s WHERE s.offering_id = o.offering_id) AS sessions_count, " +
                "(SELECT ROUND(SUM(a.attend_status = 'PRESENT') * 100.0 / COUNT(*), 1) " +
                "   FROM attend a JOIN `session` s ON s.session_id = a.session_id " +
                "  WHERE s.offering_id = o.offering_id) AS avg_attendance " +
                "FROM course_offering o JOIN course c ON c.course_id = o.course_id " +
                "ORDER BY o.semester_id DESC, c.course_code";

        List<String[]> rows = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                double avg = rs.getDouble("avg_attendance");
                boolean none = rs.wasNull();
                rows.add(new String[]{
                        String.valueOf(rs.getInt("offering_id")),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        String.valueOf(rs.getInt("sessions_count")),
                        none ? "-" : avg + "%"
                });
            }
            return rows;

        } catch (SQLException e) {
            throw new DataAccessException("Could not load course attendance summary", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Active timetable slots returned as TimetableEntity objects.
     */
    public List<TimetableEntity> getActiveTimetableEntities() {
        String sql = "SELECT te.* FROM timetable_entity te " +
                "JOIN timetable t ON t.timetable_id = te.timetable_id " +
                "WHERE t.timetable_id = (SELECT MAX(timetable_id) FROM timetable WHERE status IN ('ACTIVE', 'PUBLISHED')) " +
                "ORDER BY te.start_time";

        List<TimetableEntity> list = new ArrayList<>();
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TimetableEntity te = new TimetableEntity();
                te.setTimetable_id(rs.getInt("timetable_id"));
                te.setOffering_id(rs.getInt("offering_id"));
                te.setDay(rs.getString("day"));
                te.setStart_time(rs.getTime("start_time"));
                te.setEnd_time(rs.getTime("end_time"));
                te.setLecture_hall(rs.getString("lecture_hall"));
                list.add(te);
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Could not load timetable entities", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Map of offering_id -> course_code for active course offerings.
     */
    public Map<Integer, String> getOfferingCodes() {
        String sql = "SELECT o.offering_id, c.course_code " +
                "FROM course_offering o " +
                "JOIN course c ON c.course_id = o.course_id";

        Map<Integer, String> map = new HashMap<>();
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getInt("offering_id"), rs.getString("course_code"));
            }
            return map;
        } catch (SQLException e) {
            throw new DataAccessException("Could not load offering codes", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // =====================================================================
    //  ATTENDANCE REGISTER
    // =====================================================================

    /** Full register for one session: {student_id, name, batch, attend_status}  ("" if unsaved) */
    public List<String[]> getSessionRegister(int sessionId) {

        String sql = "SELECT u.user_id, u.name, b.batch_name, a.attend_status " +
                "FROM `session` s " +
                "JOIN enrollment e ON e.offering_id = s.offering_id " +
                "JOIN users u ON u.user_id = e.student_id " +
                "LEFT JOIN undergraduate g ON g.user_id = u.user_id " +
                "LEFT JOIN batch b ON b.batch_id = g.batch_id " +
                "LEFT JOIN attend a ON a.session_id = s.session_id AND a.student_id = u.user_id " +
                "WHERE s.session_id = ? " +
                "ORDER BY u.user_id";

        List<String[]> rows = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                            String.valueOf(rs.getInt("user_id")),
                            rs.getString("name"),
                            nz(rs.getString("batch_name")),
                            nz(rs.getString("attend_status"))
                    });
                }
            }
            return rows;

        } catch (SQLException e) {
            throw new DataAccessException("Could not load register for session " + sessionId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** {offering_id, "CODE - Course name"} for the course drop-down */
    public List<String[]> getOfferingChoices() {

        String sql = "SELECT o.offering_id, c.course_code, c.course_name " +
                "FROM course_offering o JOIN course c ON c.course_id = o.course_id " +
                "ORDER BY o.semester_id DESC, c.course_code";

        List<String[]> rows = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new String[]{
                        String.valueOf(rs.getInt("offering_id")),
                        rs.getString("course_code") + " - " + rs.getString("course_name")
                });
            }
            return rows;

        } catch (SQLException e) {
            throw new DataAccessException("Could not load courses", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Sessions of one offering on one date (yyyy-MM-dd), earliest first. */
    public List<Session> getSessions(int offeringId, String date) {

        String sql = "SELECT * FROM `session` WHERE offering_id = ? AND session_date = ? " +
                "ORDER BY start_time, session_number";

        List<Session> list = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, offeringId);
            ps.setDate(2, java.sql.Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Session(
                            rs.getInt("session_id"),
                            rs.getInt("offering_id"),
                            rs.getInt("session_number"),
                            rs.getString("session_type"),
                            rs.getDate("session_date"),
                            rs.getTime("start_time"),
                            rs.getTime("end_time"),
                            rs.getString("room")));
                }
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Could not load sessions for offering " + offeringId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Inserts a session. session_number 0 = next free number for the offering. */
    public Session insertSession(Session s) {

        if (s.getSessionNumber() <= 0) {
            s.setSessionNumber(scalarInt(
                    "SELECT COALESCE(MAX(session_number), 0) + 1 FROM `session` WHERE offering_id = "
                            + s.getOfferingId()));
        }
        return new SessionData().insert(s);
    }

    /** Deletes a session (its attendance rows go with it: ON DELETE CASCADE). */
    public boolean deleteSession(int sessionId) {
        return new SessionData().delete(sessionId);
    }

    /** Students enrolled in an offering: {student_id, name, batch} */
    public List<String[]> getEnrolledStudents(int offeringId) {

        String sql = "SELECT u.user_id, u.name, b.batch_name " +
                "FROM enrollment e " +
                "JOIN users u ON u.user_id = e.student_id " +
                "LEFT JOIN undergraduate g ON g.user_id = u.user_id " +
                "LEFT JOIN batch b ON b.batch_id = g.batch_id " +
                "WHERE e.offering_id = ? " +
                "ORDER BY u.user_id";

        List<String[]> rows = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, offeringId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                            String.valueOf(rs.getInt("user_id")),
                            rs.getString("name"),
                            nz(rs.getString("batch_name"))
                    });
                }
            }
            return rows;

        } catch (SQLException e) {
            throw new DataAccessException("Could not load students for offering " + offeringId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Saved marks of one session: student_id -> attend_status. */
    public Map<Integer, String> getAttendance(int sessionId) {
        Map<Integer, String> saved = new HashMap<>();
        for (Attendance a : new AttendanceData().findBySession(sessionId)) {
            saved.put(a.getStudentId(), a.getAttendStatus());
        }
        return saved;
    }

    /** Saves (insert or update) one student's mark. Recorded date = today. */
    public boolean saveAttendance(int sessionId, int studentId, String status) {
        return new AttendanceData().save(new Attendance(
                studentId, sessionId, status, new java.sql.Date(System.currentTimeMillis())));
    }

    // =====================================================================
    //  MEDICALS
    // =====================================================================

    /** user_id -> name for every undergraduate. */
    public Map<Integer, String> getStudentNames() {

        String sql = "SELECT u.user_id, u.name FROM users u " +
                "JOIN undergraduate g ON g.user_id = u.user_id";

        Map<Integer, String> names = new HashMap<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                names.put(rs.getInt("user_id"), rs.getString("name"));
            }
            return names;

        } catch (SQLException e) {
            throw new DataAccessException("Could not load student names", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // =====================================================================
    //  NOTICES
    // =====================================================================

    /** Notices for staff / technical officers / everyone, plus the officer's own. Newest first. */
    public List<Notice> getNotices(int officerId) {

        String sql = "SELECT notice_id, created_by, title, description, created_date, " +
                "audience, expiry_date, status FROM notice " +
                "WHERE (audience IN ('STAFF', 'ALL', 'TECHNICAL_OFFICER') OR created_by = ?) " +
                "AND (status IS NULL OR status <> 'DRAFT' OR created_by = ?) " +
                "ORDER BY created_date DESC";

        List<Notice> list = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, officerId);
            ps.setInt(2, officerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notice n = new Notice();
                    n.setNotice_id(rs.getInt("notice_id"));
                    int by = rs.getInt("created_by");
                    n.setCreated_by(rs.wasNull() ? null : by);
                    n.setTitle(rs.getString("title"));
                    n.setDescription(rs.getString("description"));
                    n.setCreated_date(rs.getTimestamp("created_date"));
                    n.setAudience(rs.getString("audience"));
                    n.setExpiry_date(rs.getDate("expiry_date"));
                    n.setStatus(rs.getString("status"));
                    list.add(n);
                }
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Could not load notices", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Posts a notice (created_by = the officer's user id). */
    public Notice insertNotice(Notice n) {
        return new NoticeData().insert(n);
    }

    /** Deletes a notice, but only if this officer posted it. */
    public boolean deleteNotice(int noticeId, int officerId) {

        String sql = "DELETE FROM notice WHERE notice_id = ? AND created_by = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, noticeId);
            ps.setInt(2, officerId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Could not delete notice " + noticeId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // =====================================================================
    //  TIMETABLE
    // =====================================================================

    /**
     * Slots of the latest ACTIVE / PUBLISHED timetable:
     * {day, start_time HH:mm, end_time HH:mm, course_code, lecture_hall}
     */
    public List<String[]> getActiveTimetable() {

        String sql = "SELECT te.day, te.start_time, te.end_time, te.lecture_hall, c.course_code " +
                "FROM timetable_entity te " +
                "JOIN timetable t ON t.timetable_id = te.timetable_id " +
                "LEFT JOIN course_offering o ON o.offering_id = te.offering_id " +
                "LEFT JOIN course c ON c.course_id = o.course_id " +
                "WHERE t.timetable_id = (SELECT MAX(timetable_id) FROM timetable " +
                "                         WHERE status IN ('ACTIVE', 'PUBLISHED')) " +
                "AND te.offering_id IS NOT NULL " +
                "ORDER BY te.start_time";

        List<String[]> rows = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new String[]{
                        nz(rs.getString("day")),
                        hhmm(rs.getTime("start_time")),
                        hhmm(rs.getTime("end_time")),
                        nz(rs.getString("course_code")),
                        nz(rs.getString("lecture_hall"))
                });
            }
            return rows;

        } catch (SQLException e) {
            throw new DataAccessException("Could not load the timetable", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // =====================================================================
    //  HELPERS
    // =====================================================================

    private int scalarInt(String sql) {
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Scalar-int query failed", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String hhmm(java.sql.Time t) {
        return t == null ? "" : t.toString().substring(0, 5);
    }

    // 6. Convert ResultSet into TechnicalOfficer object
    private TechnicalOfficer map(ResultSet rs) throws SQLException {

        TechnicalOfficer t = new TechnicalOfficer();

        // User details
        t.setUser_id(rs.getInt("user_id"));
        t.setName(rs.getString("name"));
        t.setEmail(rs.getString("email"));
        t.setPassword(rs.getString("password"));
        t.setContact_number(rs.getString("phone"));
        t.setAddress(rs.getString("address"));
        t.setDOB(rs.getDate("dob"));
        t.setGender(rs.getString("gender"));
        t.setUserType(rs.getString("user_type"));

        // Technical officer details
        t.setDep_id(rs.getInt("dep_id"));

        return t;
    }
}