package data;

import database.Database;
import model.Notice;
import Exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class NoticeData {

    private static final String TABLE = "notice";
    private static final String COLUMNS =
            "notice_id, created_by, title, description, created_date, audience, expiry_date, status";

    // ---------- CREATE ----------

    /** Inserts the notice. created_date is filled by the database. */
    public Notice insert(Notice n) {
        String sql = "INSERT INTO " + TABLE
                + " (created_by, title, description, audience, expiry_date, status)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setValues(ps, n);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    n.setNotice_id(keys.getInt(1));
                }
            }
            return n;
        } catch (Exception e) {
            throw new DataAccessException("Could not insert notice", e);
        }
    }

    // ---------- READ ----------

    public Notice findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " WHERE notice_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("Could not load notice " + id, e);
        }
    }

    public List<Notice> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " ORDER BY created_date DESC";
        List<Notice> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load notices", e);
        }
    }

    /** Notices for one audience (for example "Student") that are not expired. */
    public List<Notice> findByAudience(String audience) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE
                + " WHERE audience = ? AND (expiry_date IS NULL OR expiry_date >= CURRENT_DATE)"
                + " ORDER BY created_date DESC";
        List<Notice> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, audience);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load notices for " + audience, e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM " + TABLE;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not count notices", e);
        }
    }

    // ---------- UPDATE ----------

    public boolean update(Notice n) {
        String sql = "UPDATE " + TABLE
                + " SET created_by = ?, title = ?, description = ?, audience = ?, expiry_date = ?, status = ?"
                + " WHERE notice_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            setValues(ps, n);
            ps.setInt(7, n.getNotice_id());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not update notice " + n.getNotice_id(), e);
        }
    }

    // ---------- DELETE ----------

    public boolean delete(int id) {
        String sql = "DELETE FROM " + TABLE + " WHERE notice_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not delete notice " + id, e);
        }
    }

    /** Sets parameters 1-6 (shared by insert and update). */
    private void setValues(PreparedStatement ps, Notice n) throws SQLException {
        if (n.getCreated_by() != null) ps.setInt(1, n.getCreated_by());
        else ps.setNull(1, Types.INTEGER);
        ps.setString(2, n.getTitle());
        ps.setString(3, n.getDescription());
        ps.setString(4, n.getAudience());
        ps.setDate(5, n.getExpiry_date());
        ps.setString(6, n.getStatus());
    }

    private Notice map(ResultSet rs) throws SQLException {
        Notice n = new Notice();
        n.setNotice_id(rs.getInt("notice_id"));
        int by = rs.getInt("created_by");
        n.setCreated_by(rs.wasNull() ? null : by);
        n.setTitle(rs.getString("title"));
        n.setDescription(rs.getString("description"));
        n.setCreated_date(rs.getTimestamp("created_date"));
        n.setAudience(rs.getString("audience"));
        n.setExpiry_date(rs.getDate("expiry_date"));
        n.setStatus(rs.getString("status"));
        return n;
    }
}
