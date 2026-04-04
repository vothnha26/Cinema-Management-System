package com.example.cinema.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_details")
public class BookingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = true) // Cho phép null để xóa ghế thoải mái
    private Seat seat;

    @Column(name = "seat_code") // Lưu tên ghế (vd: A1) để audit sau này
    private String seatCode;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public BookingDetail() {}

    public BookingDetail(Long id, Booking booking, Seat seat, String seatCode, BigDecimal price) {
        this.id = id;
        this.booking = booking;
        this.seat = seat;
        this.seatCode = seatCode;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
