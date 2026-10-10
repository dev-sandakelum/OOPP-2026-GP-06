package model;

public class TechnicalOfficer extends User {

    private int dep_id;

    public TechnicalOfficer() {
    }

    public int getDep_id() {
        return dep_id;
    }

    public void setDep_id(int dep_id) {
        this.dep_id = dep_id;
    }

    @Override
    public String toString() {
        return "TechnicalOfficer{" +
                "user_id=" + getUser_id() +
                ", name='" + getName() + '\'' +
                ", dep_id=" + dep_id +
                '}';
    }
}