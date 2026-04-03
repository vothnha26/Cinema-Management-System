package com.example.cinema.service.pos.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.PosBookingRequest;
import com.example.cinema.model.dto.response.BookingResponse;
import com.example.cinema.model.entity.Booking;
import com.example.cinema.model.entity.Combo;
import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.Payment;
import com.example.cinema.model.entity.Showtime;
import com.example.cinema.model.enums.BookingStatus;
import com.example.cinema.model.enums.PaymentMethod;
import com.example.cinema.model.enums.PaymentStatus;
import com.example.cinema.repository.booking.BookingRepository;
import com.example.cinema.repository.booking.PaymentRepository;
import com.example.cinema.repository.commerce.ComboRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.service.notification.TicketBookedEvent;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StaffPosFacade {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ComboRepository comboRepository;
    private final CustomerRepository customerRepository;
    private final ShowtimeRepository showtimeRepository;
    private final ApplicationEventPublisher eventPublisher;

    public StaffPosFacade(BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            ComboRepository comboRepository,
            CustomerRepository customerRepository,
            ShowtimeRepository showtimeRepository,
            ApplicationEventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.comboRepository = comboRepository;
        this.customerRepository = customerRepository;
        this.showtimeRepository = showtimeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BookingResponse processDirectBooking(PosBookingRequest request) {
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new AppException("Vui lòng chọn ít nhất 1 ghế.");
        }
        if (request.getShowtimeId() == null) {
            throw new AppException("Vui lòng chọn suất chiếu.");
        }

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new AppException("Suất chiếu không tồn tại."));

        Customer customer = null;
        if (request.getCustomerPhone() != null && !request.getCustomerPhone().isBlank()) {
            customer = customerRepository.findAll().stream()
                    .filter(c -> request.getCustomerPhone().equals(c.getPhone()))
                    .findFirst()
                    .orElse(null);
        }

        if (customer == null) {
            throw new AppException(
                    "Không tìm thấy thông tin khách hàng. Bán vé tại quầy yêu cầu thông tin khách hàng (nullable=false).");
        }

        String bookingCode = "SC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BigDecimal totalPrice = BigDecimal.valueOf(request.getSeatIds().size() * 95000L);

        BigDecimal comboTotal = BigDecimal.ZERO;
        if (request.getCombos() != null && !request.getCombos().isEmpty()) {
            for (Map.Entry<Long, Integer> entry : request.getCombos().entrySet()) {
                Combo combo = comboRepository.findById(entry.getKey()).orElse(null);
                if (combo != null) {
                    comboTotal = comboTotal.add(combo.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
                }
            }
        }
        totalPrice = totalPrice.add(comboTotal);

        Booking booking = new Booking();
        booking.setBookingCode(bookingCode);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setShowtime(showtime);
        booking.setCustomer(customer);
        bookingRepository.save(booking);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(totalPrice);
        payment.setPaymentMethod(PaymentMethod.valueOf(
                request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH"));
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId("POS-" + bookingCode);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        if (customer != null && customer.getUser() != null) {
            eventPublisher.publishEvent(new TicketBookedEvent(
                    this,
                    customer.getUser(),
                    bookingCode,
                    "Phim tại quầy",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")),
                    request.getSeatIds().size() + " ghế",
                    totalPrice.toPlainString() + "đ"));
        }

        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingCode(bookingCode);
        response.setTotalPrice(totalPrice);
        response.setStatus(BookingStatus.CONFIRMED);
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setCustomerName(customer != null ? customer.getFullName() : "Khách vãng lai");
        response.setCreatedAt(booking.getCreatedAt());

        return response;
    }
}
