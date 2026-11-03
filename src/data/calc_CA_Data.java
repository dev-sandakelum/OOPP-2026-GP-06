package data;

import database.Database;
import Exception.DataAccessException;
import model.Course;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Calculates the CA marks of the students of a course and saves them in result.ca_result.
 *
 * Every saved mark (quizzes, assignments, project, mid exam) is out of 100.
 * The weights come from the course table (quiz_weight, assignment_weight, project_weight, mid_weight);
 * together they add up to the course's ca_weight, so the CA mark is out of ca_weight.
 *
 *   quiz       = (average of the 2 best quizzes) / 100 * quiz_weight
 *   assignment = (Assignment 1 + Assignment 2) / 200 * assignment_weight   (the total of the two assignments, scaled to the weight)
 *   project    = project mark / 100 * project_weight
 *   mid exam   = mid mark / 100 * mid_weight
 *   CA marks   = quiz + assignment + project + mid exam      (rounded to 2 decimals)
 *
 * A mark that is not entered counts as 0.
 */
public class calc_CA_Data {

    private final MarksData marksData = new MarksData();

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    /** The CA marks of one student. marks: name ("Quiz 1", "Mid Exam", ...) -> mark out of 100. */
    public static BigDecimal calculate(Course course, int assignmentCount, Map<String, BigDecimal> marks) {
        // quizzes: only the best two count
        List<Double> quizzes = new ArrayList<>();
        for (String name : new String[]{"Quiz 1", "Quiz 2", "Quiz 3"}) {
            quizzes.add(value(marks, name));
        }
        Collections.sort(quizzes, Collections.reverseOrder());
        double quiz = (quizzes.get(0) + quizzes.get(1)) / 2.0;

        // assignments: the total of the two assignment marks (out of 200) scaled to 100, so the weight can be applied
        int count = Math.min(assignmentCount, 2);
        double assignment = 0;
        for (int i = 1; i <= count; i++) {
            assignment += value(marks, "Assignment " + i) / count;
        }

        double ca = part(quiz, course.getQuiz_weight())
                + part(assignment, course.getAssignment_weight())
                + part(value(marks, "Project"), course.getProject_weight())
                + part(value(marks, "Mid Exam"), course.getMid_weight());

        return BigDecimal.valueOf(ca).setScale(2, RoundingMode.HALF_UP);
    }

    // mark out of 100 -> marks out of the weight
    private static double part(double mark, int weight) {
        return mark / 100.0 * weight;
    }

    // saved mark, or 0 if it was not entered
    private static double value(Map<String, BigDecimal> marks, String name) {
        BigDecimal mark = marks.get(name);
        return mark == null ? 0 : mark.doubleValue();
    }

    /**
     * Calculates and saves the CA marks of the given students (from their SAVED marks).
     * Students who have no saved marks yet are skipped.
     * Returns how many students got a CA mark.
     */
    public int calculateAndSave(int offeringId, Course course, int assignmentCount, List<Integer> studentIds) {
        Map<Integer, Map<String, BigDecimal>> saved = marksData.findByOffering(offeringId);
        String sql = "UPDATE result SET ca_result = ? WHERE student_id = ? " +
                "AND exam_id = (SELECT MIN(exam_id) FROM exam WHERE offering_id = ?)";
        int done = 0;
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int studentId : studentIds) {
                Map<String, BigDecimal> marks = saved.get(studentId);
                if (marks == null) continue;   // no marks saved for this student
                ps.setBigDecimal(1, calculate(course, assignmentCount, marks));
                ps.setInt(2, studentId);
                ps.setInt(3, offeringId);
                done += ps.executeUpdate();
            }
            return done;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save CA marks for offering ID: " + offeringId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
