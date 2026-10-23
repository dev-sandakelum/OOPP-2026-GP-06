package model;

import java.sql.Date;
import java.util.Optional;

public class User {
    private int user_id ;
    private String name ;
    private String email ;
    private String contact_number ;
    private String password ;
    private Date DOB;
    private String address ;
    private String gender ;
    private String userType;


    // No-arg constructor (needed by many frameworks)
    public User() {
    }

    // Full constructor
    public User(int userId, String name, String email, String contactNumber,
                String passwordHash, Date dob, String address, String gender) {
        this.user_id = userId;
        this.name = name;
        this.email = email;
        this.contact_number = contactNumber;
        this.password = passwordHash;
        this.DOB = dob;
        this.address = address;
        this.gender = gender;

    }

    // Copy constructor (replaces the Optional<User> one)
    public User(User other) {
        this(other.user_id, other.name, other.email, other.contact_number,
                other.password, other.DOB, other.address, other.gender);
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact_number() {
        return contact_number;
    }

    public void setContact_number(String contact_number) {
        this.contact_number = contact_number;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getDOB() {
        return DOB;
    }

    public void setDOB(Date DOB) {
        this.DOB = DOB;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id=" + user_id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", contact_number='" + contact_number + '\'' +
                ", password='" + password + '\'' +
                ", DOB=" + DOB +
                ", address='" + address + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}
