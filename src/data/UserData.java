package data;

import database.Database;
import model.User;
import Exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class UserData {

    private static final String TABLE = "users";
    private static final String COLUMNS = "user_id, name, email, password, phone, address, dob, user_type";

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    // ---------- CREATE ----------

    /** Inserts the user and sets the generated user_id on the object. */
    public User insert(User u) {
        String sql = "INSERT INTO " + TABLE
                + " (name, email, password, phone, address, dob, user_type)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPassword());   // store a HASH, not plain text
            ps.setString(4, u.getContact_number());
            ps.setString(5, u.getAddress());
            if (u.getDOB() != null) ps.setDate(6, u.getDOB());
            else ps.setNull(6, Types.DATE);
            ps.setString(7, u.getUserType());

            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("Insert failed, no rows affected", null);
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    u.setUser_id(keys.getInt(1));
                }
            }
            return u;
        } catch (SQLException e) {
            throw new DataAccessException("Could not insert user", e);
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------- READ ----------

    public User findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " WHERE user_id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load user " + id, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " ORDER BY user_id";
        List<User> users = new ArrayList<>();
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(map(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new DataAccessException("Could not load users", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM " + TABLE;
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not count users", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------- UPDATE ----------

    /** Updates profile fields. Does NOT touch the password. */
    public boolean update(User u) {
        String sql = "UPDATE " + TABLE
                + " SET name = ?, email = ?, phone = ?, address = ?, dob = ?, user_type = ?"
                + " WHERE user_id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getContact_number());
            ps.setString(4, u.getAddress());
            if (u.getDOB() != null) ps.setDate(5, u.getDOB());
            else ps.setNull(5, Types.DATE);
            ps.setString(6, u.getUserType());
            ps.setInt(7, u.getUser_id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not update user " + u.getUser_id(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Pass an already-hashed password. */
    public boolean updatePassword(int id, String passwordHash) {
        String sql = "UPDATE " + TABLE + " SET password = ? WHERE user_id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not update password for user " + id, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------- DELETE ----------

    public boolean delete(int id) {
        String sql = "DELETE FROM " + TABLE + " WHERE user_id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete user " + id, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Returns the user matching email + password, or null if no match. */
    public User login(String email, String password) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE
                + " WHERE email = ? AND password = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Login query failed", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUser_id(rs.getInt("user_id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setContact_number(rs.getString("phone"));
        u.setAddress(rs.getString("address"));
        u.setDOB(rs.getDate("dob"));
        u.setUserType(rs.getString("user_type"));
        return u;
    }
}