package model;

public class Admin extends User {

    public Admin() {
    }

    public Admin(User user){
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

    @Override
    public String toString() {
        return "Admin{" +
                "user_id=" + getUser_id() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }
}