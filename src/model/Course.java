package model;

public class Course {

    private int course_id;
    private String course_name;
    private String course_code;
    private double credit;
    private Integer dep_id;
    private String course_type;      // "Theory" | "Practical"
    private Integer lecturer_id;
    private String batch;            // e.g. 2023/24
    private String gpa_type;         // "GPA" | "NGPA"
    private int end_weight;
    private int ca_weight;
    private int quiz_weight;
    private int assignment_weight;
    private int project_weight;
    private int mid_weight;

    public String getCourse_type() { return course_type; }
    public void setCourse_type(String v) { this.course_type = v; }
    public Integer getLecturer_id() { return lecturer_id; }
    public void setLecturer_id(Integer v) { this.lecturer_id = v; }
    public String getBatch() { return batch; }
    public void setBatch(String v) { this.batch = v; }
    public String getGpa_type() { return gpa_type; }
    public void setGpa_type(String v) { this.gpa_type = v; }
    public int getEnd_weight() { return end_weight; }
    public void setEnd_weight(int v) { this.end_weight = v; }
    public int getCa_weight() { return ca_weight; }
    public void setCa_weight(int v) { this.ca_weight = v; }
    public int getQuiz_weight() { return quiz_weight; }
    public void setQuiz_weight(int v) { this.quiz_weight = v; }
    public int getAssignment_weight() { return assignment_weight; }
    public void setAssignment_weight(int v) { this.assignment_weight = v; }
    public int getProject_weight() { return project_weight; }
    public void setProject_weight(int v) { this.project_weight = v; }
    public int getMid_weight() { return mid_weight; }
    public void setMid_weight(int v) { this.mid_weight = v; }

    public Course() {
    }

    public int getCourse_id() {
        return course_id;
    }

    public void setCourse_id(int course_id) {
        this.course_id = course_id;
    }

    public String getCourse_name() {
        return course_name;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public String getCourse_code() {
        return course_code;
    }

    public void setCourse_code(String course_code) {
        this.course_code = course_code;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public Integer getDep_id() {
        return dep_id;
    }

    public void setDep_id(Integer dep_id) {
        this.dep_id = dep_id;
    }

    @Override
    public String toString() {
        return "Course{" +
                "course_id=" + course_id +
                ", course_name='" + course_name + '\'' +
                ", course_code='" + course_code + '\'' +
                ", credit=" + credit +
                ", dep_id=" + dep_id +
                '}';
    }
}