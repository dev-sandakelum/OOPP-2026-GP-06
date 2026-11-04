package data;

import database.Database;
import model.Timetable;
import Exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** CRUD for the timetable table. The rows inside a timetable are in TimetableEntityData. */
public class TimetableData {

    private static final String TABLE = "timetable";
    private static final String COLUMNS = "timetable_id, academic_year, published_date, status";

    // ---------- CREATE ----------

    public Timetable insert(Timetable t) {
        String sql = "INSERT INTO " + TABLE + " (academic_year, published_date, status) VALUES (?, ?, ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getAcademic_year());
            ps.setDate(2, t.getPublished_date());
            ps.setString(3, t.getStatus());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    t.setTimetable_id(keys.getInt(1));
                }
            }
            return t;
        } catch (Exception e) {
            throw new DataAccessException("Could not insert timetable", e);
        }
    }

    // ---------- READ ----------

    public Timetable findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " WHERE timetable_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("Could not load timetable " + id, e);
        }
    }

    public List<Timetable> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " ORDER BY timetable_id";
        List<Timetable> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load timetables", e);
        }
    }

    public List<Timetable> findByStatus(String status) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " WHERE status = ? ORDER BY timetable_id";
        List<Timetable> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load timetables", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM " + TABLE;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not count timetables", e);
        }
    }

    // ---------- UPDATE ----------

    public boolean update(Timetable t) {
        String sql = "UPDATE " + TABLE
                + " SET academic_year = ?, published_date = ?, status = ? WHERE timetable_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getAcademic_year());
            ps.setDate(2, t.getPublished_date());
            ps.setString(3, t.getStatus());
            ps.setInt(4, t.getTimetable_id());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not update timetable " + t.getTimetable_id(), e);
        }
    }

    // ---------- DELETE ----------

    /** Also deletes its entries (ON DELETE CASCADE). */
    public boolean delete(int id) {
        String sql = "DELETE FROM " + TABLE + " WHERE timetable_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not delete timetable " + id, e);
        }
    }

    private Timetable map(ResultSet rs) throws SQLException {
        Timetable t = new Timetable();
        t.setTimetable_id(rs.getInt("timetable_id"));
        t.setAcademic_year(rs.getString("academic_year"));
        t.setPublished_date(rs.getDate("published_date"));
        t.setStatus(rs.getString("status"));
        return t;
    }
}
