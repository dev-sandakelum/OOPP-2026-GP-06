package model;

import java.sql.Date;

public class Medical {
    private int medicalId;
    private int studentId;
    private Date medicalDate;
    private String reason;
    private String certificatePath;
    private String status; // PENDING, APPROVED, REJECTED
    private String reviewerNote;
    private Date reviewedDate;

    public Medical() {}

    public Medical(int medicalId, int studentId, Date medicalDate, String reason,
                   String certificatePath, String status, String reviewerNote, Date reviewedDate) {
        this.medicalId = medicalId;
        this.studentId = studentId;
        this.medicalDate = medicalDate;
        this.reason = reason;
        this.certificatePath = certificatePath;
        this.status = status;
        this.reviewerNote = reviewerNote;
        this.reviewedDate = reviewedDate;
    }

    public int getMedicalId() {
        return medicalId;
    }

    public void setMedicalId(int medicalId) {
        this.medicalId = medicalId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public Date getMedicalDate() {
        return medicalDate;
    }

    public void setMedicalDate(Date medicalDate) {
        this.medicalDate = medicalDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCertificatePath() {
        return certificatePath;
    }

    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReviewerNote() {
        return reviewerNote;
    }

    public void setReviewerNote(String reviewerNote) {
        this.reviewerNote = reviewerNote;
    }

    public Date getReviewedDate() {
        return reviewedDate;
    }

    public void setReviewedDate(Date reviewedDate) {
        this.reviewedDate = reviewedDate;
    }
}