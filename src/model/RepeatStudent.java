package model;

public class RepeatStudent {
    private int studentId;
    private int subjectId;
    private int repeatYear;
    private int repeatShy;
    private String review;

    public RepeatStudent() {}

    public RepeatStudent(int studentId, int subjectId, int repeatYear, int repeatShy, String review) {
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.repeatYear = repeatYear;
        this.repeatShy = repeatShy;
        this.review = review;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public int getRepeatYear() { return repeatYear; }
    public void setRepeatYear(int repeatYear) { this.repeatYear = repeatYear; }

    public int getRepeatShy() { return repeatShy; }
    public void setRepeatShy(int repeatShy) { this.repeatShy = repeatShy; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }
}