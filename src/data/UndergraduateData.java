
package data;

import database.Database;
import model.Undergraduate;
import Exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UndergraduateData {

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    // 1. Find undergraduate by user ID
    public Undergraduate findById(int userId) {

        String sql = "SELECT users.user_id, users.name, users.email, " +
                "users.password, users.phone, users.address, " +
                "users.dob, users.user_type, " +
                "undergraduate.dep_id, undergraduate.batch_id, " +
                "undergraduate.profile_picture " +
                "FROM undergraduate JOIN users USING(user_id) " +
                "WHERE undergraduate.user_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not load undergraduate " + userId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 2. Find all undergraduates
    public List<Undergraduate> findAll() {

        String sql = "SELECT users.user_id, users.name, users.email, " +
                "users.password, users.phone, users.address, " +
                "users.dob, users.user_type, " +
                "undergraduate.dep_id, undergraduate.batch_id, " +
                "undergraduate.profile_picture " +
                "FROM undergraduate JOIN users USING(user_id) " +
                "ORDER BY users.user_id";

        List<Undergraduate> undergraduates = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                undergraduates.add(map(rs));
            }

            return undergraduates;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not load undergraduates", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 3. Find undergraduates by batch
    public List<Undergraduate> findByBatch(int batchId) {

        String sql = "SELECT users.user_id, users.name, users.email, " +
                "users.password, users.phone, users.address, " +
                "users.dob, users.user_type, " +
                "undergraduate.dep_id, undergraduate.batch_id, " +
                "undergraduate.profile_picture " +
                "FROM undergraduate JOIN users USING(user_id) " +
                "WHERE undergraduate.batch_id = ? " +
                "ORDER BY users.user_id";

        List<Undergraduate> undergraduates = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, batchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    undergraduates.add(map(rs));
                }
            }

            return undergraduates;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not load undergraduates for batch " + batchId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 4. Find undergraduates by department
    public List<Undergraduate> findByDepartment(int depId) {

        String sql = "SELECT users.user_id, users.name, users.email, " +
                "users.password, users.phone, users.address, " +
                "users.dob, users.user_type, " +
                "undergraduate.dep_id, undergraduate.batch_id, " +
                "undergraduate.profile_picture " +
                "FROM undergraduate JOIN users USING(user_id) " +
                "WHERE undergraduate.dep_id = ? " +
                "ORDER BY users.user_id";

        List<Undergraduate> undergraduates = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, depId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    undergraduates.add(map(rs));
                }
            }

            return undergraduates;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not load undergraduates for department " + depId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 5. Insert undergraduate
    public Undergraduate insert(Undergraduate u) {

        String sql = "INSERT INTO undergraduate " +
                "(user_id, dep_id, batch_id, profile_picture) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, u.getUser_id());
            ps.setInt(2, u.getDep_id());
            ps.setInt(3, u.getBatch_id());
            ps.setString(4, u.getProfile_picture());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                return u;
            }

            throw new SQLException("Undergraduate insert failed.");

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not insert undergraduate " + u.getUser_id(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 6. Update undergraduate
    public boolean update(Undergraduate u) {

        String sql = "UPDATE undergraduate SET dep_id = ?, " +
                "batch_id = ?, profile_picture = ? WHERE user_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, u.getDep_id());
            ps.setInt(2, u.getBatch_id());
            ps.setString(3, u.getProfile_picture());
            ps.setInt(4, u.getUser_id());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not update undergraduate " + u.getUser_id(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 7. Delete undergraduate
    public boolean delete(int userId) {

        String sql = "DELETE FROM undergraduate WHERE user_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not delete undergraduate " + userId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 8. Convert ResultSet into Undergraduate object
    private Undergraduate map(ResultSet rs) throws SQLException {

        Undergraduate u = new Undergraduate();

        // User details
        u.setUser_id(rs.getInt("user_id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setContact_number(rs.getString("phone"));
        u.setAddress(rs.getString("address"));
        u.setDOB(rs.getDate("dob"));
        u.setUserType(rs.getString("user_type"));

        // Undergraduate details
        u.setDep_id(rs.getInt("dep_id"));
        u.setBatch_id(rs.getInt("batch_id"));
        u.setProfile_picture(rs.getString("profile_picture"));

        return u;
    }
}
