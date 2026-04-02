package com.example.cinema.service.pos.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.PosBookingRequest;
import com.example.cinema.model.dto.response.BookingResponse;
import com.example.cinema.model.entity.*;
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

/**
 * Facade Pattern (SRP + OCP):
 * Gom nhóm tất cả các bước phức tạp của quy trình bán vé tại quầy
 * thành MỘT method duy nhất. Controller chỉ cần gọi:
 *   posFacade.processDirectBooking(request)
 *
 * Bên trong Facade tự điều phối:
 *   1. Validate showtime/seats
 *   2. Tạo Booking + BookingDetail
 *   3. Xử lý Combo (nếu có)
 *   4. Tạo Payment (CASH/CARD/MOMO thành công ngay tại quầy)
 *   5. Publish event gửi notification (Observer Pattern)
 */
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

    /**
     * Xử lý toàn bộ quy trình bán vé trực tiếp tại quầy.
     * Facade gom: validate → booking → payment → notification.
     */
    @Transactional
    public BookingResponse processDirectBooking(PosBookingRequest request) {
        // 1. Validate input
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new AppException("Vui lòng chọn ít nhất 1 ghế.");
        }
        if (request.getShowtimeId() == null) {
            throw new AppException("Vui lòng chọn suất chiếu.");
        }

        // Tìm Showtime (BẮT BUỘC vì nullable=false)
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new AppException("Suất chiếu không tồn tại."));

        // 2. Tìm customer nếu có SĐT (tích điểm)
        Customer customer = null;
        if (request.getCustomerPhone() != null && !request.getCustomerPhone().isBlank()) {
            customer = customerRepository.findAll().stream()
                    .filter(c -> request.getCustomerPhone().equals(c.getPhone()))
                    .findFirst()
                    .orElse(null);
        }

        // Nếu DB bắt buộc customer (nullable=false), ta phải có customer
        if (customer == null) {
            // Trong thực tế, có thể tự động tạo 1 Guest Customer hoặc yêu cầu staff nhập.
            // Ở đây throw exception để debug nếu test không cung cấp customer đúng.
            throw new AppException("Không tìm thấy thông tin khách hàng. Bán vé tại quầy yêu cầu thông tin khách hàng (nullable=false).");
        }

        // 3. Tạo booking code
        String bookingCode = "SC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 4. Tính tổng tiền (placeholder - khi tích hợp sẽ query SeatPrice thật)
        BigDecimal totalPrice = BigDecimal.valueOf(request.getSeatIds().size() * 95000L);

        // 5. Tính thêm combo
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

        // 6. Tạo Booking entity
        Booking booking = new Booking();
        booking.setBookingCode(bookingCode);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.CONFIRMED); // POS = xác nhận ngay
        booking.setShowtime(showtime);
        booking.setCustomer(customer);
        bookingRepository.save(booking);

        // 7. Tạo Payment - POS thanh toán ngay
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(totalPrice);
        payment.setPaymentMethod(PaymentMethod.valueOf(
                request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH"));
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId("POS-" + bookingCode);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 8. Publish event để gửi notification (Observer Pattern)
        if (customer != null && customer.getUser() != null) {
            eventPublisher.publishEvent(new TicketBookedEvent(
                    this,
                    customer.getUser(),
                    bookingCode,
                    "Phim tại quầy", // Sẽ lấy từ showtime.movie.title khi tích hợp đầy đủ
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")),
                    request.getSeatIds().size() + " ghế",
                    totalPrice.toPlainString() + "đ"
            ));
        }

        // 9. Build response
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingCode(bookingCode);
        response.setTotalPrice(totalPrice);
        response.setStatus(BookingStatus.CONFIRMED.name());
        response.setPaymentMethod(payment.getPaymentMethod().name());
        response.setCustomerName(customer != null ? customer.getFullName() : "Khách vãng lai");
        response.setCreatedAt(booking.getCreatedAt());

        return response;
    }
}
