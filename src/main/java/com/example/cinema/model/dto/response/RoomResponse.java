package com.example.cinema.model.dto.response;

import java.util.List;

public class RoomResponse {
    private Long id;
    private String name;
    private String roomTypeId;
    private String roomTypeName;
    private List<String> supportedFormats;
    private Integer capacity;
    private Integer rows;
    private Integer cols;
    private com.example.cinema.model.enums.RoomStatus status;
    private List<SeatResponse> seats;
    private Boolean hasShowtime;
    private Long branchId;

    public static class SeatResponse {
        private Long id;
        private String rowChar;
        private Integer colNum;
        private String seatTypeId;
        private String seatTypeName;
        private Boolean status;

        public SeatResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getRowChar() { return rowChar; }
        public void setRowChar(String rowChar) { this.rowChar = rowChar; }
        public Integer getColNum() { return colNum; }
        public void setColNum(Integer colNum) { this.colNum = colNum; }
        public String getSeatTypeId() { return seatTypeId; }
        public void setSeatTypeId(String seatTypeId) { this.seatTypeId = seatTypeId; }
        public String getSeatTypeName() { return seatTypeName; }
        public void setSeatTypeName(String seatTypeName) { this.seatTypeName = seatTypeName; }
        public Boolean getStatus() { return status; }
        public void setStatus(Boolean status) { this.status = status; }
    }

    public RoomResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(String roomTypeId) { this.roomTypeId = roomTypeId; }
    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }
    public List<String> getSupportedFormats() { return supportedFormats; }
    public void setSupportedFormats(List<String> supportedFormats) { this.supportedFormats = supportedFormats; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows; }
    public Integer getCols() { return cols; }
    public void setCols(Integer cols) { this.cols = cols; }
    public com.example.cinema.model.enums.RoomStatus getStatus() { return status; }
    public void setStatus(com.example.cinema.model.enums.RoomStatus status) { this.status = status; }
    public List<SeatResponse> getSeats() { return seats; }
    public void setSeats(List<SeatResponse> seats) { this.seats = seats; }
    public Boolean getHasShowtime() { return hasShowtime; }
    public void setHasShowtime(Boolean hasShowtime) { this.hasShowtime = hasShowtime; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
}
