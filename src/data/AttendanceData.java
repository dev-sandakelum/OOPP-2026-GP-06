package data;

import database.Database;
import Exception.DataAccessException;
import model.Attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceData {

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    private Attendance map(ResultSet rs) throws SQLException {
        return new Attendance(
                rs.getInt("student_id"),
                rs.getInt("session_id"),
                rs.getString("attend_status"),
                rs.getDate("recorded_date")
        );
    }

    public Attendance find(int studentId, int sessionId) {
        String sql = "SELECT * FROM attend WHERE student_id = ? AND session_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find attendance for student " + studentId + ", session " + sessionId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Attendance> findByStudent(int studentId) {
        String sql = "SELECT * FROM attend WHERE student_id = ?";
        List<Attendance> list = new ArrayList<>();
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find attendance for student ID: " + studentId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Attendance> findBySession(int sessionId) {
        String sql = "SELECT * FROM attend WHERE session_id = ?";
        List<Attendance> list = new ArrayList<>();
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find attendance for session ID: " + sessionId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Attendance> findByStudentAndOffering(int studentId, int offeringId) {
        String sql = "SELECT a.* FROM attend a " +
                "JOIN session s ON a.session_id = s.session_id " +
                "WHERE a.student_id = ? AND s.offering_id = ?";
        List<Attendance> list = new ArrayList<>();
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find attendance for student " + studentId + " in offering " + offeringId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean save(Attendance a) {
        String sql = "INSERT INTO attend (student_id, session_id, attend_status, recorded_date) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE attend_status = VALUES(attend_status), recorded_date = VALUES(recorded_date)";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, a.getStudentId());
            ps.setInt(2, a.getSessionId());
            ps.setString(3, a.getAttendStatus());
            if (a.getRecordedDate() != null) {
                ps.setDate(4, a.getRecordedDate());
            } else {
                ps.setNull(4, Types.DATE);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save attendance record", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(int studentId, int sessionId) {
        String sql = "DELETE FROM attend WHERE student_id = ? AND session_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete attendance for student " + studentId + ", session " + sessionId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}