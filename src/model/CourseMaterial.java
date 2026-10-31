package model;

import java.sql.Timestamp;

public class CourseMaterial {

    private int material_id;
    private Integer offering_id;    // nullable – SET NULL on offering delete
    private Integer lecturer_id;    // nullable – SET NULL on lecturer delete
    private String title;
    private String material_type;
    private String file_path;
    private Long file_size;
    private String external_link;
    private Timestamp upload_date;

    public CourseMaterial() {
    }

    public int getMaterial_id() {
        return material_id;
    }

    public void setMaterial_id(int material_id) {
        this.material_id = material_id;
    }

    public Integer getOffering_id() {
        return offering_id;
    }

    public void setOffering_id(Integer offering_id) {
        this.offering_id = offering_id;
    }

    public Integer getLecturer_id() {
        return lecturer_id;
    }

    public void setLecturer_id(Integer lecturer_id) {
        this.lecturer_id = lecturer_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMaterial_type() {
        return material_type;
    }

    public void setMaterial_type(String material_type) {
        this.material_type = material_type;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public Long getFile_size() {
        return file_size;
    }

    public void setFile_size(Long file_size) {
        this.file_size = file_size;
    }

    public String getExternal_link() {
        return external_link;
    }

    public void setExternal_link(String external_link) {
        this.external_link = external_link;
    }

    public Timestamp getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(Timestamp upload_date) {
        this.upload_date = upload_date;
    }

    @Override
    public String toString() {
        return "CourseMaterial{" +
                "material_id=" + material_id +
                ", offering_id=" + offering_id +
                ", lecturer_id=" + lecturer_id +
                ", title='" + title + '\'' +
                ", material_type='" + material_type + '\'' +
                ", file_path='" + file_path + '\'' +
                ", file_size=" + file_size +
                ", external_link='" + external_link + '\'' +
                ", upload_date=" + upload_date +
                '}';
    }
}
