package data;

import database.Database;
import Exception.DataAccessException;
import model.Exam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExamData {

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    private Exam map(ResultSet rs) throws SQLException {
        return new Exam(
                rs.getInt("exam_id"),
                rs.getInt("offering_id"),
                rs.getString("type")
        );
    }

    public Exam findById(int examId) {
        String sql = "SELECT * FROM exam WHERE exam_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find exam by ID: " + examId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Exam> findByOffering(int offeringId) {
        String sql = "SELECT * FROM exam WHERE offering_id = ?";
        List<Exam> list = new ArrayList<>();
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find exams for offering ID: " + offeringId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Exam insert(Exam e) {
        String sql = "INSERT INTO exam (offering_id, type) VALUES (?, ?)";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getOfferingId());
            ps.setString(2, e.getType());

            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    e.setExamId(gk.getInt(1));
                }
            }
            return e;
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to insert exam", ex);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean delete(int examId) {
        String sql = "DELETE FROM exam WHERE exam_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete exam ID: " + examId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}