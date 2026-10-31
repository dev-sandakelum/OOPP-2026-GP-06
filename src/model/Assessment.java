package model;

import java.sql.Date;

public class Assessment {
    private int assessmentId;
    private int offeringId;
    private String assessmentName;
    private String assessmentType; // QUIZ, ASSIGNMENT, MID_EXAM, PROJECT, PRACTICAL
    private Date submittedDate;
    private String referenceNumber;
    private Date assessmentDate;

    public Assessment() {}

    public Assessment(int assessmentId, int offeringId, String assessmentName,
                      String assessmentType, Date submittedDate, String referenceNumber, Date assessmentDate) {
        this.assessmentId = assessmentId;
        this.offeringId = offeringId;
        this.assessmentName = assessmentName;
        this.assessmentType = assessmentType;
        this.submittedDate = submittedDate;
        this.referenceNumber = referenceNumber;
        this.assessmentDate = assessmentDate;
    }

    public int getAssessmentId() { return assessmentId; }
    public void setAssessmentId(int assessmentId) { this.assessmentId = assessmentId; }

    public int getOfferingId() { return offeringId; }
    public void setOfferingId(int offeringId) { this.offeringId = offeringId; }

    public String getAssessmentName() { return assessmentName; }
    public void setAssessmentName(String assessmentName) { this.assessmentName = assessmentName; }

    public String getAssessmentType() { return assessmentType; }
    public void setAssessmentType(String assessmentType) { this.assessmentType = assessmentType; }

    public Date getSubmittedDate() { return submittedDate; }
    public void setSubmittedDate(Date submittedDate) { this.submittedDate = submittedDate; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public Date getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(Date assessmentDate) { this.assessmentDate = assessmentDate; }
}
