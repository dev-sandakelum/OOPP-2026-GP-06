
package data;

import database.Database;
import model.Semester;
import Exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SemesterData {

    private static final String TABLE = "semester";

    private static final String COLUMNS =
            "semester_id, semester_number, academic_year, start_date, end_date, status";


    // 1. Find semester by ID
    public Semester findById(int semesterId) {

        String sql = "SELECT " + COLUMNS +
                " FROM " + TABLE + " WHERE semester_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, semesterId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not load semester " + semesterId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // 2. Find all semesters
    public List<Semester> findAll() {

        String sql = "SELECT " + COLUMNS + " FROM " + TABLE;

        List<Semester> semesters = new ArrayList<>();

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                semesters.add(map(rs));
            }

            return semesters;

        } catch (SQLException e) {
            throw new DataAccessException("Could not load semesters", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // 3. Find ongoing semester
    public Semester findOngoing() {

        String sql = "SELECT " + COLUMNS +
                " FROM " + TABLE +
                " WHERE status = 'ONGOING' LIMIT 1";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? map(rs) : null;

        } catch (SQLException e) {
            throw new DataAccessException("Could not load ongoing semester", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // 4. Insert a new semester
    public Semester insert(Semester s) {

        String sql = "INSERT INTO " + TABLE +
                " (semester_number, academic_year, start_date, end_date, status)" +
                " VALUES (?, ?, ?, ?, ?)";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, s.getSemester_number());
            ps.setString(2, s.getAcademic_year());

            if (s.getStart_date() == null) {
                ps.setNull(3, Types.DATE);
            } else {
                ps.setDate(3, s.getStart_date());
            }

            if (s.getEnd_date() == null) {
                ps.setNull(4, Types.DATE);
            } else {
                ps.setDate(4, s.getEnd_date());
            }

            ps.setString(5, s.getStatus());

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Inserting semester failed.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {

                if (keys.next()) {
                    s.setSemester_id(keys.getInt(1));
                    return s;
                } else {
                    throw new SQLException("No generated ID returned.");
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not insert semester", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // 5. Update semester
    public boolean update(Semester s) {

        String sql = "UPDATE " + TABLE +
                " SET semester_number = ?, academic_year = ?, " +
                "start_date = ?, end_date = ?, status = ?" +
                " WHERE semester_id = ?";

        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, s.getSemester_number());
            ps.setString(2, s.getAcademic_year());

            if (s.getStart_date() == null) {
                ps.setNull(3, Types.DATE);
            } else {
                ps.setDate(3, s.getStart_date());
            }

            if (s.getEnd_date() == null) {
                ps.setNull(4, Types.DATE);
            } else {
                ps.setDate(4, s.getEnd_date());
            }

            ps.setString(5, s.getStatus());
            ps.setInt(6, s.getSemester_id());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not update semester " + s.getSemester_id(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // 6. Convert ResultSet row into Semester object
    private Semester map(ResultSet rs) throws SQLException {

        Semester s = new Semester();

        s.setSemester_id(rs.getInt("semester_id"));
        s.setSemester_number(rs.getInt("semester_number"));
        s.setAcademic_year(rs.getString("academic_year"));
        s.setStart_date(rs.getDate("start_date"));
        s.setEnd_date(rs.getDate("end_date"));
        s.setStatus(rs.getString("status"));

        return s;
    }


    // Database connection
    private Connection conn() throws Exception {
        return Database.getConnection();
    }
}
