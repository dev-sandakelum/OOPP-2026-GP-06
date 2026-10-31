package model;

public class Department {

    private int dep_id;
    private String dep_name;
    private String contact_no;

    public Department() {
    }

    public int getDep_id() {
        return dep_id;
    }

    public void setDep_id(int dep_id) {
        this.dep_id = dep_id;
    }

    public String getDep_name() {
        return dep_name;
    }

    public void setDep_name(String dep_name) {
        this.dep_name = dep_name;
    }

    public String getContact_no() {
        return contact_no;
    }

    public void setContact_no(String contact_no) {
        this.contact_no = contact_no;
    }

    @Override
    public String toString() {
        return "Department{" +
                "dep_id=" + dep_id +
                ", dep_name='" + dep_name + '\'' +
                ", contact_no='" + contact_no + '\'' +
                '}';
    }
}
