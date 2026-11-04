package model;

import java.math.BigDecimal;
import java.sql.Date;

public class Result {
    private int studentId;
    private int examId;
    private BigDecimal caResult;
    private BigDecimal finalExamMark;
    private BigDecimal totalMark;
    private BigDecimal sgpa;
    private String resultStatus;
    private Date publishedDate;
    private BigDecimal cgpa;

    public Result() {}

    public Result(int studentId, int examId, BigDecimal caResult, BigDecimal finalExamMark,
                  BigDecimal totalMark, BigDecimal sgpa, String resultStatus, Date publishedDate, BigDecimal cgpa) {
        this.studentId = studentId;
        this.examId = examId;
        this.caResult = caResult;
        this.finalExamMark = finalExamMark;
        this.totalMark = totalMark;
        this.sgpa = sgpa;
        this.resultStatus = resultStatus;
        this.publishedDate = publishedDate;
        this.cgpa = cgpa;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }

    public BigDecimal getCaResult() { return caResult; }
    public void setCaResult(BigDecimal caResult) { this.caResult = caResult; }

    public BigDecimal getFinalExamMark() { return finalExamMark; }
    public void setFinalExamMark(BigDecimal finalExamMark) { this.finalExamMark = finalExamMark; }

    public BigDecimal getTotalMark() { return totalMark; }
    public void setTotalMark(BigDecimal totalMark) { this.totalMark = totalMark; }

    public BigDecimal getSgpa() { return sgpa; }
    public void setSgpa(BigDecimal sgpa) { this.sgpa = sgpa; }

    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }

    public Date getPublishedDate() { return publishedDate; }
    public void setPublishedDate(Date publishedDate) { this.publishedDate = publishedDate; }

    public BigDecimal getCgpa() { return cgpa; }
    public void setCgpa(BigDecimal cgpa) { this.cgpa = cgpa; }
}
