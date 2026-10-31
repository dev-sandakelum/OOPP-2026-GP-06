package data;

import database.Database;
import Exception.DataAccessException;
import model.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionData {

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    private Session map(ResultSet rs) throws SQLException {
        return new Session(
                rs.getInt("session_id"),
                rs.getInt("offering_id"),
                rs.getInt("session_number"),
                rs.getString("session_type"),
                rs.getDate("session_date"),
                rs.getTime("start_time"),
                rs.getTime("end_time"),
                rs.getString("room")
        );
    }

    public Session findById(int sessionId) {
        String sql = "SELECT * FROM session WHERE session_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find session by ID: " + sessionId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Session> findByOffering(int offeringId) {
        String sql = "SELECT * FROM session WHERE offering_id = ? ORDER BY session_number ASC";
        List<Session> list = new ArrayList<>();
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find sessions for offering ID: " + offeringId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Session insert(Session s) {
        String sql = "INSERT INTO session (offering_id, session_number, session_type, session_date, start_time, end_time, room) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getOfferingId());
            ps.setInt(2, s.getSessionNumber());
            ps.setString(3, s.getSessionType());
            ps.setDate(4, s.getSessionDate());
            ps.setTime(5, s.getStartTime());
            ps.setTime(6, s.getEndTime());
            ps.setString(7, s.getRoom());

            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    s.setSessionId(gk.getInt(1));
                }
            }
            return s;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert session", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean update(Session s) {
        String sql = "UPDATE session SET offering_id = ?, session_number = ?, session_type = ?, " +
                "session_date = ?, start_time = ?, end_time = ?, room = ? WHERE session_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, s.getOfferingId());
            ps.setInt(2, s.getSessionNumber());
            ps.setString(3, s.getSessionType());
            ps.setDate(4, s.getSessionDate());
            ps.setTime(5, s.getStartTime());
            ps.setTime(6, s.getEndTime());
            ps.setString(7, s.getRoom());
            ps.setInt(8, s.getSessionId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update session ID: " + s.getSessionId(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(int sessionId) {
        String sql = "DELETE FROM session WHERE session_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete session ID: " + sessionId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}