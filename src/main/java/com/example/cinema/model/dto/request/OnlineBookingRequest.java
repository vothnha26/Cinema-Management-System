package com.example.cinema.model.dto.request;


import java.util.List;
import java.util.Map;

/**
 * DTO cho Đặt vé Online (Hỗ trợ cả Thành viên và Khách vãng lai)
 */
public class OnlineBookingRequest {

    private Long showtimeId;
    private List<Long> seatIds;
    private Map<Long, Integer> combos; // comboId -> quantity
    private String paymentMethod;      // VNPAY, MOMO, CARD

    // Thông tin khách vãng lai (nếu không đăng nhập)
    private String guestName;
    private String guestPhone;
    private String guestEmail;

    public OnlineBookingRequest() {}

    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }

    public List<Long> getSeatIds() { return seatIds; }
    public void setSeatIds(List<Long> seatIds) { this.seatIds = seatIds; }

    public Map<Long, Integer> getCombos() { return combos; }
    public void setCombos(Map<Long, Integer> combos) { this.combos = combos; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
}
