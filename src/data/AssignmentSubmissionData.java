package data;

import database.Database;
import Exception.DataAccessException;
import model.AssignmentSubmission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD access for the assignment_submission table.
 * The table is created automatically on first use (CREATE TABLE IF NOT EXISTS),
 * so no manual migration script needs to be run before the student logs in.
 */
public class AssignmentSubmissionData {

    private static final String TABLE   = "assignment_submission";
    private static final String COLUMNS =
            "submission_id, assessment_id, student_id, file_path, submitted_at, status, remarks";

    // Created once per JVM run; subsequent calls skip immediately.
    private static volatile boolean tableEnsured = false;

    private void ensureTable() {
        if (tableEnsured) return;
        String ddl =
            "CREATE TABLE IF NOT EXISTS assignment_submission ("
            + "  submission_id  INT AUTO_INCREMENT PRIMARY KEY,"
            + "  assessment_id  INT NOT NULL,"
            + "  student_id     INT NOT NULL,"
            + "  file_path      VARCHAR(500),"
            + "  submitted_at   DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "  status         VARCHAR(30) DEFAULT 'SUBMITTED',"
            + "  remarks        VARCHAR(500),"
            + "  UNIQUE (assessment_id, student_id),"
            + "  CONSTRAINT fk_sub_assessment FOREIGN KEY (assessment_id)"
            + "    REFERENCES assessment(assessment_id) ON UPDATE CASCADE ON DELETE CASCADE,"
            + "  CONSTRAINT fk_sub_student FOREIGN KEY (student_id)"
            + "    REFERENCES undergraduate(user_id) ON UPDATE CASCADE ON DELETE CASCADE"
            + ")";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(ddl)) {
            ps.executeUpdate();
            tableEnsured = true;
        } catch (Exception ignored) {
            // If DDL privilege is missing the individual DML calls will fail
            // and callers handle those exceptions gracefully.
        }
    }

    // ---------- map ----------

    private AssignmentSubmission map(ResultSet rs) throws SQLException {
        return new AssignmentSubmission(
                rs.getInt("submission_id"),
                rs.getInt("assessment_id"),
                rs.getInt("student_id"),
                rs.getString("file_path"),
                rs.getTimestamp("submitted_at"),
                rs.getString("status"),
                rs.getString("remarks")
        );
    }

    // ---------- INSERT ----------

    public AssignmentSubmission insert(AssignmentSubmission s) {
        ensureTable();
        String sql = "INSERT INTO " + TABLE
                + " (assessment_id, student_id, file_path, status, remarks)"
                + " VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getAssessmentId());
            ps.setInt(2, s.getStudentId());
            ps.setString(3, s.getFilePath());
            ps.setString(4, s.getStatus() != null ? s.getStatus() : "SUBMITTED");
            ps.setString(5, s.getRemarks());
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) s.setSubmissionId(gk.getInt(1));
            }
            return s;
        } catch (Exception e) {
            throw new DataAccessException("Could not insert assignment submission", e);
        }
    }

    // ---------- READ ----------

    public AssignmentSubmission findById(int submissionId) {
        ensureTable();
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " WHERE submission_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, submissionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("Could not find submission by id", e);
        }
    }

    /** Returns null if no record exists (table missing → returns null via safeGetSubmission). */
    public AssignmentSubmission findByAssessmentAndStudent(int assessmentId, int studentId) {
        ensureTable();
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE
                + " WHERE assessment_id = ? AND student_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, assessmentId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("Could not find submission", e);
        }
    }

    public List<AssignmentSubmission> findByAssessment(int assessmentId) {
        ensureTable();
        return findList("assessment_id", assessmentId);
    }

    public List<AssignmentSubmission> findByStudent(int studentId) {
        ensureTable();
        return findList("student_id", studentId);
    }

    private List<AssignmentSubmission> findList(String column, int value) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " WHERE " + column + " = ?";
        List<AssignmentSubmission> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not list submissions", e);
        }
    }

    // ---------- UPDATE ----------

    public boolean update(AssignmentSubmission s) {
        ensureTable();
        String sql = "UPDATE " + TABLE
                + " SET file_path = ?, status = ?, remarks = ?, submitted_at = CURRENT_TIMESTAMP"
                + " WHERE submission_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getFilePath());
            ps.setString(2, s.getStatus());
            ps.setString(3, s.getRemarks());
            ps.setInt(4, s.getSubmissionId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not update submission", e);
        }
    }

    // ---------- DELETE ----------

    public boolean delete(int submissionId) {
        ensureTable();
        String sql = "DELETE FROM " + TABLE + " WHERE submission_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, submissionId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not delete submission", e);
        }
    }
}
