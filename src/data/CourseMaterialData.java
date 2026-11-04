package data;

import database.Database;
import model.CourseMaterial;
import Exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CourseMaterialData {

    private static final String TABLE = "course_material";
    private static final String COLUMNS =
            "material_id, offering_id, lecturer_id, title, material_type, file_path, file_size, external_link, upload_date";

    // ---------- CREATE ----------

    /** Inserts the material. upload_date is filled by the database. */
    public CourseMaterial insert(CourseMaterial m) {
        String sql = "INSERT INTO " + TABLE
                + " (offering_id, lecturer_id, title, material_type, file_path, file_size, external_link)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setValues(ps, m);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    m.setMaterial_id(keys.getInt(1));
                }
            }
            return m;
        } catch (Exception e) {
            throw new DataAccessException("Could not insert material", e);
        }
    }

    // ---------- READ ----------

    public CourseMaterial findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " WHERE material_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("Could not load material " + id, e);
        }
    }

    public List<CourseMaterial> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " ORDER BY upload_date DESC";
        List<CourseMaterial> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load materials", e);
        }
    }

    public List<CourseMaterial> findByOffering(int offeringId) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE
                + " WHERE offering_id = ? ORDER BY upload_date DESC";
        List<CourseMaterial> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load materials for offering " + offeringId, e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM " + TABLE;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not count materials", e);
        }
    }

    // ---------- UPDATE ----------

    public boolean update(CourseMaterial m) {
        String sql = "UPDATE " + TABLE
                + " SET offering_id = ?, lecturer_id = ?, title = ?, material_type = ?,"
                + " file_path = ?, file_size = ?, external_link = ? WHERE material_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            setValues(ps, m);
            ps.setInt(8, m.getMaterial_id());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not update material " + m.getMaterial_id(), e);
        }
    }

    // ---------- DELETE ----------

    public boolean delete(int id) {
        String sql = "DELETE FROM " + TABLE + " WHERE material_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not delete material " + id, e);
        }
    }

    /** Sets parameters 1-7 (shared by insert and update). */
    private void setValues(PreparedStatement ps, CourseMaterial m) throws SQLException {
        if (m.getOffering_id() != null) ps.setInt(1, m.getOffering_id());
        else ps.setNull(1, Types.INTEGER);
        if (m.getLecturer_id() != null) ps.setInt(2, m.getLecturer_id());
        else ps.setNull(2, Types.INTEGER);
        ps.setString(3, m.getTitle());
        ps.setString(4, m.getMaterial_type());
        ps.setString(5, m.getFile_path());
        if (m.getFile_size() != null) ps.setLong(6, m.getFile_size());
        else ps.setNull(6, Types.BIGINT);
        ps.setString(7, m.getExternal_link());
    }

    private CourseMaterial map(ResultSet rs) throws SQLException {
        CourseMaterial m = new CourseMaterial();
        m.setMaterial_id(rs.getInt("material_id"));
        int offering = rs.getInt("offering_id");
        m.setOffering_id(rs.wasNull() ? null : offering);
        int lecturer = rs.getInt("lecturer_id");
        m.setLecturer_id(rs.wasNull() ? null : lecturer);
        m.setTitle(rs.getString("title"));
        m.setMaterial_type(rs.getString("material_type"));
        m.setFile_path(rs.getString("file_path"));
        long size = rs.getLong("file_size");
        m.setFile_size(rs.wasNull() ? null : size);
        m.setExternal_link(rs.getString("external_link"));
        m.setUpload_date(rs.getTimestamp("upload_date"));
        return m;
    }
}
