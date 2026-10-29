package model;

public class Exam {

    private int exam_id;
    private int offering_id;
    private String type;

    public Exam() {
    }

    public int getExam_id() {
        return exam_id;
    }

    public void setExam_id(int exam_id) {
        this.exam_id = exam_id;
    }

    public int getOffering_id() {
        return offering_id;
    }

    public void setOffering_id(int offering_id) {
        this.offering_id = offering_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Exam{" +
                "exam_id=" + exam_id +
                ", offering_id=" + offering_id +
                ", type='" + type + '\'' +
                '}';
    }
}
