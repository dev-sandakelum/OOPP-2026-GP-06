package data;

import database.Database;
import model.Department;
import Exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DepartmentData {

    private static final String TABLE = "department";

    private static final String COLUMNS =
            "dep_id, dep_name, contact_no";

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    public Department insert(Department d) {

        String sql = "INSERT INTO " + TABLE
                + " (dep_name, contact_no) "
                + "VALUES (?, ?)";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set department details
            ps.setString(1, d.getDep_name());
            ps.setString(2, d.getContact_no());

            // Execute INSERT query
            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new DataAccessException(
                        "Failed to insert department.", null
                );
            }

            // Retrieve generated department ID
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
}
