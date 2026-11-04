package model;

import java.sql.Date;

public class Timetable {

    private int timetable_id;
    private String academic_year;
    private Date published_date;
    private String status;

    public Timetable() {
    }

    public int getTimetable_id() {
        return timetable_id;
    }

    public void setTimetable_id(int timetable_id) {
        this.timetable_id = timetable_id;
    }

    public String getAcademic_year() {
        return academic_year;
    }

    public void setAcademic_year(String academic_year) {
        this.academic_year = academic_year;
    }

    public Date getPublished_date() {
        return published_date;
    }

    public void setPublished_date(Date published_date) {
        this.published_date = published_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Timetable{" +
                "timetable_id=" + timetable_id +
                ", academic_year='" + academic_year + '\'' +
                ", published_date=" + published_date +
                ", status='" + status + '\'' +
                '}';
    }
}
