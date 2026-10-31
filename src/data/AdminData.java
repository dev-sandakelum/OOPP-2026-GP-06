package data;

import database.Database;
import Exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database queries used exclusively by admin UI pages.
 *
 * All "count" helpers query the live DB so the Overview KPI cards
 * always show real numbers.
 */
public class AdminData {

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    // =====================================================================
    //  KPI COUNTS  (Overview cards)
    // =====================================================================

    public int countByRole(String userType) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_type = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userType);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not count users by role", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Courses offered in the most recent (latest semester_id) semester. */
    public int countActiveCourses() {
        String sql = "SELECT COUNT(*) FROM course_offering"
                + " WHERE semester_id = (SELECT MAX(semester_id) FROM semester)";
        return scalarInt(sql);
    }

    /** Notices whose status is 'ACTIVE' or that have not yet expired. */
    public int countPublishedNotices() {
        String sql = "SELECT COUNT(*) FROM notice"
                + " WHERE status = 'ACTIVE' OR (expiry_date IS NULL OR expiry_date >= CURDATE())";
        return scalarInt(sql);
    }

    public int countPendingMedicals() {
        String sql = "SELECT COUNT(*) FROM medical WHERE status = 'PENDING'";
        return scalarInt(sql);
    }

    /** Students whose attendance is below the given threshold (default 80). */
    public int countLowAttendance(int thresholdPct) {
        String sql = "SELECT COUNT(DISTINCT a.student_id)"
                + " FROM attend a"
                + " JOIN session s ON s.session_id = a.session_id"
                + " GROUP BY a.student_id"
                + " HAVING (SUM(CASE WHEN a.attend_status = 'PRESENT' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) < ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM (" + sql + ") sub")) {
            ps.setInt(1, thresholdPct);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not count low-attendance students", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Timetable slots with no course assigned (offering_id IS NULL). */
    public int countUnassignedSlots() {
        String sql = "SELECT COUNT(*) FROM timetable_entity WHERE offering_id IS NULL";
        return scalarInt(sql);
    }

    /** Notices expiring within the next 7 days. */
    public int countExpiringNotices() {
        String sql = "SELECT COUNT(*) FROM notice"
                + " WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)";
        return scalarInt(sql);
    }

    // =====================================================================
    //  ROLES TABLE  (Overview "Users by role" card)
    //  Returns rows of {label, count} already formatted as String[].
    // =====================================================================

    /** Columns: {role label, count} */
    public List<String[]> userCountsByRole() {
        // Fixed labels that match the UI — note user_type values from the schema ENUM.
        String[][] mapping = {
                {"Undergraduates",     "UNDERGRADUATE"},
                {"Lecturers",          "LECTURER"},
                {"Technical Officers", "TECHNICAL_OFFICER"},
                {"Administrators",     "ADMIN"}
        };
        List<String[]> rows = new ArrayList<>();
        for (String[] m : mapping) {
            rows.add(new String[]{m[0], String.valueOf(countByRole(m[1]))});
        }
        return rows;
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
}
