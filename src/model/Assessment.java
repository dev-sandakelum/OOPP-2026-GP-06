package model;

import java.sql.Date;

public class Assessment {

    private int assessment_id;
    private int offering_id;
    private String assessment_name;
    private String assessment_type;
    private Date submitted_date;
    private String reference_number;
    private Date assessment_date;

    public Assessment() {
    }

    public int getAssessment_id() {
        return assessment_id;
    }

    public void setAssessment_id(int assessment_id) {
        this.assessment_id = assessment_id;
    }

    public int getOffering_id() {
        return offering_id;
    }

    public void setOffering_id(int offering_id) {
        this.offering_id = offering_id;
    }

    public String getAssessment_name() {
        return assessment_name;
    }

    public void setAssessment_name(String assessment_name) {
        this.assessment_name = assessment_name;
    }

    public String getAssessment_type() {
        return assessment_type;
    }

    public void setAssessment_type(String assessment_type) {
        this.assessment_type = assessment_type;
    }

    public Date getSubmitted_date() {
        return submitted_date;
    }

    public void setSubmitted_date(Date submitted_date) {
        this.submitted_date = submitted_date;
    }

    public String getReference_number() {
        return reference_number;
    }

    public void setReference_number(String reference_number) {
        this.reference_number = reference_number;
    }

    public Date getAssessment_date() {
        return assessment_date;
    }

    public void setAssessment_date(Date assessment_date) {
        this.assessment_date = assessment_date;
    }

    @Override
    public String toString() {
        return "Assessment{" +
                "assessment_id=" + assessment_id +
                ", offering_id=" + offering_id +
                ", assessment_name='" + assessment_name + '\'' +
                ", assessment_type='" + assessment_type + '\'' +
                ", submitted_date=" + submitted_date +
                ", reference_number='" + reference_number + '\'' +
                ", assessment_date=" + assessment_date +
                '}';
    }
}
