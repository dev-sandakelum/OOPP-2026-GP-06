package data;

import database.Database;
import Exception.DataAccessException;
import model.RepeatStudent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepeatStudentData {

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    private RepeatStudent map(ResultSet rs) throws SQLException {
        return new RepeatStudent(
                rs.getInt("student_id"),
                rs.getInt("subject_id"),
                rs.getInt("repeat_year"),
                rs.getInt("repeat_shy"),
                rs.getString("review")
        );
    }

    public RepeatStudent find(int studentId, int subjectId) {
        String sql = "SELECT * FROM repeat_student WHERE student_id = ? AND subject_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find repeat student for student " + studentId + ", subject " + subjectId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<RepeatStudent> findByStudent(int studentId) {
        String sql = "SELECT * FROM repeat_student WHERE student_id = ?";
        List<RepeatStudent> list = new ArrayList<>();
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
            throw new DataAccessException("Failed to find repeat modules for student ID: " + studentId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean save(RepeatStudent r) {
        String sql = "INSERT INTO repeat_student (student_id, subject_id, repeat_year, repeat_shy, review) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE repeat_year = VALUES(repeat_year), repeat_shy = VALUES(repeat_shy), review = VALUES(review)";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, r.getStudentId());
            ps.setInt(2, r.getSubjectId());
            ps.setInt(3, r.getRepeatYear());
            ps.setInt(4, r.getRepeatShy());
            ps.setString(5, r.getReview());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save repeat student record", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(int studentId, int subjectId) {
        String sql = "DELETE FROM repeat_student WHERE student_id = ? AND subject_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete repeat student record for student " + studentId + ", subject " + subjectId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}