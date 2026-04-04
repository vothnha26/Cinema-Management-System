package com.example.cinema.model.dto.response;

import com.example.cinema.model.enums.RoomType;
import com.example.cinema.model.enums.SeatType;

import java.util.List;

public class RoomResponse {
    private Long id;
    private String name;
    private RoomType type;
    private Integer capacity;
    private Integer rows;
    private Integer cols;
    private Boolean status;
    private List<SeatResponse> seats;

    public static class SeatResponse {
        private Long id;
        private String rowChar;
        private Integer colNum;
        private SeatType type;
        private Boolean status;

        // Getters/Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

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

    public RoomResponse() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Integer getCols() {
        return cols;
    }

    public void setCols(Integer cols) {
        this.cols = cols;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public List<SeatResponse> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatResponse> seats) {
        this.seats = seats;
    }
}
