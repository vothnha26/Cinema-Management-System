package com.example.cinema.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SeatPriceResponse {
    private Long id;
    private String roomTypeId;
    private String seatTypeId;
    private BigDecimal price;
    private LocalDate effectiveDate;
    private Boolean isActive;

    public SeatPriceResponse() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(String roomTypeId) { this.roomTypeId = roomTypeId; }
    public String getSeatTypeId() { return seatTypeId; }
    public void setSeatTypeId(String seatTypeId) { this.seatTypeId = seatTypeId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
