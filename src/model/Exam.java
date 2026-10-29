package model;

public class Exam {
    private int examId;
    private int offeringId;
    private String type; // MID, FINAL

    public Exam() {}

    public Exam(int examId, int offeringId, String type) {
        this.examId = examId;
        this.offeringId = offeringId;
        this.type = type;
    }

    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }

    public int getOfferingId() { return offeringId; }
    public void setOfferingId(int offeringId) { this.offeringId = offeringId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}