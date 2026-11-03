package data;

import database.Database;
import Exception.DataAccessException;
import model.Medical;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MedicalData {

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    private Medical map(ResultSet rs) throws SQLException {
        Medical m = new Medical();
        m.setMedicalId(rs.getInt("medical_id"));
        m.setStudentId(rs.getInt("student_id"));
        m.setMedicalDate(rs.getDate("medical_date"));
        m.setReason(rs.getString("reason"));
        m.setCertificatePath(rs.getString("certificate_path"));
        m.setStatus(rs.getString("status"));
        m.setReviewerNote(rs.getString("reviewer_note"));
        m.setReviewedDate(rs.getDate("reviewed_date"));
        return m;
    }

    public Medical findById(int medicalId) {
        String sql = "SELECT * FROM medical WHERE medical_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, medicalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find medical record by ID: " + medicalId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Medical> findByStudent(int studentId) {
        String sql = "SELECT * FROM medical WHERE student_id = ? ORDER BY medical_date DESC";
        List<Medical> list = new ArrayList<>();
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
            throw new DataAccessException("Failed to find medical records for student ID: " + studentId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Medical> findByStatus(String status) {
        String sql = "SELECT * FROM medical WHERE status = ? ORDER BY medical_date DESC";
        List<Medical> list = new ArrayList<>();
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find medical records by status: " + status, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Medical insert(Medical m) {
        String sql = "INSERT INTO medical (student_id, medical_date, reason, certificate_path, status, reviewer_note, reviewed_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getStudentId());
            ps.setDate(2, m.getMedicalDate());
            ps.setString(3, m.getReason());
            ps.setString(4, m.getCertificatePath());
            ps.setString(5, m.getStatus() != null ? m.getStatus() : "PENDING");
            ps.setString(6, m.getReviewerNote());

            if (m.getReviewedDate() != null) {
                ps.setDate(7, m.getReviewedDate());
            } else {
                ps.setNull(7, Types.DATE);
            }

            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    m.setMedicalId(gk.getInt(1));
                }
            }
            return m;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert medical record", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateStatus(int medicalId, String status, String reviewerNote, Date reviewedDate) {
        String sql = "UPDATE medical SET status = ?, reviewer_note = ?, reviewed_date = ? WHERE medical_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, reviewerNote);
            if (reviewedDate != null) {
                ps.setDate(3, reviewedDate);
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setInt(4, medicalId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update status for medical ID: " + medicalId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(int medicalId) {
        String sql = "DELETE FROM medical WHERE medical_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, medicalId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete medical record ID: " + medicalId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}