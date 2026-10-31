package model;

import java.sql.Date;

public class Attendance {
    private int studentId;
    private int sessionId;
    private String attendStatus; // PRESENT, ABSENT, LATE, MEDICAL
    private Date recordedDate;

    public Attendance() {}

    public Attendance(int studentId, int sessionId, String attendStatus, Date recordedDate) {
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.attendStatus = attendStatus;
        this.recordedDate = recordedDate;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public String getAttendStatus() { return attendStatus; }
    public void setAttendStatus(String attendStatus) { this.attendStatus = attendStatus; }

    public Date getRecordedDate() { return recordedDate; }
    public void setRecordedDate(Date recordedDate) { this.recordedDate = recordedDate; }
}