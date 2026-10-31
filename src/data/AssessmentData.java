package data;

import database.Database;
import Exception.DataAccessException;
import model.Assessment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssessmentData {

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    private Assessment map(ResultSet rs) throws SQLException {
        return new Assessment(
                rs.getInt("assessment_id"),
                rs.getInt("offering_id"),
                rs.getString("assessment_name"),
                rs.getString("assessment_type"),
                rs.getDate("submitted_date"),
                rs.getString("reference_number"),
                rs.getDate("assessment_date")
        );
    }

    public Assessment findById(int assessmentId) {
        String sql = "SELECT * FROM assessment WHERE assessment_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, assessmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find assessment by ID: " + assessmentId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Assessment> findByOffering(int offeringId) {
        String sql = "SELECT * FROM assessment WHERE offering_id = ? ORDER BY assessment_id ASC";
        List<Assessment> list = new ArrayList<>();
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
            throw new DataAccessException("Failed to find assessments for offering ID: " + offeringId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Assessment insert(Assessment a) {
        String sql = "INSERT INTO assessment (offering_id, assessment_name, assessment_type, submitted_date, reference_number, assessment_date) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getOfferingId());
            ps.setString(2, a.getAssessmentName());
            ps.setString(3, a.getAssessmentType());

            if (a.getSubmittedDate() != null) {
                ps.setDate(4, a.getSubmittedDate());
            } else {
                ps.setNull(4, Types.DATE);
            }

            ps.setString(5, a.getReferenceNumber());

            if (a.getAssessmentDate() != null) {
                ps.setDate(6, a.getAssessmentDate());
            } else {
                ps.setNull(6, Types.DATE);
            }

            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    a.setAssessmentId(gk.getInt(1));
                }
            }
            return a;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert assessment", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean update(Assessment a) {
        String sql = "UPDATE assessment SET offering_id = ?, assessment_name = ?, assessment_type = ?, " +
                "submitted_date = ?, reference_number = ?, assessment_date = ? WHERE assessment_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, a.getOfferingId());
            ps.setString(2, a.getAssessmentName());
            ps.setString(3, a.getAssessmentType());

            if (a.getSubmittedDate() != null) {
                ps.setDate(4, a.getSubmittedDate());
            } else {
                ps.setNull(4, Types.DATE);
            }

            ps.setString(5, a.getReferenceNumber());

            if (a.getAssessmentDate() != null) {
                ps.setDate(6, a.getAssessmentDate());
            } else {
                ps.setNull(6, Types.DATE);
            }

            ps.setInt(7, a.getAssessmentId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update assessment ID: " + a.getAssessmentId(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(int assessmentId) {
        String sql = "DELETE FROM assessment WHERE assessment_id = ?";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, assessmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete assessment ID: " + assessmentId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}