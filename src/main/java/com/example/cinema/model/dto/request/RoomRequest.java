package com.example.cinema.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RoomRequest {
    @NotBlank(message = "Tên phòng không được để trống")
    private String name;

    @NotBlank(message = "Room Type không được để trống")
    private String roomTypeId;

    private Integer rows;

    private Integer cols;

    private String templateFileName;

    public RoomRequest() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(String roomTypeId) { this.roomTypeId = roomTypeId; }
    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows; }
    public Integer getCols() { return cols; }
    public void setCols(Integer cols) { this.cols = cols; }
    public String getTemplateFileName() { return templateFileName; }
    public void setTemplateFileName(String templateFileName) { this.templateFileName = templateFileName; }
}
