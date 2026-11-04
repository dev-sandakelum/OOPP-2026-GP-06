package model;

public class Batch {

    private int batch_id;
    private String batch_name;
    private int intake_year;
    private Integer dep_id;

    public Batch() {
    }

    public int getBatch_id() {
        return batch_id;
    }

    public void setBatch_id(int batch_id) {
        this.batch_id = batch_id;
    }

    public String getBatch_name() {
        return batch_name;
    }

    public void setBatch_name(String batch_name) {
        this.batch_name = batch_name;
    }

    public int getIntake_year() {
        return intake_year;
    }

    public void setIntake_year(int intake_year) {
        this.intake_year = intake_year;
    }

    public Integer getDep_id() {
        return dep_id;
    }

    public void setDep_id(Integer dep_id) {
        this.dep_id = dep_id;
    }

    @Override
    public String toString() {
        return "Batch{" +
                "batch_id=" + batch_id +
                ", batch_name='" + batch_name + '\'' +
                ", intake_year=" + intake_year +
                ", dep_id=" + dep_id +
                '}';
    }
}
