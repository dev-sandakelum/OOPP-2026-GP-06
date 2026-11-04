package data;

import database.Database;
import Exception.DataAccessException;
import model.Course;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Calculates the subject grade, SGPA and CGPA of the students and saves them in the result table.
 *
 * Subject (per course):
 *   end exam part = end exam mark (out of 100) / 100 * course.end_weight
 *   total mark    = CA marks + end exam part              (out of 100, because ca_weight + end_weight = 100)
 *   grade         = grade of the total mark (scale below)
 *   result_status = PASS if total mark is 40 or more, else FAIL
 *
 * GPA:
 *   SGPA = sum(credit x grade point) / sum(credit)   of the courses of one semester
 *   CGPA = the same for all the courses of the student
 *   Courses with gpa_type = NGPA are not counted.
 *
 * NOTE: the grade scale is the one used in this project. Check it against UGC Circular No. 12/2024
 * and change the two arrays below if the circular uses different values.
 */
public class Grades_Gpa_Data {

    // a total mark of at least MIN_MARK[i] gets GRADES[i] with POINTS[i] grade points
    private static final double[] MIN_MARK = {85, 75, 70, 65, 60, 55, 50, 45, 40, 35, 30, 0};
    private static final String[] GRADES = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "E"};
    private static final double[] POINTS = {4.0, 4.0, 3.7, 3.3, 3.0, 2.7, 2.3, 2.0, 1.7, 1.3, 1.0, 0.0};
    private static final double PASS_MARK = 40;

    private final MarksData marksData = new MarksData();

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    // =====================================================================
    // SMALL CALCULATIONS (no database)
    // =====================================================================

    /** End exam mark (out of 100) -> marks out of the course's end exam weight. */
    public static double endExamPart(Course course, double endMark) {
        return endMark / 100.0 * course.getEnd_weight();
    }

    public static String gradeOf(double total) {
        for (int i = 0; i < MIN_MARK.length; i++) {
            if (total >= MIN_MARK[i]) return GRADES[i];
        }
        return "E";
    }

    public static double pointOf(double total) {
        for (int i = 0; i < MIN_MARK.length; i++) {
            if (total >= MIN_MARK[i]) return POINTS[i];
        }
        return 0;
    }

    public static String statusOf(double total) {
        return total >= PASS_MARK ? "PASS" : "FAIL";
    }

    // =====================================================================
    // CALCULATE AND SAVE
    // =====================================================================

    /**
     * Calculates total mark, status, SGPA and CGPA of the given students and saves them.
     * A student needs both CA marks and an end exam mark (saved), otherwise the student is skipped.
     * Returns how many students were calculated.
     */
    public int calculateAndSave(int offeringId, int semesterId, Course course, List<Integer> studentIds) {
        Map<Integer, Map<String, BigDecimal>> saved = marksData.findByOffering(offeringId);
        int done = 0;
        try (Connection c = conn()) {
            for (int studentId : studentIds) {
                Map<String, BigDecimal> marks = saved.get(studentId);
                if (marks == null || marks.get("CA") == null || marks.get("End Exam") == null) continue;

                // 1. total mark of this course
                double end = endExamPart(course, marks.get("End Exam").doubleValue());
                double total = marks.get("CA").doubleValue() + end;
                saveTotal(c, studentId, offeringId, total);

                // 2. SGPA (this semester) and CGPA (all semesters) of the student
                saveSgpa(c, studentId, semesterId, gpa(c, studentId, semesterId));
                saveCgpa(c, studentId, gpa(c, studentId, 0));
                done++;
            }
            return done;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to calculate grades for offering ID: " + offeringId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // total_mark and result_status of the student's result row in this course
    private void saveTotal(Connection c, int studentId, int offeringId, double total) throws SQLException {
        String sql = "UPDATE result SET total_mark = ?, result_status = ? WHERE student_id = ? " +
                "AND exam_id = (SELECT MIN(exam_id) FROM exam WHERE offering_id = ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, round(total));
            ps.setString(2, statusOf(total));
            ps.setInt(3, studentId);
            ps.setInt(4, offeringId);
            ps.executeUpdate();
        }
    }

    // GPA of one semester (semesterId > 0) or of all semesters (semesterId = 0)
    private double gpa(Connection c, int studentId, int semesterId) throws SQLException {
        String sql = "SELECT cs.credit, r.total_mark " +
                "FROM result r " +
                "JOIN exam e ON e.exam_id = r.exam_id " +
                "JOIN course_offering o ON o.offering_id = e.offering_id " +
                "JOIN course cs ON cs.course_id = o.course_id " +
                "WHERE r.student_id = ? AND r.total_mark IS NOT NULL " +
                "AND (cs.gpa_type IS NULL OR cs.gpa_type <> 'NGPA') " +
                "AND e.exam_id = (SELECT MIN(exam_id) FROM exam WHERE offering_id = o.offering_id) " +
                (semesterId > 0 ? "AND o.semester_id = ?" : "");
        double credits = 0, points = 0;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            if (semesterId > 0) ps.setInt(2, semesterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    credits += rs.getDouble("credit");
                    points += rs.getDouble("credit") * pointOf(rs.getDouble("total_mark"));
                }
            }
        }
        return credits == 0 ? 0 : points / credits;
    }

    // the SGPA is saved in every result row of the student in that semester
    private void saveSgpa(Connection c, int studentId, int semesterId, double sgpa) throws SQLException {
        String sql = "UPDATE result r " +
                "JOIN exam e ON e.exam_id = r.exam_id " +
                "JOIN course_offering o ON o.offering_id = e.offering_id " +
                "SET r.sgpa = ? WHERE r.student_id = ? AND o.semester_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, round(sgpa));
            ps.setInt(2, studentId);
            ps.setInt(3, semesterId);
            ps.executeUpdate();
        }
    }

    // the CGPA is saved in every result row of the student
    private void saveCgpa(Connection c, int studentId, double cgpa) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE result SET cgpa = ? WHERE student_id = ?")) {
            ps.setBigDecimal(1, round(cgpa));
            ps.setInt(2, studentId);
            ps.executeUpdate();
        }
    }

    private BigDecimal round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
