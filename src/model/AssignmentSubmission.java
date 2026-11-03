package model;

import java.sql.Timestamp;

public class AssignmentSubmission {

    private int submissionId;
    private int assessmentId;
    private int studentId;
    private String filePath;
    private Timestamp submittedAt;
    private String status;   // SUBMITTED, LATE, GRADED
    private String remarks;

    public AssignmentSubmission() {}

    public AssignmentSubmission(int submissionId, int assessmentId, int studentId,
                                String filePath, Timestamp submittedAt,
                                String status, String remarks) {
        this.submissionId = submissionId;
        this.assessmentId = assessmentId;
        this.studentId    = studentId;
        this.filePath     = filePath;
        this.submittedAt  = submittedAt;
        this.status       = status;
        this.remarks      = remarks;
    }

    public int getSubmissionId()               { return submissionId; }
    public void setSubmissionId(int v)         { this.submissionId = v; }

    public int getAssessmentId()               { return assessmentId; }
    public void setAssessmentId(int v)         { this.assessmentId = v; }

    public int getStudentId()                  { return studentId; }
    public void setStudentId(int v)            { this.studentId = v; }

    public String getFilePath()                { return filePath; }
    public void setFilePath(String v)          { this.filePath = v; }

    public Timestamp getSubmittedAt()          { return submittedAt; }
    public void setSubmittedAt(Timestamp v)    { this.submittedAt = v; }

    public String getStatus()                  { return status; }
    public void setStatus(String v)            { this.status = v; }

    public String getRemarks()                 { return remarks; }
    public void setRemarks(String v)           { this.remarks = v; }
}
