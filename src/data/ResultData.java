package data;

import database.Database;
import Exception.DataAccessException;
import model.Result;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResultData {

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    private Result map(ResultSet rs) throws SQLException {
        return new Result(
                rs.getInt("student_id"),
                rs.getInt("exam_id"),
                rs.getBigDecimal("ca_result"),
                rs.getBigDecimal("final_exam_mark"),
                rs.getBigDecimal("total_mark"),
                rs.getBigDecimal("sgpa"),
                rs.getString("result_status"),
                rs.getDate("published_date"),
                rs.getBigDecimal("cgpa")
        );
    }

    public Result find(int studentId, int examId) {
        String sql = "SELECT * FROM result WHERE student_id = ? AND exam_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find result for student " + studentId + ", exam " + examId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Result> findByStudent(int studentId) {
        String sql = "SELECT * FROM result WHERE student_id = ? ORDER BY published_date DESC";
        List<Result> list = new ArrayList<>();
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
            throw new DataAccessException("Failed to find results for student ID: " + studentId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Result> findByExam(int examId) {
        String sql = "SELECT * FROM result WHERE exam_id = ?";
        List<Result> list = new ArrayList<>();
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find results for exam ID: " + examId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean save(Result r) {
        String sql = "INSERT INTO result (student_id, exam_id, ca_result, final_exam_mark, total_mark, sgpa, result_status, published_date, cgpa) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE ca_result = VALUES(ca_result), final_exam_mark = VALUES(final_exam_mark), " +
                "total_mark = VALUES(total_mark), sgpa = VALUES(sgpa), result_status = VALUES(result_status), " +
                "published_date = VALUES(published_date), cgpa = VALUES(cgpa)";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, r.getStudentId());
            ps.setInt(2, r.getExamId());
            ps.setBigDecimal(3, r.getCaResult());
            ps.setBigDecimal(4, r.getFinalExamMark());
            ps.setBigDecimal(5, r.getTotalMark());
            ps.setBigDecimal(6, r.getSgpa());
            ps.setString(7, r.getResultStatus());

            if (r.getPublishedDate() != null) {
                ps.setDate(8, r.getPublishedDate());
            } else {
                ps.setNull(8, Types.DATE);
            }

            ps.setBigDecimal(9, r.getCgpa());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save result record", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(int studentId, int examId) {
        String sql = "DELETE FROM result WHERE student_id = ? AND exam_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, examId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete result for student " + studentId + ", exam " + examId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}