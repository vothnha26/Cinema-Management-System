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
    private final com.example.cinema.repository.room.SeatRepository seatRepository;
    private final com.example.cinema.service.commerce.PricingService pricingService;
    private final ApplicationEventPublisher eventPublisher;
    private final com.example.cinema.repository.commerce.BranchComboRepository branchComboRepository;

    public StaffPosFacade(BookingRepository bookingRepository,
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
    public BookingResponse processDirectBooking(PosBookingRequest request) {
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new AppException(com.example.cinema.model.constant.ErrorMessages.CHOOSE_AT_LEAST_ONE_SEAT);
        }
        if (request.getShowtimeId() == null) {
            throw new AppException(com.example.cinema.model.constant.ErrorMessages.CHOOSE_SHOWTIME);
        }

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new AppException(com.example.cinema.model.constant.ErrorMessages.SHOWTIME_NOT_FOUND));

        Customer customer = null;
        if (request.getCustomerPhone() != null && !request.getCustomerPhone().isBlank()) {
            customer = customerRepository.findByPhone(request.getCustomerPhone()).orElse(null);
        }

        if (customer == null) {
            throw new AppException(com.example.cinema.model.constant.ErrorMessages.CUSTOMER_NOT_FOUND);
        }

        String bookingCode = com.example.cinema.model.constant.AppConstants.BOOKING_CODE_PREFIX_SC 
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Tính tiền qua PricingService động (Không dùng giá vé cứng 95000L)
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
        payment.setTransactionId(com.example.cinema.model.constant.AppConstants.POS_CODE_PREFIX + bookingCode);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        if (customer.getUser() != null) {
            eventPublisher.publishEvent(new TicketBookedEvent(
                    this,
                    customer.getUser(),
                    bookingCode,
                    showtime.getMovie().getTitle(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(com.example.cinema.model.constant.AppConstants.DATE_TIME_FORMAT)),
                    request.getSeatIds().size() + " ghế",
                    totalPrice.toPlainString() + "đ"));
        }

        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingCode(bookingCode);
        response.setTotalPrice(totalPrice);
        response.setStatus(BookingStatus.CONFIRMED);
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setCustomerName(customer.getFullName());
        response.setCreatedAt(booking.getCreatedAt());

        return response;
    }
}
