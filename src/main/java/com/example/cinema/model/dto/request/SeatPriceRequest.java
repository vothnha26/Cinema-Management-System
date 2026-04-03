package com.example.cinema.model.dto.request;

import com.example.cinema.model.enums.RoomType;
import com.example.cinema.model.enums.SeatType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class SeatPriceRequest {
    @NotNull(message = "Room Type không được để trống")
    private RoomType roomType;

    @NotNull(message = "Seat Type không được để trống")
    private SeatType seatType;

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @NotNull(message = "Ngày hiệu lực không được để trống")
    private LocalDate effectiveDate;

    public SeatPriceRequest() {
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
