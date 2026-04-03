package com.example.cinema.model.dto.response;

import com.example.cinema.model.enums.RoomType;
import com.example.cinema.model.enums.SeatType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SeatPriceResponse {
    private Long id;
    private RoomType roomType;
    private SeatType seatType;
    private BigDecimal price;
    private LocalDate effectiveDate;
    private Boolean isActive;

    public SeatPriceResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
