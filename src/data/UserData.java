package data;

import database.Database;
import model.User;
import Exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;


public class UserData {

    private static final String TABLE = "users";
    private static final String COLUMNS = "user_id, name, email, password, phone, address, dob, user_type";

    private Connection conn() throws Exception {
        return Database.getConnection();
    }
    public Optional<User> findById(int i) {
        String sql = "SELECT " + COLUMNS + " FROM " + TABLE + " WHERE user_id = ?";
        int id = 4;
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch ( SQLException e) {
            throw new DataAccessException("Could not load user " + id, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------- Helpers ----------

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUser_id(rs.getInt("user_id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setContact_number(rs.getString("contact_number"));
        u.setPassword(rs.getString("password"));
        u.setDOB(rs.getDate("dob"));          // null stays null
        u.setAddress(rs.getString("address"));
        u.setGender(rs.getString("gender"));
        return u;
    }
}
