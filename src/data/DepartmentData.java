
package data;

import database.Database;
import model.Department;
import Exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DepartmentData {

    private static final String TABLE = "department";

    private static final String COLUMNS =
            "dep_id, dep_name, contact_no";

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    // INSERT - Add a new department
    public Department insert(Department d) {

        String sql = "INSERT INTO " + TABLE
                + " (dep_name, contact_no) "
                + "VALUES (?, ?)";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, d.getDep_name());
            ps.setString(2, d.getContact_no());

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new DataAccessException(
                        "Failed to insert department.", null
                );
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {

                if (keys.next()) {
                    d.setDep_id(keys.getInt(1));
                } else {
                    throw new DataAccessException(
                            "Failed to retrieve department ID.", null
                    );
                }
            }

            return d;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Error inserting department: " + e.getMessage(), e
            );

        } catch (Exception e) {
            if (e instanceof DataAccessException) {
                throw (DataAccessException) e;
            }

            throw new DataAccessException(
                    "Database connection error: " + e.getMessage(), e
            );
        }
    }

    // FIND BY ID - Find a department using its ID
    public Department findById(int id) {

        String sql = "SELECT " + COLUMNS
                + " FROM " + TABLE
                + " WHERE dep_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not load department " + id, e
            );

        } catch (Exception e) {
            if (e instanceof DataAccessException) {
                throw (DataAccessException) e;
            }

            throw new DataAccessException(
                    "Database connection error: " + e.getMessage(), e
            );
        }
    }

    // FIND ALL - Retrieve all departments
    public ArrayList<Department> findAll() {

        String sql = "SELECT " + COLUMNS + " FROM " + TABLE;

        ArrayList<Department> departments = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                departments.add(map(rs));
            }

            return departments;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not load departments: " + e.getMessage(), e
            );

        } catch (Exception e) {
            if (e instanceof DataAccessException) {
                throw (DataAccessException) e;
            }

            throw new DataAccessException(
                    "Database connection error: " + e.getMessage(), e
            );
        }
    }

    // COUNT - Count total departments
    public int count() {

        String sql = "SELECT COUNT(*) FROM " + TABLE;

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not count departments: " + e.getMessage(), e
            );

        } catch (Exception e) {
            if (e instanceof DataAccessException) {
                throw (DataAccessException) e;
            }

            throw new DataAccessException(
                    "Database connection error: " + e.getMessage(), e
            );
        }
    }

    // UPDATE - Update department details
    public boolean update(Department d) {

        String sql = "UPDATE " + TABLE
                + " SET dep_name = ?, contact_no = ?"
                + " WHERE dep_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, d.getDep_name());
            ps.setString(2, d.getContact_no());
            ps.setInt(3, d.getDep_id());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not update department: " + e.getMessage(), e
            );

        } catch (Exception e) {
            if (e instanceof DataAccessException) {
                throw (DataAccessException) e;
            }

            throw new DataAccessException(
                    "Database connection error: " + e.getMessage(), e
            );
        }
    }

    // DELETE - Delete a department using its ID
    public boolean delete(int id) {

        String sql = "DELETE FROM " + TABLE
                + " WHERE dep_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not delete department: " + e.getMessage(), e
            );

        } catch (Exception e) {
            if (e instanceof DataAccessException) {
                throw (DataAccessException) e;
            }

            throw new DataAccessException(
                    "Database connection error: " + e.getMessage(), e
            );
        }
    }

    // MAP - Convert a database row into a Department object
    private Department map(ResultSet rs) throws SQLException {

        Department d = new Department();

        d.setDep_id(rs.getInt("dep_id"));
        d.setDep_name(rs.getString("dep_name"));
        d.setContact_no(rs.getString("contact_no"));

        return d;
    }
}
