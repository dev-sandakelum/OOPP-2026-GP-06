package model;

import java.time.LocalTime;

/** One weekly slot of the timetable, already joined with its course (view model). */
public class TimetableSlot {

    private final int id;              // timetable_entity_id
    private final int offeringId;
    private final String day;          // MONDAY .. SUNDAY
    private final LocalTime start;
    private final LocalTime end;
    private final String hall;
    private final String type;         // LECTURE / PRACTICAL
    private final String courseCode;
    private final String courseName;

    public TimetableSlot(int id, int offeringId, String day, LocalTime start, LocalTime end,
                         String hall, String type, String courseCode, String courseName) {
        this.id = id;
        this.offeringId = offeringId;
        this.day = day == null ? "" : day.trim().toUpperCase();
        this.start = start;
        this.end = end;
        this.hall = hall == null ? "" : hall;
        this.type = type == null ? "" : type;
        this.courseCode = courseCode == null ? "" : courseCode;
        this.courseName = courseName == null ? "" : courseName;
    }

    public int getId()            { return id; }
    public int getOfferingId()    { return offeringId; }
    public String getDay()        { return day; }
    public LocalTime getStart()   { return start; }
    public LocalTime getEnd()     { return end; }
    public String getHall()       { return hall; }
    public String getType()       { return type; }
    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
}
