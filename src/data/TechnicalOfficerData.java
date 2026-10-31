
package data;

import database.Database;
import model.TechnicalOfficer;
import Exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TechnicalOfficerData {

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    // 1. Find technical officer by user ID
    public TechnicalOfficer findById(int userId) {

        String sql = "SELECT users.user_id, users.name, users.email, " +
                "users.password, users.phone, users.address, " +
                "users.dob, users.user_type, " +
                "technical_officer.dep_id " +
                "FROM technical_officer JOIN users USING(user_id) " +
                "WHERE technical_officer.user_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not load technical officer " + userId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 2. Find all technical officers
    public List<TechnicalOfficer> findAll() {

        String sql = "SELECT users.user_id, users.name, users.email, " +
                "users.password, users.phone, users.address, " +
                "users.dob, users.user_type, " +
                "technical_officer.dep_id " +
                "FROM technical_officer JOIN users USING(user_id) " +
                "ORDER BY users.user_id";

        List<TechnicalOfficer> officers = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                officers.add(map(rs));
            }

            return officers;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not load technical officers", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 3. Insert technical officer
    public TechnicalOfficer insert(TechnicalOfficer t) {

        String sql = "INSERT INTO technical_officer(user_id, dep_id) " +
                "VALUES (?, ?)";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, t.getUser_id());
            ps.setInt(2, t.getDep_id());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                return t;
            }

            throw new SQLException("Technical officer insert failed.");

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not insert technical officer " + t.getUser_id(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 4. Update technical officer
    public boolean update(TechnicalOfficer t) {

        String sql = "UPDATE technical_officer SET dep_id = ? " +
                "WHERE user_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, t.getDep_id());
            ps.setInt(2, t.getUser_id());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not update technical officer " + t.getUser_id(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 5. Delete technical officer
    public boolean delete(int userId) {

        String sql = "DELETE FROM technical_officer WHERE user_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not delete technical officer " + userId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 6. Convert ResultSet into TechnicalOfficer object
    private TechnicalOfficer map(ResultSet rs) throws SQLException {

        TechnicalOfficer t = new TechnicalOfficer();

        // User details
        t.setUser_id(rs.getInt("user_id"));
        t.setName(rs.getString("name"));
        t.setEmail(rs.getString("email"));
        t.setPassword(rs.getString("password"));
        t.setContact_number(rs.getString("phone"));
        t.setAddress(rs.getString("address"));
        t.setDOB(rs.getDate("dob"));
        t.setUserType(rs.getString("user_type"));

        // Technical officer details
        t.setDep_id(rs.getInt("dep_id"));

        return t;
    }
}
