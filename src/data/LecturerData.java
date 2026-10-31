package data;

import database.Database;
import model.Assessment;
import model.CourseMaterial;
import model.Exam;
import model.Lecturer;
import model.Notice;
import model.Result;
import model.Session;
import Exception.DataAccessException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * All database queries used by the lecturer pages.
 *
 * Simple rows (one table) are returned as model objects.
 * Rows that join several tables are returned as String[] so the UI
 * can put them straight into a table (the column order is written above each method).
 */
public class LecturerData {

    private final UserData userData = new UserData();

    private Connection conn() throws Exception {
        return Database.getConnection();
    }

    // =====================================================================
    //  LECTURER  (My Profile page + admin)
    // =====================================================================

    /** Inserts a row in users AND in lecturer. Sets the new user_id on the object. */
    public Lecturer insertLecturer(Lecturer l) {
        l.setUserType("LECTURER");
        userData.insert(l);   // fills l.getUser_id()

        String sql = "INSERT INTO lecturer (user_id, qualification, dep_id) VALUES (?, ?, ?)";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, l.getUser_id());
            ps.setString(2, l.getQualification());
            ps.setInt(3, l.getDep_id());
            ps.executeUpdate();
            return l;
        } catch (Exception e) {
            throw new DataAccessException("Could not insert lecturer", e);
        }
    }

    public Lecturer findLecturerById(int id) {
        String sql = "SELECT u.*, l.qualification, l.dep_id"
                + " FROM users u JOIN lecturer l ON l.user_id = u.user_id"
                + " WHERE u.user_id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapLecturer(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("Could not load lecturer " + id, e);
        }
    }

    public List<Lecturer> findAllLecturers() {
        String sql = "SELECT u.*, l.qualification, l.dep_id"
                + " FROM users u JOIN lecturer l ON l.user_id = u.user_id"
                + " ORDER BY u.name";
        List<Lecturer> list = new ArrayList<>();
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapLecturer(rs));
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load lecturers", e);
        }
    }

    /** Updates the users row and the lecturer row. Does NOT change the password. */
    public boolean updateLecturer(Lecturer l) {
        userData.update(l);

        String sql = "UPDATE lecturer SET qualification = ?, dep_id = ? WHERE user_id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, l.getQualification());
            ps.setInt(2, l.getDep_id());
            ps.setInt(3, l.getUser_id());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not update lecturer " + l.getUser_id(), e);
        }
    }

    /** Deleting the users row also deletes the lecturer row (ON DELETE CASCADE). */
    public boolean deleteLecturer(int id) {
        return userData.delete(id);
    }

    // =====================================================================
    //  MY COURSES LIST  (used by the course drop-down on most pages)
    // =====================================================================

    /** Columns: {offering_id, course_code, course_name} */
    public List<String[]> getMyCourses(int lecturerId) {
        String sql = "SELECT co.offering_id, c.course_code, c.course_name"
                + " FROM course_offering co JOIN course c ON c.course_id = co.course_id"
                + " WHERE co.lecturer_id = ?"
                + " ORDER BY c.course_code";
        List<String[]> rows = new ArrayList<>();
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                            rs.getString("offering_id"),
                            rs.getString("course_code"),
                            rs.getString("course_name")
                    });
                }
            }
            return rows;
        } catch (Exception e) {
            throw new DataAccessException("Could not load courses", e);
        }
    }

    // =====================================================================
    //  OVERVIEW PAGE
    // =====================================================================

    public int countMyCourses(int lecturerId) {
        String sql = "SELECT COUNT(*) FROM course_offering WHERE lecturer_id = ?";
        return countWithId(sql, lecturerId);
    }

    /** Number of different students enrolled in this lecturer's courses. */
    public int countMyStudents(int lecturerId) {
        String sql = "SELECT COUNT(DISTINCT e.student_id)"
                + " FROM enrollment e JOIN course_offering co ON co.offering_id = e.offering_id"
                + " WHERE co.lecturer_id = ?";
        return countWithId(sql, lecturerId);
    }

    /** Pending medicals of students who are enrolled in this lecturer's courses. */
    public int countPendingMedicals(int lecturerId) {
        String sql = "SELECT COUNT(DISTINCT m.medical_id)"
                + " FROM medical m"
                + " JOIN enrollment e ON e.student_id = m.student_id"
                + " JOIN course_offering co ON co.offering_id = e.offering_id"
                + " WHERE co.lecturer_id = ? AND m.status = 'PENDING'";
        return countWithId(sql, lecturerId);
    }

    /** Average attendance % across all sessions of this lecturer's courses. */
    public double averageAttendance(int lecturerId) {
        String sql = "SELECT AVG(CASE WHEN a.attend_status = 'PRESENT' THEN 100 ELSE 0 END)"
                + " FROM attend a"
                + " JOIN `session` s ON s.session_id = a.session_id"
                + " JOIN course_offering co ON co.offering_id = s.offering_id"
                + " WHERE co.lecturer_id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } catch (Exception e) {
            throw new DataAccessException("Could not load average attendance", e);
        }
    }

    /**
     * Today's classes from the timetable.
     * day = "Monday", "Tuesday", ... (same text that is saved in timetable_entity.day)
     * Columns: {start_time, "code - name", lecture_hall}
     */
    public List<String[]> getTodaySchedule(int lecturerId, String day) {
        String sql = "SELECT te.start_time, c.course_code, c.course_name, te.lecture_hall"
                + " FROM timetable_entity te"
                + " JOIN course_offering co ON co.offering_id = te.offering_id"
                + " JOIN course c ON c.course_id = co.course_id"
                + " WHERE co.lecturer_id = ? AND te.day = ?"
                + " ORDER BY te.start_time";
        List<String[]> rows = new ArrayList<>();
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, lecturerId);
            ps.setString(2, day);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                            rs.getString("start_time"),
                            rs.getString("course_code") + " - " + rs.getString("course_name"),
                            rs.getString("lecture_hall")
                    });
                }
            }
            return rows;
        } catch (Exception e) {
            throw new DataAccessException("Could not load today's schedule", e);
        }
    }

    // =====================================================================
    //  MY COURSES PAGE - lecture materials  (table: course_material)
    // =====================================================================

    public CourseMaterial insertMaterial(CourseMaterial m) {
        String sql = "INSERT INTO course_material"
                + " (offering_id, lecturer_id, title, material_type, file_path, file_size, external_link)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, m.getOffering_id());   // setObject also works when the value is null
            ps.setObject(2, m.getLecturer_id());
            ps.setString(3, m.getTitle());
            ps.setString(4, m.getMaterial_type());
            ps.setString(5, m.getFile_path());
            ps.setObject(6, m.getFile_size());
            ps.setString(7, m.getExternal_link());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    m.setMaterial_id(keys.getInt(1));
                }
            }
            return m;
        } catch (Exception e) {
            throw new DataAccessException("Could not insert material", e);
        }
    }

    public List<CourseMaterial> getMaterials(int offeringId) {
        String sql = "SELECT * FROM course_material WHERE offering_id = ? ORDER BY upload_date DESC";
        List<CourseMaterial> list = new ArrayList<>();
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CourseMaterial m = new CourseMaterial();
                    m.setMaterial_id(rs.getInt("material_id"));
                    m.setOffering_id(rs.getInt("offering_id"));
                    m.setLecturer_id(rs.getInt("lecturer_id"));
                    m.setTitle(rs.getString("title"));
                    m.setMaterial_type(rs.getString("material_type"));
                    m.setFile_path(rs.getString("file_path"));
                    m.setFile_size(rs.getLong("file_size"));
                    m.setExternal_link(rs.getString("external_link"));
                    m.setUpload_date(rs.getTimestamp("upload_date"));
                    list.add(m);
                }
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load materials", e);
        }
    }

    public boolean updateMaterial(CourseMaterial m) {
        String sql = "UPDATE course_material SET title = ?, material_type = ?, file_path = ?,"
                + " file_size = ?, external_link = ? WHERE material_id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getTitle());
            ps.setString(2, m.getMaterial_type());
            ps.setString(3, m.getFile_path());
            ps.setObject(4, m.getFile_size());
            ps.setString(5, m.getExternal_link());
            ps.setInt(6, m.getMaterial_id());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not update material " + m.getMaterial_id(), e);
        }
    }

    public boolean deleteMaterial(int materialId) {
        String sql = "DELETE FROM course_material WHERE material_id = ?";
        return deleteWithId(sql, materialId);
    }

    // =====================================================================
    //  MY COURSES PAGE - quizzes / assignments  (table: assessment)
    //  assessment_type is 'QUIZ', 'ASSIGNMENT', 'MID_EXAM', 'PROJECT', ...
    // =====================================================================

    public Assessment insertAssessment(Assessment a) {
        String sql = "INSERT INTO assessment"
                + " (offering_id, assessment_name, assessment_type, submitted_date, reference_number, assessment_date)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getOffering_id());
            ps.setString(2, a.getAssessment_name());
            ps.setString(3, a.getAssessment_type());
            ps.setDate(4, a.getSubmitted_date());
            ps.setString(5, a.getReference_number());
            ps.setDate(6, a.getAssessment_date());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    a.setAssessment_id(keys.getInt(1));
                }
            }
            return a;
        } catch (Exception e) {
            throw new DataAccessException("Could not insert assessment", e);
        }
    }

    public List<Assessment> getAssessments(int offeringId) {
        String sql = "SELECT * FROM assessment WHERE offering_id = ? ORDER BY assessment_date";
        List<Assessment> list = new ArrayList<>();
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Assessment a = new Assessment();
                    a.setAssessment_id(rs.getInt("assessment_id"));
                    a.setOffering_id(rs.getInt("offering_id"));
                    a.setAssessment_name(rs.getString("assessment_name"));
                    a.setAssessment_type(rs.getString("assessment_type"));
                    a.setSubmitted_date(rs.getDate("submitted_date"));
                    a.setReference_number(rs.getString("reference_number"));
                    a.setAssessment_date(rs.getDate("assessment_date"));
                    list.add(a);
                }
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load assessments", e);
        }
    }

    public boolean updateAssessment(Assessment a) {
        String sql = "UPDATE assessment SET assessment_name = ?, assessment_type = ?, submitted_date = ?,"
                + " reference_number = ?, assessment_date = ? WHERE assessment_id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getAssessment_name());
            ps.setString(2, a.getAssessment_type());
            ps.setDate(3, a.getSubmitted_date());
            ps.setString(4, a.getReference_number());
            ps.setDate(5, a.getAssessment_date());
            ps.setInt(6, a.getAssessment_id());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not update assessment " + a.getAssessment_id(), e);
        }
    }

    public boolean deleteAssessment(int assessmentId) {
        String sql = "DELETE FROM assessment WHERE assessment_id = ?";
        return deleteWithId(sql, assessmentId);
    }

    // =====================================================================
    //  ATTENDANCE PAGE  (tables: session, enrollment, attend)
    // =====================================================================

    public Session insertSession(Session s) {
        String sql = "INSERT INTO `session`"
                + " (offering_id, session_number, session_type, session_date, start_time, end_time, room)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getOffering_id());
            ps.setInt(2, s.getSession_number());
            ps.setString(3, s.getSession_type());
            ps.setDate(4, s.getSession_date());
            ps.setTime(5, s.getStart_time());
            ps.setTime(6, s.getEnd_time());
            ps.setString(7, s.getRoom());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setSession_id(keys.getInt(1));
                }
            }
            return s;
        } catch (Exception e) {
            throw new DataAccessException("Could not insert session", e);
        }
    }

    /** Sessions of one course on one date (fills the "Session" drop-down). */
    public List<Session> getSessions(int offeringId, Date date) {
        String sql = "SELECT * FROM `session` WHERE offering_id = ? AND session_date = ? ORDER BY start_time";
        List<Session> list = new ArrayList<>();
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            ps.setDate(2, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Session s = new Session();
                    s.setSession_id(rs.getInt("session_id"));
                    s.setOffering_id(rs.getInt("offering_id"));
                    s.setSession_number(rs.getInt("session_number"));
                    s.setSession_type(rs.getString("session_type"));
                    s.setSession_date(rs.getDate("session_date"));
                    s.setStart_time(rs.getTime("start_time"));
                    s.setEnd_time(rs.getTime("end_time"));
                    s.setRoom(rs.getString("room"));
                    list.add(s);
                }
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load sessions", e);
        }
    }

    public boolean deleteSession(int sessionId) {
        String sql = "DELETE FROM `session` WHERE session_id = ?";
        return deleteWithId(sql, sessionId);
    }

    /** Students enrolled in a course. Columns: {student_id, name} */
    public List<String[]> getEnrolledStudents(int offeringId) {
        String sql = "SELECT u.user_id, u.name"
                + " FROM enrollment e JOIN users u ON u.user_id = e.student_id"
                + " WHERE e.offering_id = ?"
                + " ORDER BY u.user_id";
        List<String[]> rows = new ArrayList<>();
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{rs.getString("user_id"), rs.getString("name")});
                }
            }
            return rows;
        } catch (Exception e) {
            throw new DataAccessException("Could not load enrolled students", e);
        }
    }

    public int countEnrolled(int offeringId) {
        String sql = "SELECT COUNT(*) FROM enrollment WHERE offering_id = ?";
        return countWithId(sql, offeringId);
    }

    /** Saved attendance of a session. Key = student_id, value = 'PRESENT' / 'ABSENT'. */
    public Map<Integer, String> getAttendance(int sessionId) {
        String sql = "SELECT student_id, attend_status FROM attend WHERE session_id = ?";
        Map<Integer, String> map = new HashMap<>();
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("student_id"), rs.getString("attend_status"));
                }
            }
            return map;
        } catch (Exception e) {
            throw new DataAccessException("Could not load attendance", e);
        }
    }

    /**
     * Saves one student's attendance.
     * Inserts a new row, or updates the status if the row already exists.
     * Call this once for every student in the table.
     */
    public boolean saveAttendance(int sessionId, int studentId, String status) {
        String sql = "INSERT INTO attend (student_id, session_id, attend_status, recorded_date)"
                + " VALUES (?, ?, ?, CURDATE())"
                + " ON DUPLICATE KEY UPDATE attend_status = VALUES(attend_status), recorded_date = CURDATE()";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, sessionId);
            ps.setString(3, status);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not save attendance", e);
        }
    }

    public boolean deleteAttendance(int sessionId, int studentId) {
        String sql = "DELETE FROM attend WHERE session_id = ? AND student_id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not delete attendance", e);
        }
    }

    // =====================================================================
    //  MARKS + GRADES & GPA PAGES  (tables: exam, result)
    // =====================================================================

    public Exam insertExam(Exam ex) {
        String sql = "INSERT INTO exam (offering_id, type) VALUES (?, ?)";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ex.getOffering_id());
            ps.setString(2, ex.getType());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    ex.setExam_id(keys.getInt(1));
                }
            }
            return ex;
        } catch (Exception e) {
            throw new DataAccessException("Could not insert exam", e);
        }
    }

    /** Exams of a course ('MID', 'FINAL', ...). */
    public List<Exam> getExams(int offeringId) {
        String sql = "SELECT * FROM exam WHERE offering_id = ? ORDER BY exam_id";
        List<Exam> list = new ArrayList<>();
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Exam ex = new Exam();
                    ex.setExam_id(rs.getInt("exam_id"));
                    ex.setOffering_id(rs.getInt("offering_id"));
                    ex.setType(rs.getString("type"));
                    list.add(ex);
                }
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load exams", e);
        }
    }

    public boolean deleteExam(int examId) {
        String sql = "DELETE FROM exam WHERE exam_id = ?";
        return deleteWithId(sql, examId);
    }

    /**
     * Every enrolled student of the course with their marks for one exam.
     * Students without marks yet are also returned (marks are empty).
     * Columns: {student_id, name, ca_result, final_exam_mark, total_mark, result_status}
     */
    public List<String[]> getMarks(int offeringId, int examId) {
        String sql = "SELECT u.user_id, u.name, r.ca_result, r.final_exam_mark, r.total_mark, r.result_status"
                + " FROM enrollment e"
                + " JOIN users u ON u.user_id = e.student_id"
                + " LEFT JOIN result r ON r.student_id = e.student_id AND r.exam_id = ?"
                + " WHERE e.offering_id = ?"
                + " ORDER BY u.user_id";
        List<String[]> rows = new ArrayList<>();
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setInt(2, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                            rs.getString("user_id"),
                            rs.getString("name"),
                            rs.getString("ca_result"),
                            rs.getString("final_exam_mark"),
                            rs.getString("total_mark"),
                            rs.getString("result_status")
                    });
                }
            }
            return rows;
        } catch (Exception e) {
            throw new DataAccessException("Could not load marks", e);
        }
    }

    /**
     * Saves one student's marks for one exam.
     * Inserts a new row, or updates it if the student already has a result for that exam.
     */
    public boolean saveResult(Result r) {
        String sql = "INSERT INTO result (student_id, exam_id, ca_result, final_exam_mark, total_mark, result_status)"
                + " VALUES (?, ?, ?, ?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE ca_result = VALUES(ca_result),"
                + " final_exam_mark = VALUES(final_exam_mark),"
                + " total_mark = VALUES(total_mark),"
                + " result_status = VALUES(result_status)";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, r.getStudent_id());
            ps.setInt(2, r.getExam_id());
            ps.setBigDecimal(3, r.getCa_result());   // null is saved as NULL
            ps.setBigDecimal(4, r.getFinal_exam_mark());
            ps.setBigDecimal(5, r.getTotal_mark());
            ps.setString(6, r.getResult_status());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not save result", e);
        }
    }

    /** Sets the published date of all results of an exam to today. */
    public boolean publishResults(int examId) {
        String sql = "UPDATE result SET published_date = CURDATE() WHERE exam_id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not publish results", e);
        }
    }

    public boolean deleteResult(int resultId) {
        String sql = "DELETE FROM result WHERE result_id = ?";
        return deleteWithId(sql, resultId);
    }

    // =====================================================================
    //  UNDERGRADUATES PAGE
    // =====================================================================

    /**
     * Students in one course, or in ALL of the lecturer's courses when offeringId = 0.
     * Columns: {student_id, name, batch_name, email, phone, attendance %}
     */
    public List<String[]> getMyStudents(int lecturerId, int offeringId) {
        String sql = "SELECT DISTINCT u.user_id, u.name, b.batch_name, u.email, u.phone"
                + " FROM enrollment e"
                + " JOIN course_offering co ON co.offering_id = e.offering_id"
                + " JOIN users u ON u.user_id = e.student_id"
                + " JOIN undergraduate ug ON ug.user_id = u.user_id"
                + " LEFT JOIN batch b ON b.batch_id = ug.batch_id"
                + " WHERE co.lecturer_id = ? AND (? = 0 OR co.offering_id = ?)"
                + " ORDER BY u.user_id";
        List<String[]> rows = new ArrayList<>();
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, lecturerId);
            ps.setInt(2, offeringId);
            ps.setInt(3, offeringId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int studentId = rs.getInt("user_id");
                    rows.add(new String[]{
                            String.valueOf(studentId),
                            rs.getString("name"),
                            rs.getString("batch_name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            String.format("%.0f", studentAttendance(studentId, lecturerId))
                    });
                }
            }
            return rows;
        } catch (Exception e) {
            throw new DataAccessException("Could not load students", e);
        }
    }

    /** Attendance % of one student in this lecturer's courses. */
    public double studentAttendance(int studentId, int lecturerId) {
        String sql = "SELECT AVG(CASE WHEN a.attend_status = 'PRESENT' THEN 100 ELSE 0 END)"
                + " FROM attend a"
                + " JOIN `session` s ON s.session_id = a.session_id"
                + " JOIN course_offering co ON co.offering_id = s.offering_id"
                + " WHERE a.student_id = ? AND co.lecturer_id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } catch (Exception e) {
            throw new DataAccessException("Could not load student attendance", e);
        }
    }

    /** Latest CGPA of a student (null if there is no result yet). */
    public Double getStudentGpa(int studentId) {
        String sql = "SELECT cgpa FROM result WHERE student_id = ? AND cgpa IS NOT NULL"
                + " ORDER BY published_date DESC LIMIT 1";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("cgpa") : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("Could not load GPA", e);
        }
    }

    // =====================================================================
    //  MEDICALS PAGE  (table: medical)
    // =====================================================================

    /**
     * Medicals of students who are enrolled in this lecturer's courses.
     * Columns: {medical_id, student name, student_id, medical_date, reason, status}
     */
    public List<String[]> getMedicals(int lecturerId) {
        String sql = "SELECT DISTINCT m.medical_id, u.name, u.user_id, m.medical_date, m.reason, m.status"
                + " FROM medical m"
                + " JOIN users u ON u.user_id = m.student_id"
                + " JOIN enrollment e ON e.student_id = m.student_id"
                + " JOIN course_offering co ON co.offering_id = e.offering_id"
                + " WHERE co.lecturer_id = ?"
                + " ORDER BY m.medical_date DESC";
        List<String[]> rows = new ArrayList<>();
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                            rs.getString("medical_id"),
                            rs.getString("name"),
                            rs.getString("user_id"),
                            rs.getString("medical_date"),
                            rs.getString("reason"),
                            rs.getString("status")
                    });
                }
            }
            return rows;
        } catch (Exception e) {
            throw new DataAccessException("Could not load medicals", e);
        }
    }

    /** Approve or reject a medical. status = 'APPROVED' or 'REJECTED'. */
    public boolean reviewMedical(int medicalId, String status, String note) {
        String sql = "UPDATE medical SET status = ?, reviewer_note = ?, reviewed_date = CURDATE()"
                + " WHERE medical_id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, note);
            ps.setInt(3, medicalId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not update medical " + medicalId, e);
        }
    }

    // =====================================================================
    //  NOTICES PAGE  (table: notice)
    // =====================================================================

    public Notice insertNotice(Notice n) {
        String sql = "INSERT INTO notice (created_by, title, description, audience, expiry_date, status)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, n.getCreated_by());
            ps.setString(2, n.getTitle());
            ps.setString(3, n.getDescription());
            ps.setString(4, n.getAudience());
            ps.setDate(5, n.getExpiry_date());
            ps.setString(6, n.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    n.setNotice_id(keys.getInt(1));
                }
            }
            return n;
        } catch (Exception e) {
            throw new DataAccessException("Could not insert notice", e);
        }
    }

    /** Notices that lecturers can see, plus the ones this lecturer created. */
    public List<Notice> getNotices(int lecturerId) {
        String sql = "SELECT * FROM notice"
                + " WHERE audience IN ('LECTURER', 'ALL') OR created_by = ?"
                + " ORDER BY created_date DESC";
        List<Notice> list = new ArrayList<>();
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notice n = new Notice();
                    n.setNotice_id(rs.getInt("notice_id"));
                    n.setCreated_by(rs.getInt("created_by"));
                    n.setTitle(rs.getString("title"));
                    n.setDescription(rs.getString("description"));
                    n.setCreated_date(rs.getTimestamp("created_date"));
                    n.setAudience(rs.getString("audience"));
                    n.setExpiry_date(rs.getDate("expiry_date"));
                    n.setStatus(rs.getString("status"));
                    list.add(n);
                }
            }
            return list;
        } catch (Exception e) {
            throw new DataAccessException("Could not load notices", e);
        }
    }

    public boolean updateNotice(Notice n) {
        String sql = "UPDATE notice SET title = ?, description = ?, audience = ?, expiry_date = ?, status = ?"
                + " WHERE notice_id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, n.getTitle());
            ps.setString(2, n.getDescription());
            ps.setString(3, n.getAudience());
            ps.setDate(4, n.getExpiry_date());
            ps.setString(5, n.getStatus());
            ps.setInt(6, n.getNotice_id());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Could not update notice " + n.getNotice_id(), e);
        }
    }

    public boolean deleteNotice(int noticeId) {
        String sql = "DELETE FROM notice WHERE notice_id = ?";
        return deleteWithId(sql, noticeId);
    }

    // =====================================================================
    //  HELPERS
    // =====================================================================

    /** Runs a "SELECT COUNT(*) ... WHERE x = ?" query. */
    private int countWithId(String sql, int id) {
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            throw new DataAccessException("Count query failed", e);
        }
    }

    /** Runs a "DELETE ... WHERE x = ?" query. */
    private boolean deleteWithId(String sql, int id) {
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException("Delete failed", e);
        }
    }

    private Lecturer mapLecturer(ResultSet rs) throws SQLException {
        Lecturer l = new Lecturer();
        l.setUser_id(rs.getInt("user_id"));
        l.setName(rs.getString("name"));
        l.setEmail(rs.getString("email"));
        l.setPassword(rs.getString("password"));
        l.setContact_number(rs.getString("phone"));
        l.setAddress(rs.getString("address"));
        l.setDOB(rs.getDate("dob"));
        l.setUserType(rs.getString("user_type"));
        l.setQualification(rs.getString("qualification"));
        l.setDep_id(rs.getInt("dep_id"));
        return l;
    }
}
