package model;

public class Admin extends User {

    public Admin() {
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