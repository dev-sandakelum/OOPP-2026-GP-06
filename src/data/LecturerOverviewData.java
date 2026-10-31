package data;

import database.Database;
import Exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** Read-only queries for the lecturer Overview page. Every query is filtered by lecturer_id. */
public class LecturerOverviewData {

    /** Number of modules (course offerings) taught by the lecturer. */
    public int countModules(int lecturerId) {
        String sql = "SELECT COUNT(*) FROM course_offering WHERE lecturer_id = ?";
        return scalarInt(sql, lecturerId);
    }

    /** Number of different students enrolled in the lecturer's modules. */
    public int countStudents(int lecturerId) {
        String sql = "SELECT COUNT(DISTINCT e.student_id) FROM enrollment e"
                + " JOIN course_offering o ON o.offering_id = e.offering_id"
                + " WHERE o.lecturer_id = ?";
        return scalarInt(sql, lecturerId);
    }

    /** Average attendance percentage (0-100) over all sessions of the lecturer's modules. */
    public int averageAttendance(int lecturerId) {
        String sql = "SELECT ROUND(SUM(a.attend_status = 'PRESENT') * 100.0 / COUNT(*)) FROM attend a"
                + " JOIN `session` s ON s.session_id = a.session_id"
                + " JOIN course_offering o ON o.offering_id = s.offering_id"
                + " WHERE o.lecturer_id = ?";
        return scalarInt(sql, lecturerId);
    }

    /** Pending medicals of students enrolled in the lecturer's modules. */
    public int countPendingMedicals(int lecturerId) {
        String sql = "SELECT COUNT(DISTINCT m.medical_id) FROM medical m"
                + " JOIN enrollment e ON e.student_id = m.student_id"
                + " JOIN course_offering o ON o.offering_id = e.offering_id"
                + " WHERE o.lecturer_id = ? AND m.status = 'PENDING'";
        return scalarInt(sql, lecturerId);
    }

    /** Today's classes from the latest published timetable. Each row: {time, module, hall}. */
    public List<String[]> todaySchedule(int lecturerId, String day) {
        String sql = "SELECT te.start_time, te.end_time, c.course_code, c.course_name, te.lecture_hall"
                + " FROM timetable_entity te"
                + " JOIN course_offering o ON o.offering_id = te.offering_id"
                + " JOIN course c ON c.course_id = o.course_id"
                + " WHERE o.lecturer_id = ? AND UPPER(te.day) = UPPER(?)"
                + " AND te.timetable_id = (SELECT MAX(timetable_id) FROM timetable WHERE status = 'PUBLISHED')"
                + " ORDER BY te.start_time";
        List<String[]> rows = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, lecturerId);
            ps.setString(2, day);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String start = String.valueOf(rs.getTime("start_time")).substring(0, 5);
                    String end = String.valueOf(rs.getTime("end_time")).substring(0, 5);
                    rows.add(new String[]{
                            start + " - " + end,
                            rs.getString("course_code") + " " + rs.getString("course_name"),
                            rs.getString("lecture_hall")});
                }
            }
            return rows;
        } catch (Exception e) {
            throw new DataAccessException("Could not load today's schedule", e);
        }
    }

    /** Latest pending medicals of the lecturer's students. Each row: {student name, medical date}. */
    public List<String[]> pendingMedicals(int lecturerId, int limit) {
        String sql = "SELECT DISTINCT m.medical_id, u.name, m.medical_date FROM medical m"
                + " JOIN users u ON u.user_id = m.student_id"
                + " JOIN enrollment e ON e.student_id = m.student_id"
                + " JOIN course_offering o ON o.offering_id = e.offering_id"
                + " WHERE o.lecturer_id = ? AND m.status = 'PENDING'"
                + " ORDER BY m.medical_date DESC LIMIT ?";
        List<String[]> rows = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, lecturerId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{rs.getString("name"), String.valueOf(rs.getDate("medical_date"))});
                }
            }
            return rows;
        } catch (Exception e) {
            throw new DataAccessException("Could not load pending medicals", e);
        }
    }

    private int scalarInt(String sql, int lecturerId) {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            throw new DataAccessException("Could not run overview query", e);
        }
    }
}
