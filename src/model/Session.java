package model;

import java.sql.Date;
import java.sql.Time;

public class Session {

    private int session_id;
    private int offering_id;
    private int session_number;
    private String session_type;
    private Date session_date;
    private Time start_time;
    private Time end_time;
    private String room;

    public Session() {
    }

    public int getSession_id() {
        return session_id;
    }

    public void setSession_id(int session_id) {
        this.session_id = session_id;
    }

    public int getOffering_id() {
        return offering_id;
    }

    public void setOffering_id(int offering_id) {
        this.offering_id = offering_id;
    }

    public int getSession_number() {
        return session_number;
    }

    public void setSession_number(int session_number) {
        this.session_number = session_number;
    }

    public String getSession_type() {
        return session_type;
    }

    public void setSession_type(String session_type) {
        this.session_type = session_type;
    }

    public Date getSession_date() {
        return session_date;
    }

    public void setSession_date(Date session_date) {
        this.session_date = session_date;
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

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    @Override
    public String toString() {
        return "Session{" +
                "session_id=" + session_id +
                ", offering_id=" + offering_id +
                ", session_number=" + session_number +
                ", session_type='" + session_type + '\'' +
                ", session_date=" + session_date +
                ", start_time=" + start_time +
                ", end_time=" + end_time +
                ", room='" + room + '\'' +
                '}';
    }
}
