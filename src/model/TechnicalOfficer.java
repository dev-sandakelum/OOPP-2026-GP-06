package model;

public class TechnicalOfficer extends User {

    private int dep_id;

    public TechnicalOfficer() {
    }
    public TechnicalOfficer(User user) {
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