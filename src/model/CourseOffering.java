package model;

public class CourseOffering {

    private int offering_id;
    private int course_id;
    private int semester_id;
    private Integer lecturer_id;    // nullable – SET NULL on lecturer delete

    public CourseOffering() {
    }

    public int getOffering_id() {
        return offering_id;
    }

    public void setOffering_id(int offering_id) {
        this.offering_id = offering_id;
    }

    public int getCourse_id() {
        return course_id;
    }

    public void setCourse_id(int course_id) {
        this.course_id = course_id;
    }

    public int getSemester_id() {
        return semester_id;
    }

    public void setSemester_id(int semester_id) {
        this.semester_id = semester_id;
    }

    public Integer getLecturer_id() {
        return lecturer_id;
    }

    public void setLecturer_id(Integer lecturer_id) {
        this.lecturer_id = lecturer_id;
    }

    @Override
    public String toString() {
        return "CourseOffering{" +
                "offering_id=" + offering_id +
                ", course_id=" + course_id +
                ", semester_id=" + semester_id +
                ", lecturer_id=" + lecturer_id +
                '}';
    }
}
