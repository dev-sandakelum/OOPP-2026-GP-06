package model;

import java.sql.Date;

public class Attend {

    // Composite PK: student_id + session_id
    private int student_id;
    private int session_id;
    private String attend_status;
    private Date recorded_date;

    public Attend() {
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public int getSession_id() {
        return session_id;
    }

    public void setSession_id(int session_id) {
        this.session_id = session_id;
    }

    public String getAttend_status() {
        return attend_status;
    }

    public void setAttend_status(String attend_status) {
        this.attend_status = attend_status;
    }

    public Date getRecorded_date() {
        return recorded_date;
    }

    public void setRecorded_date(Date recorded_date) {
        this.recorded_date = recorded_date;
    }

    @Override
    public String toString() {
        return "Attend{" +
                "student_id=" + student_id +
                ", session_id=" + session_id +
                ", attend_status='" + attend_status + '\'' +
                ", recorded_date=" + recorded_date +
                '}';
    }
}
