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
 * Saves and loads the marks of the Grades & GPA page.
 *
 * All the marks of one student in one course are stored in ONE row of the result table
 * (the row of the course's first exam), in these columns (see sql/15.add_result_assessment_marks.sql):
 *   quiz1_mark, quiz2_mark, quiz3_mark, mid_mark, assignment1_mark, assignment2_mark, project_mark
 * A mark that was not entered is NULL. The other result columns (ca_result, final_exam_mark,
 * total_mark, sgpa, ...) are not changed here.
 */
public class MarksData {

    // The names used by the page, in the same order as the result columns below
    public static final String[] NAMES = {"Quiz 1", "Quiz 2", "Quiz 3", "Mid Exam", "Assignment 1", "Assignment 2", "Project"};
    private static final String[] COLUMNS = {"quiz1_mark", "quiz2_mark", "quiz3_mark", "mid_mark",
            "assignment1_mark", "assignment2_mark", "project_mark"};

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    /** All saved marks of a course: student id -> (name such as "Quiz 1" -> mark; "CA" -> calculated CA marks). */
    public Map<Integer, Map<String, BigDecimal>> findByOffering(int offeringId) {
        String sql = "SELECT r.* FROM result r WHERE r.exam_id = (SELECT MIN(exam_id) FROM exam WHERE offering_id = ?)";
        Map<Integer, Map<String, BigDecimal>> all = new HashMap<>();
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, BigDecimal> marks = new HashMap<>();
                    for (int i = 0; i < NAMES.length; i++) {
                        marks.put(NAMES[i], rs.getBigDecimal(COLUMNS[i]));
                    }
                    marks.put("End Exam", rs.getBigDecimal("final_exam_mark"));
                    marks.put("CA", rs.getBigDecimal("ca_result"));
                    marks.put("Total", rs.getBigDecimal("total_mark"));   // saved by Grades_Gpa_Data
                    marks.put("SGPA", rs.getBigDecimal("sgpa"));
                    marks.put("CGPA", rs.getBigDecimal("cgpa"));   // calculated by calc_CA_Data
                    all.put(rs.getInt("student_id"), marks);
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
     * marks: name -> mark. A missing name or a null mark is saved as NULL (not entered).
     */
    public void save(int studentId, int offeringId, Map<String, BigDecimal> marks) {
        String sql = "INSERT INTO result (student_id, exam_id, " + String.join(", ", COLUMNS) + ") " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "quiz1_mark = VALUES(quiz1_mark), quiz2_mark = VALUES(quiz2_mark), quiz3_mark = VALUES(quiz3_mark), " +
                "mid_mark = VALUES(mid_mark), assignment1_mark = VALUES(assignment1_mark), " +
                "assignment2_mark = VALUES(assignment2_mark), project_mark = VALUES(project_mark)";
        try (Connection c = conn()) {
            int examId = findOrCreateExam(c, offeringId);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, studentId);
                ps.setInt(2, examId);
                for (int i = 0; i < NAMES.length; i++) {
                    ps.setBigDecimal(3 + i, marks.get(NAMES[i]));
                }
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save marks of student " + studentId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Saves the end exam mark (result.final_exam_mark) of one student. null = not entered. */
    public void saveEndExam(int studentId, int offeringId, BigDecimal mark) {
        String sql = "INSERT INTO result (student_id, exam_id, final_exam_mark) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE final_exam_mark = VALUES(final_exam_mark)";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, findOrCreateExam(c, offeringId));
            ps.setBigDecimal(3, mark);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save end exam mark of student " + studentId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // The exam that holds the marks of a course (its first exam). It is created if the course has none.
    private int findOrCreateExam(Connection c, int offeringId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT MIN(exam_id) FROM exam WHERE offering_id = ?")) {
            ps.setInt(1, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return rs.getInt(1);
            }
        }

        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO exam (offering_id, type) VALUES (?, 'Final')", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, offeringId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }
}
