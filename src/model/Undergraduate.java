package model;

public class Undergraduate extends User {

    private int dep_id;
    private int batch_id;
    private String profile_picture;

    public Undergraduate() {
    }

    public int getDep_id() {
        return dep_id;
    }

    public void setDep_id(int dep_id) {
        this.dep_id = dep_id;
    }

    public int getBatch_id() {
        return batch_id;
    }

    public void setBatch_id(int batch_id) {
        this.batch_id = batch_id;
    }

    public String getProfile_picture() {
        return profile_picture;
    }

    public void setProfile_picture(String profile_picture) {
        this.profile_picture = profile_picture;
    }

    @Override
    public String toString() {
        return "Undergraduate{" +
                "user_id=" + getUser_id() +
                ", name='" + getName() + '\'' +
                ", dep_id=" + dep_id +
                ", batch_id=" + batch_id +
                ", profile_picture='" + profile_picture + '\'' +
                '}';
    }
}