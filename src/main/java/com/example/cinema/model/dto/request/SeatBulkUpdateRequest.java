package com.example.cinema.model.dto.request;

import java.util.List;

public class SeatBulkUpdateRequest {
    private List<SeatUpdateRequest> seats;

    public SeatBulkUpdateRequest() {}

    public List<SeatUpdateRequest> getSeats() { return seats; }
    public void setSeats(List<SeatUpdateRequest> seats) { this.seats = seats; }
}
