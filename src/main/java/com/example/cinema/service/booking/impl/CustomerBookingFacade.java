package com.example.cinema.service.booking.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.OnlineBookingRequest;
import com.example.cinema.model.dto.response.BookingResponse;
import com.example.cinema.model.entity.Booking;
import com.example.cinema.model.entity.Combo;
import com.example.cinema.model.entity.Customer;
import com.example.cinema.model.entity.Payment;
import com.example.cinema.model.entity.User;
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
import java.util.Map;
import java.util.UUID;

/**
 * Facade Pattern: Xử lý booking online bao gồm thành viên và khách vãng lai.
 */
@Service
public class CustomerBookingFacade {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ComboRepository comboRepository;
    private final CustomerRepository customerRepository;
    private final ShowtimeRepository showtimeRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CustomerBookingFacade(BookingRepository bookingRepository,
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
    public BookingResponse processOnlineBooking(OnlineBookingRequest request, String username) {
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new AppException("Vui lòng chọn ít nhất 1 ghế.");
        }

        // 1. Xác định Customer
        Customer customer = null;
        String emailToNotify = request.getGuestEmail();
        String nameToNotify = request.getGuestName();

        if (username != null) {
            // Logged in
            customer = customerRepository.findAll().stream()
                    .filter(c -> c.getUser().getUsername().equals(username))
                    .findFirst().orElse(null);
            if (customer != null) {
                emailToNotify = customer.getUser().getEmail();
                nameToNotify = customer.getFullName();
            }
        } else {
            // Guest validate
            if (request.getGuestName() == null || request.getGuestPhone() == null || request.getGuestEmail() == null ||
                    request.getGuestName().isBlank() || request.getGuestPhone().isBlank()
                    || request.getGuestEmail().isBlank()) {
                throw new AppException("Vui lòng cung cấp đầy đủ thông tin khách vãng lai (Tên, Email, SĐT).");
            }
        }

        // 2. Tính tiền (Mockup Seat Price)
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

        // Giảm giá cho member
        if (customer != null) {
            // Tạm fix giảm 10%
            BigDecimal discount = totalPrice.multiply(BigDecimal.valueOf(0.1));
            totalPrice = totalPrice.subtract(discount);
        }

        // 3. Tạo Booking
        String bookingCode = "BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking booking = new Booking();
        booking.setBookingCode(bookingCode);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING);
        
        // Gán showtime từ request
        if (request.getShowtimeId() != null) {
            booking.setShowtime(showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new AppException("Suất chiếu không tồn tại")));
        }

        if (customer != null) {
            booking.setCustomer(customer);
        }
        bookingRepository.save(booking);

        // 4. Tạo Payment
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(totalPrice);
        payment.setPaymentMethod(PaymentMethod.valueOf(
                request.getPaymentMethod() != null ? request.getPaymentMethod() : "MOMO"));
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionId("ONL-" + bookingCode);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 5. Gửi Notification (Dù guest cũng gửi mail qua Observer Pattern)
        User targetUser = customer != null ? customer.getUser() : new User();
        if (customer == null) {
            targetUser.setEmail(emailToNotify);
            targetUser.setUsername(nameToNotify);
        }

        eventPublisher.publishEvent(new TicketBookedEvent(
                this,
                targetUser,
                bookingCode,
                "Phim Online", // Lấy từ showtime thật sau
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")),
                request.getSeatIds().size() + " ghế",
                totalPrice.toPlainString() + "đ"));

        // 6. Response
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingCode(bookingCode);
        response.setTotalPrice(totalPrice);
        response.setStatus(BookingStatus.CONFIRMED);
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setCustomerName(nameToNotify);
        response.setCreatedAt(booking.getCreatedAt());

        return response;
    }
}
