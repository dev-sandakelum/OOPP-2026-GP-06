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
    private static final String COLUMNS = "course_id, course_name, course_code, credit, dep_id";

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
        return c;
    }
}
