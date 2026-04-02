package com.example.cinema.model.dto.request;

import java.util.List;
import java.util.Map;

/**
 * DTO cho Staff tạo booking tại quầy POS.
 */
public class PosBookingRequest {

    private Long showtimeId;
    private List<Long> seatIds;
    private Map<Long, Integer> combos; // comboId -> quantity
    private String paymentMethod;      // CASH, CARD, MOMO
    private String customerPhone;      // Tùy chọn, null = khách vãng lai

    public PosBookingRequest() {}

    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }

    public List<Long> getSeatIds() { return seatIds; }
    public void setSeatIds(List<Long> seatIds) { this.seatIds = seatIds; }

    public Map<Long, Integer> getCombos() { return combos; }
    public void setCombos(Map<Long, Integer> combos) { this.combos = combos; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
}
