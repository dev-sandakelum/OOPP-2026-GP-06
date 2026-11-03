package data;

import database.Database;
import Exception.DataAccessException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Saves and loads the marks of the Grades & GPA page (Quiz 1, 2, 3, Mid Exam, Assignments, Project).
 *
 * Every assessment of a course is one row in the exam table (exam.type = "Quiz 1", "Mid Exam", ...),
 * and the mark of a student is one row in the result table:
 *   result.student_id, result.exam_id  -> which student and which assessment
 *   result.ca_result                   -> the mark entered by the lecturer
 *   result.total_mark                  -> same mark (these are not the final exam)
 *   result.result_status               -> "ENTERED"
 *   final_exam_mark, sgpa, cgpa, published_date are left empty (they are set later)
 */
public class MarksData {

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    /** All saved marks of a course: student id -> (assessment name -> mark). */
    public Map<Integer, Map<String, BigDecimal>> findByOffering(int offeringId) {
        String sql = "SELECT r.student_id, e.type, r.ca_result " +
                "FROM result r JOIN exam e ON e.exam_id = r.exam_id " +
                "WHERE e.offering_id = ? AND r.ca_result IS NOT NULL";
        Map<Integer, Map<String, BigDecimal>> all = new HashMap<>();
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    all.computeIfAbsent(rs.getInt("student_id"), k -> new HashMap<>())
                            .put(rs.getString("type"), rs.getBigDecimal("ca_result"));
                }
            }
            return all;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load marks for offering ID: " + offeringId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves the marks of one student in one course.
     * marks: assessment name -> mark (a null mark means "not entered", so that saved result is removed).
     */
    public void save(int studentId, int offeringId, Map<String, BigDecimal> marks) {
        try (Connection c = conn()) {
            c.setAutoCommit(false);   // save all marks of the student together
            try {
                for (Map.Entry<String, BigDecimal> entry : marks.entrySet()) {
                    int examId = findOrCreateExam(c, offeringId, entry.getKey());
                    if (entry.getValue() == null) {
                        deleteResult(c, studentId, examId);
                    } else {
                        saveResult(c, studentId, examId, entry.getValue());
                    }
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save marks of student " + studentId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // The exam row of an assessment (e.g. "Quiz 1" of this course). It is created the first time.
    private int findOrCreateExam(Connection c, int offeringId, String type) throws SQLException {
        String find = "SELECT exam_id FROM exam WHERE offering_id = ? AND type = ?";
        try (PreparedStatement ps = c.prepareStatement(find)) {
            ps.setInt(1, offeringId);
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        String insert = "INSERT INTO exam (offering_id, type) VALUES (?, ?)";
        try (PreparedStatement ps = c.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, offeringId);
            ps.setString(2, type);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    // Insert the result, or update it if this student already has a result for this exam
    private void saveResult(Connection c, int studentId, int examId, BigDecimal mark) throws SQLException {
        String sql = "INSERT INTO result (student_id, exam_id, ca_result, final_exam_mark, total_mark, sgpa, result_status, published_date, cgpa) " +
                "VALUES (?, ?, ?, NULL, ?, NULL, 'ENTERED', NULL, NULL) " +
                "ON DUPLICATE KEY UPDATE ca_result = VALUES(ca_result), total_mark = VALUES(total_mark), result_status = VALUES(result_status)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, examId);
            ps.setBigDecimal(3, mark);
            ps.setBigDecimal(4, mark);
            ps.executeUpdate();
        }
    }

    private void deleteResult(Connection c, int studentId, int examId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM result WHERE student_id = ? AND exam_id = ?")) {
            ps.setInt(1, studentId);
            ps.setInt(2, examId);
            ps.executeUpdate();
        }
    }
}
