
package data;

import database.Database;
import model.Lecturer;
import Exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LecturerData {

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    // 1. Find lecturer by user ID
    public Lecturer findById(int userId) {

        String sql = "SELECT users.user_id, users.name, users.email, " +
                "users.password, users.phone, users.address, " +
                "users.dob, users.user_type, " +
                "lecturer.qualification, lecturer.dep_id " +
                "FROM lecturer JOIN users USING(user_id) " +
                "WHERE lecturer.user_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not load lecturer " + userId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 2. Find all lecturers
    public List<Lecturer> findAll() {

        String sql = "SELECT users.user_id, users.name, users.email, " +
                "users.password, users.phone, users.address, " +
                "users.dob, users.user_type, " +
                "lecturer.qualification, lecturer.dep_id " +
                "FROM lecturer JOIN users USING(user_id) " +
                "ORDER BY users.user_id";

        List<Lecturer> lecturers = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lecturers.add(map(rs));
            }

            return lecturers;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not load lecturers", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 3. Find lecturers by department
    public List<Lecturer> findByDepartment(int depId) {

        String sql = "SELECT users.user_id, users.name, users.email, " +
                "users.password, users.phone, users.address, " +
                "users.dob, users.user_type, " +
                "lecturer.qualification, lecturer.dep_id " +
                "FROM lecturer JOIN users USING(user_id) " +
                "WHERE lecturer.dep_id = ? " +
                "ORDER BY users.user_id";

        List<Lecturer> lecturers = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, depId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lecturers.add(map(rs));
                }
            }

            return lecturers;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not load lecturers for department " + depId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 4. Insert lecturer
    public Lecturer insert(Lecturer l) {

        String sql = "INSERT INTO lecturer (user_id, qualification, dep_id) " +
                "VALUES (?, ?, ?)";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, l.getUser_id());
            ps.setString(2, l.getQualification());
            ps.setInt(3, l.getDep_id());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                return l;
            }

            throw new SQLException("Lecturer insert failed.");

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not insert lecturer " + l.getUser_id(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 5. Update lecturer
    public boolean update(Lecturer l) {

        String sql = "UPDATE lecturer SET qualification = ?, dep_id = ? " +
                "WHERE user_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, l.getQualification());
            ps.setInt(2, l.getDep_id());
            ps.setInt(3, l.getUser_id());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not update lecturer " + l.getUser_id(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 6. Delete lecturer
    public boolean delete(int userId) {

        String sql = "DELETE FROM lecturer WHERE user_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not delete lecturer " + userId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 7. Convert ResultSet into Lecturer object
    private Lecturer map(ResultSet rs) throws SQLException {

        Lecturer l = new Lecturer();

        // User details
        l.setUser_id(rs.getInt("user_id"));
        l.setName(rs.getString("name"));
        l.setEmail(rs.getString("email"));
        l.setPassword(rs.getString("password"));
        l.setContact_number(rs.getString("phone"));
        l.setAddress(rs.getString("address"));
        l.setDOB(rs.getDate("dob"));
        l.setUserType(rs.getString("user_type"));

        // Lecturer details
        l.setQualification(rs.getString("qualification"));
        l.setDep_id(rs.getInt("dep_id"));

        return l;
    }
}
