package com.example.cinema.service.notification;

import com.example.cinema.model.entity.User;
import org.springframework.context.ApplicationEvent;

/**
 * Observer Pattern (Spring Event):
 * Event được publish khi một vé đặt thành công.
 * BookingService chỉ cần publish event này mà KHÔNG cần biết
 * ai sẽ xử lý (gửi mail, ghi log, tích điểm...) => Decoupling hoàn toàn.
 */
public class TicketBookedEvent extends ApplicationEvent {

    private final User customer;
    private final String bookingCode;
    private final String movieTitle;
    private final String showtime;
    private final String seatInfo;
    private final String totalPrice;

    public TicketBookedEvent(Object source, User customer, String bookingCode,
                             String movieTitle, String showtime, String seatInfo, String totalPrice) {
        super(source);
        this.customer = customer;
        this.bookingCode = bookingCode;
        this.movieTitle = movieTitle;
        this.showtime = showtime;
        this.seatInfo = seatInfo;
        this.totalPrice = totalPrice;
    }

    public User getCustomer() { return customer; }
    public String getBookingCode() { return bookingCode; }
    public String getMovieTitle() { return movieTitle; }
    public String getShowtime() { return showtime; }
    public String getSeatInfo() { return seatInfo; }
    public String getTotalPrice() { return totalPrice; }
}
