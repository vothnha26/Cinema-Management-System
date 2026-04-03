package com.example.cinema.model.dto.request;

import com.example.cinema.model.enums.SeatType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SeatUpdateRequest {
    @NotBlank
    private String rowChar;

    @NotNull
    private Integer colNum;

    @NotNull
    private SeatType type; // Khôi phục lại kiểu Enum

    @NotNull
    private Boolean status;

    public SeatUpdateRequest() {
    }

    public SeatUpdateRequest(String rowChar, Integer colNum, SeatType type, Boolean status) {
        this.rowChar = rowChar;
        this.colNum = colNum;
        this.type = type;
        this.status = status;
    }

    // Getters and Setters
    public String getRowChar() {
        return rowChar;
    }

    public void setRowChar(String rowChar) {
        this.rowChar = rowChar;
    }

    public Integer getColNum() {
        return colNum;
    }

    public void setColNum(Integer colNum) {
        this.colNum = colNum;
    }

    public SeatType getType() {
        return type;
    }

    public void setType(SeatType type) {
        this.type = type;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
