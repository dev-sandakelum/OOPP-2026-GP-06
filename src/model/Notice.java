package model;

import java.sql.Date;
import java.sql.Timestamp;

public class Notice {

    private int notice_id;
    private Integer created_by;
    private String title;
    private String description;
    private Timestamp created_date;
    private String audience;
    private Date expiry_date;
    private String status;

    public Notice() {
    }

    public int getNotice_id() {
        return notice_id;
    }

    public void setNotice_id(int notice_id) {
        this.notice_id = notice_id;
    }

    public Integer getCreated_by() {
        return created_by;
    }

    public void setCreated_by(Integer created_by) {
        this.created_by = created_by;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreated_date() {
        return created_date;
    }

    public void setCreated_date(Timestamp created_date) {
        this.created_date = created_date;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public Date getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(Date expiry_date) {
        this.expiry_date = expiry_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Notice{" +
                "notice_id=" + notice_id +
                ", created_by=" + created_by +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", created_date=" + created_date +
                ", audience='" + audience + '\'' +
                ", expiry_date=" + expiry_date +
                ", status='" + status + '\'' +
                '}';
    }
}