package model;

import java.sql.Date;

public class Enrollment {

    // Composite PK: student_id + offering_id
    private int student_id;
    private int offering_id;
    private Date enrollment_date;
    private String enrollment_status;

    public Enrollment() {
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public int getOffering_id() {
        return offering_id;
    }

    public void setOffering_id(int offering_id) {
        this.offering_id = offering_id;
    }

    public Date getEnrollment_date() {
        return enrollment_date;
    }

    public void setEnrollment_date(Date enrollment_date) {
        this.enrollment_date = enrollment_date;
    }

    public String getEnrollment_status() {
        return enrollment_status;
    }

    public void setEnrollment_status(String enrollment_status) {
        this.enrollment_status = enrollment_status;
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "student_id=" + student_id +
                ", offering_id=" + offering_id +
                ", enrollment_date=" + enrollment_date +
                ", enrollment_status='" + enrollment_status + '\'' +
                '}';
    }
}
