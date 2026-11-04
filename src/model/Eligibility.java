package model;

import java.math.BigDecimal;

public class Eligibility {
    private int eligibilityId;
    private int studentId;
    private int offeringId;
    private BigDecimal caAverage;
    private BigDecimal attendancePercentage;
    private String eligibilityStatus; // ELIGIBLE, NOT_ELIGIBLE, PENDING

    public Eligibility() {}

    public Eligibility(int eligibilityId, int studentId, int offeringId,
                       BigDecimal caAverage, BigDecimal attendancePercentage, String eligibilityStatus) {
        this.eligibilityId = eligibilityId;
        this.studentId = studentId;
        this.offeringId = offeringId;
        this.caAverage = caAverage;
        this.attendancePercentage = attendancePercentage;
        this.eligibilityStatus = eligibilityStatus;
    }

    public int getEligibilityId() { return eligibilityId; }
    public void setEligibilityId(int eligibilityId) { this.eligibilityId = eligibilityId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getOfferingId() { return offeringId; }
    public void setOfferingId(int offeringId) { this.offeringId = offeringId; }

    public BigDecimal getCaAverage() { return caAverage; }
    public void setCaAverage(BigDecimal caAverage) { this.caAverage = caAverage; }

    public BigDecimal getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(BigDecimal attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public String getEligibilityStatus() { return eligibilityStatus; }
    public void setEligibilityStatus(String eligibilityStatus) { this.eligibilityStatus = eligibilityStatus; }
}
