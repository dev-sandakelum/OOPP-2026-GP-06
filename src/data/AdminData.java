
package data;

import database.Database;
import model.Admin;
import Exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminData {

    private static final String TABLE = "admin";

    private Connection conn() throws Exception {
        return Database.getConnection();
    }


    // 1. Find admin by user ID
    public Admin findById(int userId) {

        String sql = "SELECT users.user_id, users.name, users.email, " +
                "users.password, users.phone, users.address, " +
                "users.dob, users.user_type " +
                "FROM admin JOIN users USING(user_id) " +
                "WHERE user_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not load admin " + userId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // 2. Find all admins
    public List<Admin> findAll() {

        String sql = "SELECT users.user_id, users.name, users.email, " +
                "users.password, users.phone, users.address, " +
                "users.dob, users.user_type " +
                "FROM admin JOIN users USING(user_id) " +
                "ORDER BY user_id";

        List<Admin> admins = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                admins.add(map(rs));
            }

            return admins;

        } catch (SQLException e) {
            throw new DataAccessException("Could not load admins", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // 3. Insert admin
    // The corresponding users record must already exist.
    public Admin insert(Admin a) {

        String sql = "INSERT INTO " + TABLE + " (user_id) VALUES (?)";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, a.getUser_id());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                return a;
            }

            throw new SQLException("Admin insert failed.");

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not insert admin " + a.getUser_id(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // 4. Delete admin
    public boolean delete(int userId) {

        String sql = "DELETE FROM " + TABLE + " WHERE user_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not delete admin " + userId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // 5. Convert ResultSet row into Admin object
    private Admin map(ResultSet rs) throws SQLException {

        Admin a = new Admin();

        a.setUser_id(rs.getInt("user_id"));
        a.setName(rs.getString("name"));
        a.setEmail(rs.getString("email"));
        a.setPassword(rs.getString("password"));
        a.setContact_number(rs.getString("phone"));
        a.setAddress(rs.getString("address"));
        a.setDOB(rs.getDate("dob"));
        a.setUserType(rs.getString("user_type"));

        return a;
    }
}
