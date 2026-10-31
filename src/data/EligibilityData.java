package data;

import database.Database;
import Exception.DataAccessException;
import model.Eligibility;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EligibilityData {

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    private Eligibility map(ResultSet rs) throws SQLException {
        return new Eligibility(
                rs.getInt("eligibility_id"),
                rs.getInt("student_id"),
                rs.getInt("offering_id"),
                rs.getBigDecimal("ca_average"),
                rs.getBigDecimal("attendance_percentage"),
                rs.getString("eligibility_status")
        );
    }

    public Eligibility find(int studentId, int offeringId) {
        String sql = "SELECT * FROM eligibility WHERE student_id = ? AND offering_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find eligibility for student " + studentId + ", offering " + offeringId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Eligibility> findByOffering(int offeringId) {
        String sql = "SELECT * FROM eligibility WHERE offering_id = ?";
        List<Eligibility> list = new ArrayList<>();
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
            throw new DataAccessException("Failed to find eligibility for offering ID: " + offeringId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Eligibility> findByStudent(int studentId) {
        String sql = "SELECT * FROM eligibility WHERE student_id = ?";
        List<Eligibility> list = new ArrayList<>();
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find eligibility for student ID: " + studentId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Eligibility insert(Eligibility e) {
        String sql = "INSERT INTO eligibility (student_id, offering_id, ca_average, attendance_percentage, eligibility_status) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getStudentId());
            ps.setInt(2, e.getOfferingId());
            ps.setBigDecimal(3, e.getCaAverage());
            ps.setBigDecimal(4, e.getAttendancePercentage());
            ps.setString(5, e.getEligibilityStatus());

            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    e.setEligibilityId(gk.getInt(1));
                }
            }
            return e;
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to insert eligibility record", ex);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean update(Eligibility e) {
        String sql = "UPDATE eligibility SET student_id = ?, offering_id = ?, ca_average = ?, " +
                "attendance_percentage = ?, eligibility_status = ? WHERE eligibility_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, e.getStudentId());
            ps.setInt(2, e.getOfferingId());
            ps.setBigDecimal(3, e.getCaAverage());
            ps.setBigDecimal(4, e.getAttendancePercentage());
            ps.setString(5, e.getEligibilityStatus());
            ps.setInt(6, e.getEligibilityId());

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to update eligibility ID: " + e.getEligibilityId(), ex);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean delete(int eligibilityId) {
        String sql = "DELETE FROM eligibility WHERE eligibility_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, eligibilityId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete eligibility ID: " + eligibilityId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}