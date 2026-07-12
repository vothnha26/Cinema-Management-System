package com.example.cinema.model.dto.request;

import java.util.List;

public class SeatBulkRequest {
    private List<SeatUpdateRequest> seats;
    private Integer rows;
    private Integer cols;

    public SeatBulkRequest() {}

    public List<SeatUpdateRequest> getSeats() { return seats; }
    public void setSeats(List<SeatUpdateRequest> seats) { this.seats = seats; }
    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows; }
    public Integer getCols() { return cols; }
    public void setCols(Integer cols) { this.cols = cols; }
}
