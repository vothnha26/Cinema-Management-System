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
    private final com.example.cinema.repository.room.SeatRepository seatRepository;
    private final com.example.cinema.service.commerce.PricingService pricingService;
    private final ApplicationEventPublisher eventPublisher;
    private final com.example.cinema.repository.commerce.BranchComboRepository branchComboRepository;

    public CustomerBookingFacade(BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            ComboRepository comboRepository,
            CustomerRepository customerRepository,
            ShowtimeRepository showtimeRepository,
            com.example.cinema.repository.room.SeatRepository seatRepository,
            com.example.cinema.service.commerce.PricingService pricingService,
            ApplicationEventPublisher eventPublisher,
            com.example.cinema.repository.commerce.BranchComboRepository branchComboRepository) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.comboRepository = comboRepository;
        this.customerRepository = customerRepository;
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.pricingService = pricingService;
        this.eventPublisher = eventPublisher;
        this.branchComboRepository = branchComboRepository;
    }

    @Transactional
    public BookingResponse processOnlineBooking(OnlineBookingRequest request, String username) {
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new AppException(com.example.cinema.model.constant.ErrorMessages.CHOOSE_AT_LEAST_ONE_SEAT);
        }
        if (request.getShowtimeId() == null) {
            throw new AppException(com.example.cinema.model.constant.ErrorMessages.CHOOSE_SHOWTIME);
        }

        // 1. Xác định Customer (Loại bỏ N+1 query)
        Customer customer = null;
        String emailToNotify = request.getGuestEmail();
        String nameToNotify = request.getGuestName();

        if (username != null) {
            // Logged in
            customer = customerRepository.findByUserUsername(username).orElse(null);
            if (customer != null) {
                emailToNotify = customer.getUser().getEmail();
                nameToNotify = customer.getFullName();
            }
        } else {
            // Guest validate
            if (request.getGuestName() == null || request.getGuestPhone() == null || request.getGuestEmail() == null ||
                    request.getGuestName().isBlank() || request.getGuestPhone().isBlank()
                    || request.getGuestEmail().isBlank()) {
                throw new AppException(com.example.cinema.model.constant.ErrorMessages.GUEST_INFO_REQUIRED);
            }
        }

        com.example.cinema.model.entity.Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new AppException(com.example.cinema.model.constant.ErrorMessages.SHOWTIME_NOT_FOUND));

        // 2. Tính tiền qua PricingService động (Không dùng giá vé cứng 95000L)
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Long seatId : request.getSeatIds()) {
            com.example.cinema.model.entity.Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new AppException(com.example.cinema.model.constant.ErrorMessages.SEAT_NOT_FOUND + " ID: " + seatId));
            com.example.cinema.model.dto.response.PriceCalculationResult calcResult = 
                    pricingService.calculateTicketPrice(showtime, seat, customer);
            totalPrice = totalPrice.add(calcResult.getFinalPrice());
        }

        BigDecimal comboTotal = BigDecimal.ZERO;
        if (request.getCombos() != null && !request.getCombos().isEmpty()) {
            com.example.cinema.model.entity.Branch branch = showtime.getRoom().getBranch();
            for (Map.Entry<Long, Integer> entry : request.getCombos().entrySet()) {
                Combo combo = comboRepository.findById(entry.getKey()).orElse(null);
                if (combo != null) {
                    BigDecimal price = branchComboRepository.findByBranchAndCombo(branch, combo)
                            .map(com.example.cinema.model.entity.BranchCombo::getPrice)
                            .orElse(BigDecimal.ZERO);
                    comboTotal = comboTotal.add(price.multiply(BigDecimal.valueOf(entry.getValue())));
                }
            }
        }
        totalPrice = totalPrice.add(comboTotal);

        // 3. Tạo Booking (Loại bỏ magic string bằng constants)
        String bookingCode = com.example.cinema.model.constant.AppConstants.BOOKING_CODE_PREFIX 
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking booking = new Booking();
        booking.setBookingCode(bookingCode);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING);
        booking.setShowtime(showtime);

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
        payment.setTransactionId(com.example.cinema.model.constant.AppConstants.TXN_PREFIX_ONLINE + bookingCode);
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
                showtime.getMovie().getTitle(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(com.example.cinema.model.constant.AppConstants.DATE_TIME_FORMAT)),
                request.getSeatIds().size() + " ghế",
                totalPrice.toPlainString() + "đ"));

        // 6. Response
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingCode(bookingCode);
        response.setTotalPrice(totalPrice);
        response.setStatus(BookingStatus.CONFIRMED);
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setCustomerName(nameToNotify != null ? nameToNotify : com.example.cinema.model.constant.AppConstants.DEFAULT_GUEST_NAME);
        response.setCreatedAt(booking.getCreatedAt());

        return response;
    }
}
