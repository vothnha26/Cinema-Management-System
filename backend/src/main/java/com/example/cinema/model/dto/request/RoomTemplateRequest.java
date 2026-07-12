package com.example.cinema.model.dto.request;

import java.util.List;

public class RoomTemplateRequest {
    private String templateName;
    private String roomTypeId;
    private Integer rows;
    private Integer cols;
    private List<SeatTemplateDTO> seats;

    public static class SeatTemplateDTO {
        private String rowChar;
        private Integer colNum;
        private String seatTypeId;
        private Boolean status;

        // Getters and Setters
        public String getRowChar() { return rowChar; }
        public void setRowChar(String rowChar) { this.rowChar = rowChar; }
        public Integer getColNum() { return colNum; }
        public void setColNum(Integer colNum) { this.colNum = colNum; }
        public String getSeatTypeId() { return seatTypeId; }
        public void setSeatTypeId(String seatTypeId) { this.seatTypeId = seatTypeId; }
        public Boolean getStatus() { return status; }
        public void setStatus(Boolean status) { this.status = status; }
    }

    // Getters and Setters
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(String roomTypeId) { this.roomTypeId = roomTypeId; }
    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows; }
    public Integer getCols() { return cols; }
    public void setCols(Integer cols) { this.cols = cols; }
    public List<SeatTemplateDTO> getSeats() { return seats; }
    public void setSeats(List<SeatTemplateDTO> seats) { this.seats = seats; }
}
