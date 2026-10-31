package data;

import database.Database;
import model.CourseOffering;
import Exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** A course offering = one course taught in one semester by one lecturer. */
public class CourseOfferingData {

    private static final String TABLE = "course_offering";
    private static final String COLUMNS = "offering_id, course_id, semester_id, lecturer_id";

    // ---------- CREATE ----------

    public CourseOffering insert(CourseOffering o) {
        String sql = "INSERT INTO " + TABLE + " (course_id, semester_id, lecturer_id) VALUES (?, ?, ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setValues(ps, o);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    o.setOffering_id(keys.getInt(1));
                }
            }
            return o;
        } catch (Exception e) {
            throw new DataAccessException("Could not insert course offering", e);
        }
    }

    // ---------- READ ----------

    public CourseOffering findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " WHERE offering_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("Could not load course offering " + id, e);
        }
    }

    public List<CourseOffering> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " ORDER BY offering_id";
        List<CourseOffering> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load course offerings", e);
        }
    }

    public List<CourseOffering> findByLecturer(int lecturerId) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " WHERE lecturer_id = ? ORDER BY offering_id";
        List<CourseOffering> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load offerings for lecturer " + lecturerId, e);
        }
    }

    // ---------- UPDATE ----------

    public boolean update(CourseOffering o) {
        String sql = "UPDATE " + TABLE
                + " SET course_id = ?, semester_id = ?, lecturer_id = ? WHERE offering_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            setValues(ps, o);
            ps.setInt(4, o.getOffering_id());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not update course offering " + o.getOffering_id(), e);
        }
    }

    // ---------- DELETE ----------

    public boolean delete(int id) {
        String sql = "DELETE FROM " + TABLE + " WHERE offering_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not delete course offering " + id, e);
        }
    }

    /** Sets parameters 1-3 (shared by insert and update). */
    private void setValues(PreparedStatement ps, CourseOffering o) throws SQLException {
        ps.setInt(1, o.getCourse_id());
        ps.setInt(2, o.getSemester_id());
        if (o.getLecturer_id() != null) ps.setInt(3, o.getLecturer_id());
        else ps.setNull(3, Types.INTEGER);
    }

    private CourseOffering map(ResultSet rs) throws SQLException {
        CourseOffering o = new CourseOffering();
        o.setOffering_id(rs.getInt("offering_id"));
        o.setCourse_id(rs.getInt("course_id"));
        o.setSemester_id(rs.getInt("semester_id"));
        int lecturer = rs.getInt("lecturer_id");
        o.setLecturer_id(rs.wasNull() ? null : lecturer);
        return o;
    }
}
