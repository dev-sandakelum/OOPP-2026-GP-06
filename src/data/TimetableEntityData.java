package data;

import database.Database;
import model.TimetableEntity;
import Exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** CRUD for timetable_entity: one row = one lecture slot inside a timetable. */
public class TimetableEntityData {

    private static final String TABLE = "timetable_entity";
    private static final String COLUMNS =
            "timetable_entity_id, timetable_id, offering_id, day, start_time, end_time, lecture_hall, session_type";

    // ---------- CREATE ----------

    public TimetableEntity insert(TimetableEntity t) {
        String sql = "INSERT INTO " + TABLE
                + " (timetable_id, offering_id, day, start_time, end_time, lecture_hall, session_type)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setValues(ps, t);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    t.setTimetable_entity_id(keys.getInt(1));
                }
            }
            return t;
        } catch (Exception e) {
            throw new DataAccessException("Could not insert timetable entry", e);
        }
    }

    // ---------- READ ----------

    public TimetableEntity findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " WHERE timetable_entity_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("Could not load timetable entry " + id, e);
        }
    }

    public List<TimetableEntity> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " ORDER BY timetable_entity_id";
        List<TimetableEntity> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load timetable entries", e);
        }
    }

    public List<TimetableEntity> findByTimetable(int timetableId) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE
                + " WHERE timetable_id = ? ORDER BY day, start_time";
        List<TimetableEntity> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, timetableId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load entries for timetable " + timetableId, e);
        }
    }

    // ---------- UPDATE ----------

    public boolean update(TimetableEntity t) {
        String sql = "UPDATE " + TABLE
                + " SET timetable_id = ?, offering_id = ?, day = ?, start_time = ?, end_time = ?,"
                + " lecture_hall = ?, session_type = ? WHERE timetable_entity_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            setValues(ps, t);
            ps.setInt(8, t.getTimetable_entity_id());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not update timetable entry " + t.getTimetable_entity_id(), e);
        }
    }

    // ---------- DELETE ----------

    public boolean delete(int id) {
        String sql = "DELETE FROM " + TABLE + " WHERE timetable_entity_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not delete timetable entry " + id, e);
        }
    }

    /** Sets parameters 1-7 (shared by insert and update). */
    private void setValues(PreparedStatement ps, TimetableEntity t) throws SQLException {
        ps.setInt(1, t.getTimetable_id());
        if (t.getOffering_id() != null) ps.setInt(2, t.getOffering_id());
        else ps.setNull(2, Types.INTEGER);
        ps.setString(3, t.getDay());
        ps.setTime(4, t.getStart_time());
        ps.setTime(5, t.getEnd_time());
        ps.setString(6, t.getLecture_hall());
        ps.setString(7, t.getSession_type());
    }

    private TimetableEntity map(ResultSet rs) throws SQLException {
        TimetableEntity t = new TimetableEntity();
        t.setTimetable_entity_id(rs.getInt("timetable_entity_id"));
        t.setTimetable_id(rs.getInt("timetable_id"));
        int offering = rs.getInt("offering_id");
        t.setOffering_id(rs.wasNull() ? null : offering);
        t.setDay(rs.getString("day"));
        t.setStart_time(rs.getTime("start_time"));
        t.setEnd_time(rs.getTime("end_time"));
        t.setLecture_hall(rs.getString("lecture_hall"));
        t.setSession_type(rs.getString("session_type"));
        return t;
    }
}
