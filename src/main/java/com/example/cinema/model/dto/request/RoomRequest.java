package com.example.cinema.model.dto.request;

import com.example.cinema.model.enums.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RoomRequest {
    @NotBlank(message = "Tên phòng không được để trống")
    private String name;

    @NotNull(message = "Loại phòng không được để trống")
    private RoomType type;

    @NotNull(message = "Số hàng không được để trống")
    @Min(value = 1, message = "Số hàng tối thiểu là 1")
    private Integer rows;

    @NotNull(message = "Số cột không được để trống")
    @Min(value = 1, message = "Số cột tối thiểu là 1")
    private Integer cols;

    public RoomRequest() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = type; }

    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows; }

    public Integer getCols() { return cols; }
    public void setCols(Integer cols) { this.cols = cols; }
}
