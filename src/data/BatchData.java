package data;

import database.Database;
import model.Batch;
import Exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class BatchData {

    private static final String TABLE = "batch";
    private static final String COLUMNS =
            "batch_id, batch_name, intake_year, dep_id";

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    // Find batch by ID
    public Batch findById(int batchId) {
        String sql = "SELECT " + COLUMNS +
                " FROM " + TABLE +
                " WHERE batch_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, batchId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not load batch " + batchId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Find all batches
    public List<Batch> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE;

        List<Batch> batches = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                batches.add(map(rs));
            }

            return batches;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not load batches", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Find batches by department
    public List<Batch> findByDepartment(int depId) {
        String sql = "SELECT " + COLUMNS +
                " FROM " + TABLE +
                " WHERE dep_id = ?";

        List<Batch> batches = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, depId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    batches.add(map(rs));
                }
            }

            return batches;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not load batches for department " + depId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Insert new batch
    public Batch insert(Batch b) {
        String sql = "INSERT INTO " + TABLE +
                " (batch_name, intake_year, dep_id) VALUES (?, ?, ?)";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, b.getBatch_name());
            ps.setInt(2, b.getIntake_year());

            if (b.getDep_id() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, b.getDep_id());
            }

            int rows = ps.executeUpdate();

            if (rows == 0) {
                return null;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    b.setBatch_id(keys.getInt(1));
                    return b;
                } else {
                    throw new SQLException(
                            "Creating batch failed, no generated ID returned.");
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not insert batch", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Update existing batch
    public boolean update(Batch b) {
        String sql = "UPDATE " + TABLE +
                " SET batch_name = ?, intake_year = ?, dep_id = ?" +
                " WHERE batch_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, b.getBatch_name());
            ps.setInt(2, b.getIntake_year());

            if (b.getDep_id() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, b.getDep_id());
            }

            ps.setInt(4, b.getBatch_id());

            int rows = ps.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not update batch " + b.getBatch_id(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Delete batch
    public boolean delete(int batchId) {
        String sql = "DELETE FROM " + TABLE +
                " WHERE batch_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, batchId);

            int rows = ps.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not delete batch " + batchId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Convert ResultSet row into Batch object
    private Batch map(ResultSet rs) throws SQLException {
        Batch b = new Batch();

        b.setBatch_id(rs.getInt("batch_id"));
        b.setBatch_name(rs.getString("batch_name"));
        b.setIntake_year(rs.getInt("intake_year"));

        int depId = rs.getInt("dep_id");
        b.setDep_id(rs.wasNull() ? null : depId);

        return b;
    }
}