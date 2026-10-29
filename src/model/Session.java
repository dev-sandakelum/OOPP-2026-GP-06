package model;

import java.sql.Date;
import java.sql.Time;

public class Session {
    private int sessionId;
    private int offeringId;
    private int sessionNumber;
    private String sessionType;
    private Date sessionDate;
    private Time startTime;
    private Time endTime;
    private String room;

    public Session() {}

    public Session(int sessionId, int offeringId, int sessionNumber, String sessionType,
                   Date sessionDate, Time startTime, Time endTime, String room) {
        this.sessionId = sessionId;
        this.offeringId = offeringId;
        this.sessionNumber = sessionNumber;
        this.sessionType = sessionType;
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
    }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getOfferingId() { return offeringId; }
    public void setOfferingId(int offeringId) { this.offeringId = offeringId; }

    public int getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public Date getSessionDate() { return sessionDate; }
    public void setSessionDate(Date sessionDate) { this.sessionDate = sessionDate; }

    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }

    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
}