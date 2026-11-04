package model;

import java.sql.Time;

public class TimetableEntity {

    private int timetable_entity_id;
    private int timetable_id;
    private Integer offering_id;
    private String day;
    private Time start_time;
    private Time end_time;
    private String lecture_hall;
    private String session_type;

    public TimetableEntity() {
    }

    public int getTimetable_entity_id() {
        return timetable_entity_id;
    }

    public void setTimetable_entity_id(int timetable_entity_id) {
        this.timetable_entity_id = timetable_entity_id;
    }

    public int getTimetable_id() {
        return timetable_id;
    }

    public void setTimetable_id(int timetable_id) {
        this.timetable_id = timetable_id;
    }

    public Integer getOffering_id() {
        return offering_id;
    }

    public void setOffering_id(Integer offering_id) {
        this.offering_id = offering_id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Time getStart_time() {
        return start_time;
    }

    public void setStart_time(Time start_time) {
        this.start_time = start_time;
    }

    public Time getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Time end_time) {
        this.end_time = end_time;
    }

    public String getLecture_hall() {
        return lecture_hall;
    }

    public void setLecture_hall(String lecture_hall) {
        this.lecture_hall = lecture_hall;
    }

    public String getSession_type() {
        return session_type;
    }

    public void setSession_type(String session_type) {
        this.session_type = session_type;
    }

    @Override
    public String toString() {
        return "TimetableEntity{" +
                "timetable_entity_id=" + timetable_entity_id +
                ", timetable_id=" + timetable_id +
                ", offering_id=" + offering_id +
                ", day='" + day + '\'' +
                ", start_time=" + start_time +
                ", end_time=" + end_time +
                ", lecture_hall='" + lecture_hall + '\'' +
                ", session_type='" + session_type + '\'' +
                '}';
    }
}