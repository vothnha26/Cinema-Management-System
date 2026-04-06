package com.example.cinema.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class SeatPriceRequest {
    @NotBlank(message = "Room Type không được để trống")
    private String roomTypeId;

    @NotBlank(message = "Seat Type không được để trống")
    private String seatTypeId;

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @NotNull(message = "Ngày hiệu lực không được để trống")
    private LocalDate effectiveDate;

    public SeatPriceRequest() {
    }

    public String getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(String roomTypeId) { this.roomTypeId = roomTypeId; }
    public String getSeatTypeId() { return seatTypeId; }
    public void setSeatTypeId(String seatTypeId) { this.seatTypeId = seatTypeId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
}
