package data;

import database.Database;
import model.Enrollment;
import Exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Enrollment has a composite key: student_id + offering_id. */
public class EnrollmentData {

    private static final String TABLE = "enrollment";
    private static final String COLUMNS = "student_id, offering_id, enrollment_date, enrollment_status";

    // ---------- CREATE ----------

    public Enrollment insert(Enrollment en) {
        String sql = "INSERT INTO " + TABLE
                + " (student_id, offering_id, enrollment_date, enrollment_status) VALUES (?, ?, ?, ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, en.getStudent_id());
            ps.setInt(2, en.getOffering_id());
            ps.setDate(3, en.getEnrollment_date());
            ps.setString(4, en.getEnrollment_status());
            ps.executeUpdate();
            return en;
        } catch (Exception e) {
            throw new DataAccessException("Could not insert enrollment", e);
        }
    }

    // ---------- READ ----------

    public Enrollment find(int studentId, int offeringId) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " WHERE student_id = ? AND offering_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("Could not load enrollment", e);
        }
    }

    public List<Enrollment> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " ORDER BY enrollment_date";
        List<Enrollment> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load enrollments", e);
        }
    }

    public List<Enrollment> findByStudent(int studentId) {
        return findList("student_id", studentId);
    }

    public List<Enrollment> findByOffering(int offeringId) {
        return findList("offering_id", offeringId);
    }

    private List<Enrollment> findList(String column, int value) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " WHERE " + column + " = ?";
        List<Enrollment> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load enrollments", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM " + TABLE;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not count enrollments", e);
        }
    }

    // ---------- UPDATE ----------

    /** Updates the date and status. The student/offering key cannot change. */
    public boolean update(Enrollment en) {
        String sql = "UPDATE " + TABLE
                + " SET enrollment_date = ?, enrollment_status = ? WHERE student_id = ? AND offering_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, en.getEnrollment_date());
            ps.setString(2, en.getEnrollment_status());
            ps.setInt(3, en.getStudent_id());
            ps.setInt(4, en.getOffering_id());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not update enrollment", e);
        }
    }

    // ---------- DELETE ----------

    public boolean delete(int studentId, int offeringId) {
        String sql = "DELETE FROM " + TABLE + " WHERE student_id = ? AND offering_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, offeringId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not delete enrollment", e);
        }
    }

    private Enrollment map(ResultSet rs) throws SQLException {
        Enrollment en = new Enrollment();
        en.setStudent_id(rs.getInt("student_id"));
        en.setOffering_id(rs.getInt("offering_id"));
        en.setEnrollment_date(rs.getDate("enrollment_date"));
        en.setEnrollment_status(rs.getString("enrollment_status"));
        return en;
    }
}
