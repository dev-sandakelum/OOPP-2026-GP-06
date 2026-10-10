package model;

public class Lecturer extends User {

    private String qualification;
    private int dep_id;

    public Lecturer() {
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public int getDep_id() {
        return dep_id;
    }

    public void setDep_id(int dep_id) {
        this.dep_id = dep_id;
    }

    @Override
    public String toString() {
        return "Lecturer{" +
                "user_id=" + getUser_id() +
                ", name='" + getName() + '\'' +
                ", qualification='" + qualification + '\'' +
                ", dep_id=" + dep_id +
                '}';
    }
}