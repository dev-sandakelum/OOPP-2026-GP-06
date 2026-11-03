package data;

import database.Database;
import model.Course;
import Exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CourseData {

    private static final String TABLE = "course";
    private static final String COLUMNS = "course_id, course_name, course_code, credit, dep_id, "
            + "course_type, lecturer_id, batch, gpa_type, end_weight, ca_weight, "
            + "quiz_weight, assignment_weight, project_weight, mid_weight";

    // ---------- CREATE ----------

    /** Inserts the course and sets the generated course_id on the object. */
    public Course insert(Course c) {
        String sql = "INSERT INTO " + TABLE + " (course_name, course_code, credit, dep_id) VALUES (?, ?, ?, ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCourse_name());
            ps.setString(2, c.getCourse_code());
            ps.setDouble(3, c.getCredit());
            if (c.getDep_id() != null) ps.setInt(4, c.getDep_id());
            else ps.setNull(4, Types.INTEGER);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setCourse_id(keys.getInt(1));
                }
            }
            return c;
        } catch (Exception e) {
            throw new DataAccessException("Could not insert course", e);
        }
    }

    /** Inserts a course including type, lecturer, batch, GPA type and evaluation weights. */
    public Course insertFull(Course c) {
        String sql = "INSERT INTO " + TABLE + " (course_name, course_code, credit, dep_id, course_type, lecturer_id, "
                + "batch, gpa_type, end_weight, ca_weight, quiz_weight, assignment_weight, project_weight, mid_weight) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindFull(ps, c);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setCourse_id(keys.getInt(1));
            }
            return c;
        } catch (Exception e) {
            throw new DataAccessException("Could not insert course: " + e.getMessage(), e);
        }
    }

    /** Updates every editable column of the course. */
    public boolean updateFull(Course c) {
        String sql = "UPDATE " + TABLE + " SET course_name = ?, course_code = ?, credit = ?, dep_id = ?, "
                + "course_type = ?, lecturer_id = ?, batch = ?, gpa_type = ?, end_weight = ?, ca_weight = ?, "
                + "quiz_weight = ?, assignment_weight = ?, project_weight = ?, mid_weight = ? WHERE course_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bindFull(ps, c);
            ps.setInt(15, c.getCourse_id());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not update course " + c.getCourse_id() + ": " + e.getMessage(), e);
        }
    }

    private void bindFull(PreparedStatement ps, Course c) throws SQLException {
        ps.setString(1, c.getCourse_name());
        ps.setString(2, c.getCourse_code());
        ps.setDouble(3, c.getCredit());
        if (c.getDep_id() != null) ps.setInt(4, c.getDep_id()); else ps.setNull(4, Types.INTEGER);
        ps.setString(5, c.getCourse_type());
        if (c.getLecturer_id() != null) ps.setInt(6, c.getLecturer_id()); else ps.setNull(6, Types.INTEGER);
        ps.setString(7, c.getBatch());
        ps.setString(8, c.getGpa_type());
        ps.setInt(9, c.getEnd_weight());
        ps.setInt(10, c.getCa_weight());
        ps.setInt(11, c.getQuiz_weight());
        ps.setInt(12, c.getAssignment_weight());
        ps.setInt(13, c.getProject_weight());
        ps.setInt(14, c.getMid_weight());
    }

    // ---------- READ ----------

    public Course findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " WHERE course_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("Could not load course " + id, e);
        }
    }

    public Course findByCode(String code) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " WHERE course_code = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("Could not load course " + code, e);
        }
    }

    public List<Course> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " ORDER BY course_id";
        List<Course> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load courses", e);
        }
    }

    public List<Course> findByDepartment(int depId) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " WHERE dep_id = ? ORDER BY course_id";
        List<Course> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, depId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load courses for department " + depId, e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM " + TABLE;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not count courses", e);
        }
    }

    // ---------- UPDATE ----------

    public boolean update(Course c) {
        String sql = "UPDATE " + TABLE
                + " SET course_name = ?, course_code = ?, credit = ?, dep_id = ? WHERE course_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getCourse_name());
            ps.setString(2, c.getCourse_code());
            ps.setDouble(3, c.getCredit());
            if (c.getDep_id() != null) ps.setInt(4, c.getDep_id());
            else ps.setNull(4, Types.INTEGER);
            ps.setInt(5, c.getCourse_id());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not update course " + c.getCourse_id(), e);
        }
    }

    // ---------- DELETE ----------

    public boolean delete(int id) {
        String sql = "DELETE FROM " + TABLE + " WHERE course_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not delete course " + id, e);
        }
    }

    private Course map(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setCourse_id(rs.getInt("course_id"));
        c.setCourse_name(rs.getString("course_name"));
        c.setCourse_code(rs.getString("course_code"));
        c.setCredit(rs.getDouble("credit"));
        int dep = rs.getInt("dep_id");
        c.setDep_id(rs.wasNull() ? null : dep);
        c.setCourse_type(rs.getString("course_type"));
        int lec = rs.getInt("lecturer_id");
        c.setLecturer_id(rs.wasNull() ? null : lec);
        c.setBatch(rs.getString("batch"));
        c.setGpa_type(rs.getString("gpa_type"));
        c.setEnd_weight(rs.getInt("end_weight"));
        c.setCa_weight(rs.getInt("ca_weight"));
        c.setQuiz_weight(rs.getInt("quiz_weight"));
        c.setAssignment_weight(rs.getInt("assignment_weight"));
        c.setProject_weight(rs.getInt("project_weight"));
        c.setMid_weight(rs.getInt("mid_weight"));
        return c;
    }
}
