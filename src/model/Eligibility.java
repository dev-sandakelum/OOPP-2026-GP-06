package model;

import java.math.BigDecimal;

public class Eligibility {

    private int eligibility_id;
    private int student_id;
    private int offering_id;
    private BigDecimal ca_average;             // DECIMAL(5,2) – nullable
    private BigDecimal attendance_percentage;  // DECIMAL(5,2) – 0–100, nullable
    private String eligibility_status;

    public Eligibility() {
    }

    public int getEligibility_id() {
        return eligibility_id;
    }

    public void setEligibility_id(int eligibility_id) {
        this.eligibility_id = eligibility_id;
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

    public BigDecimal getCa_average() {
        return ca_average;
    }

    public void setCa_average(BigDecimal ca_average) {
        this.ca_average = ca_average;
    }

    public BigDecimal getAttendance_percentage() {
        return attendance_percentage;
    }

    public void setAttendance_percentage(BigDecimal attendance_percentage) {
        this.attendance_percentage = attendance_percentage;
    }

    public String getEligibility_status() {
        return eligibility_status;
    }

    public void setEligibility_status(String eligibility_status) {
        this.eligibility_status = eligibility_status;
    }

    @Override
    public String toString() {
        return "Eligibility{" +
                "eligibility_id=" + eligibility_id +
                ", student_id=" + student_id +
                ", offering_id=" + offering_id +
                ", ca_average=" + ca_average +
                ", attendance_percentage=" + attendance_percentage +
                ", eligibility_status='" + eligibility_status + '\'' +
                '}';
    }
}
