package model;

import java.math.BigDecimal;
import java.sql.Date;

public class Result {

    // Composite unique: student_id + exam_id
    private int result_id;
    private int student_id;
    private int exam_id;
    private BigDecimal ca_result;        // DECIMAL(5,2) – nullable
    private BigDecimal final_exam_mark;  // DECIMAL(5,2) – nullable
    private BigDecimal total_mark;       // DECIMAL(5,2) – nullable
    private BigDecimal sgpa;             // DECIMAL(4,2) – nullable
    private String result_status;
    private Date published_date;
    private BigDecimal cgpa;             // DECIMAL(4,2) – nullable

    public Result() {
    }

    public int getResult_id() {
        return result_id;
    }

    public void setResult_id(int result_id) {
        this.result_id = result_id;
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public int getExam_id() {
        return exam_id;
    }

    public void setExam_id(int exam_id) {
        this.exam_id = exam_id;
    }

    public BigDecimal getCa_result() {
        return ca_result;
    }

    public void setCa_result(BigDecimal ca_result) {
        this.ca_result = ca_result;
    }

    public BigDecimal getFinal_exam_mark() {
        return final_exam_mark;
    }

    public void setFinal_exam_mark(BigDecimal final_exam_mark) {
        this.final_exam_mark = final_exam_mark;
    }

    public BigDecimal getTotal_mark() {
        return total_mark;
    }

    public void setTotal_mark(BigDecimal total_mark) {
        this.total_mark = total_mark;
    }

    public BigDecimal getSgpa() {
        return sgpa;
    }

    public void setSgpa(BigDecimal sgpa) {
        this.sgpa = sgpa;
    }

    public String getResult_status() {
        return result_status;
    }

    public void setResult_status(String result_status) {
        this.result_status = result_status;
    }

    public Date getPublished_date() {
        return published_date;
    }

    public void setPublished_date(Date published_date) {
        this.published_date = published_date;
    }

    public BigDecimal getCgpa() {
        return cgpa;
    }

    public void setCgpa(BigDecimal cgpa) {
        this.cgpa = cgpa;
    }

    @Override
    public String toString() {
        return "Result{" +
                "result_id=" + result_id +
                ", student_id=" + student_id +
                ", exam_id=" + exam_id +
                ", ca_result=" + ca_result +
                ", final_exam_mark=" + final_exam_mark +
                ", total_mark=" + total_mark +
                ", sgpa=" + sgpa +
                ", result_status='" + result_status + '\'' +
                ", published_date=" + published_date +
                ", cgpa=" + cgpa +
                '}';
    }
}
