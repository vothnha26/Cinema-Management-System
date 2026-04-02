package com.example.cinema.model.dto.request;

import com.example.cinema.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public class BookingRequest {
    @NotNull(message = "Showtime ID cannot be null")
    private Long showtimeId;

    private Long promotionId; // Optional promotion code id
    
    private String promotionCode; // Optional promotion string code

    @NotEmpty(message = "Must select at least one seat")
    private List<Long> seatIds;

    // Map combo_id to quantity
    private Map<Long, Integer> combos;

    private PaymentMethod paymentMethod;

    // Getters and Setters
    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }
    public Long getPromotionId() { return promotionId; }
    public void setPromotionId(Long promotionId) { this.promotionId = promotionId; }
    public String getPromotionCode() { return promotionCode; }
    public void setPromotionCode(String promotionCode) { this.promotionCode = promotionCode; }
    public List<Long> getSeatIds() { return seatIds; }
    public void setSeatIds(List<Long> seatIds) { this.seatIds = seatIds; }
    public Map<Long, Integer> getCombos() { return combos; }
    public void setCombos(Map<Long, Integer> combos) { this.combos = combos; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
}
