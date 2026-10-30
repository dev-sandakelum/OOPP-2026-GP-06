package model;

public class RepeatStudent {

    // Composite PK: student_id + subject_id + repeat_year
    private int student_id;
    private int subject_id;   // references course(course_id)
    private int repeat_year;
    private String repeat_shy;
    private String review;

    public RepeatStudent() {
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public int getSubject_id() {
        return subject_id;
    }

    public void setSubject_id(int subject_id) {
        this.subject_id = subject_id;
    }

    public int getRepeat_year() {
        return repeat_year;
    }

    public void setRepeat_year(int repeat_year) {
        this.repeat_year = repeat_year;
    }

    public String getRepeat_shy() {
        return repeat_shy;
    }

    public void setRepeat_shy(String repeat_shy) {
        this.repeat_shy = repeat_shy;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    @Override
    public String toString() {
        return "RepeatStudent{" +
                "student_id=" + student_id +
                ", subject_id=" + subject_id +
                ", repeat_year=" + repeat_year +
                ", repeat_shy='" + repeat_shy + '\'' +
                ", review='" + review + '\'' +
                '}';
    }
}
