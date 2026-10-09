package model;

import java.sql.Date;

public class Medical {

    private int medical_id;
    private int student_id;
    private Date medical_date;
    private String reason;
    private String certificate_path;
    private String status;
    private String reviewer_note;
    private Date reviewed_date;

    public Medical() {
    }

    public int getMedical_id() {
        return medical_id;
    }

    public void setMedical_id(int medical_id) {
        this.medical_id = medical_id;
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public Date getMedical_date() {
        return medical_date;
    }

    public void setMedical_date(Date medical_date) {
        this.medical_date = medical_date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCertificate_path() {
        return certificate_path;
    }

    public void setCertificate_path(String certificate_path) {
        this.certificate_path = certificate_path;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReviewer_note() {
        return reviewer_note;
    }

    public void setReviewer_note(String reviewer_note) {
        this.reviewer_note = reviewer_note;
    }

    public Date getReviewed_date() {
        return reviewed_date;
    }

    public void setReviewed_date(Date reviewed_date) {
        this.reviewed_date = reviewed_date;
    }

    @Override
    public String toString() {
        return "Medical{" +
                "medical_id=" + medical_id +
                ", student_id=" + student_id +
                ", medical_date=" + medical_date +
                ", reason='" + reason + '\'' +
                ", certificate_path='" + certificate_path + '\'' +
                ", status='" + status + '\'' +
                ", reviewer_note='" + reviewer_note + '\'' +
                ", reviewed_date=" + reviewed_date +
                '}';
    }
}