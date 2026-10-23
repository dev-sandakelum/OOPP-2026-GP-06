package model;

public class Lecturer extends User {

    private String qualification;
    private int dep_id;

    public Lecturer() {
    }

    public Lecturer(User user){
        this.setUser_id(user.getUser_id());
        this.setName(user.getName());
        this.setEmail(user.getEmail());
        this.setContact_number(user.getContact_number());
        this.setPassword(user.getPassword());
        this.setDOB(user.getDOB());
        this.setAddress(user.getAddress());
        this.setGender(user.getGender());
        this.setUserType(user.getUserType());
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