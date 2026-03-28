package com.example.cinema.model.dto.request;

import java.util.List;

public class SeatBulkRequest {
    private List<SeatUpdateRequest> seats;

    public SeatBulkRequest() {}

    public List<SeatUpdateRequest> getSeats() { return seats; }
    public void setSeats(List<SeatUpdateRequest> seats) { this.seats = seats; }
}
