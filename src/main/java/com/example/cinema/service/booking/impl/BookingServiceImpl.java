package com.example.cinema.service.booking.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.BookingRequest;
import com.example.cinema.model.dto.response.BookingResponse;
import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.*;
import com.example.cinema.repository.booking.BookingDetailRepository;
import com.example.cinema.repository.booking.BookingRepository;
import com.example.cinema.repository.commerce.ComboRepository;
import com.example.cinema.repository.commerce.PromotionRepository;
import com.example.cinema.repository.notification.NotificationRepository;
import com.example.cinema.repository.room.SeatPriceRepository;
import com.example.cinema.repository.room.SeatRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.service.booking.BookingService;
import com.example.cinema.service.notification.INotificationAutomationService;
import com.example.cinema.service.user.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final CustomerRepository customerRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final ComboRepository comboRepository;
    private final PromotionRepository promotionRepository;
    private final SeatPriceRepository seatPriceRepository;
    private final NotificationRepository notificationRepository;
    private final CustomerService customerService;
    private final INotificationAutomationService notificationAutomationService;

    public BookingServiceImpl(BookingRepository bookingRepository, ShowtimeRepository showtimeRepository,
                              SeatRepository seatRepository, CustomerRepository customerRepository,
                              BookingDetailRepository bookingDetailRepository, ComboRepository comboRepository,
                              PromotionRepository promotionRepository, SeatPriceRepository seatPriceRepository,
                              NotificationRepository notificationRepository,
                              CustomerService customerService,
                              INotificationAutomationService notificationAutomationService) {
        this.bookingRepository = bookingRepository;
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.customerRepository = customerRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.comboRepository = comboRepository;
        this.promotionRepository = promotionRepository;
        this.seatPriceRepository = seatPriceRepository;
        this.notificationRepository = notificationRepository;
        this.customerService = customerService;
        this.notificationAutomationService = notificationAutomationService;
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Starting booking process for request: {}", request);

        Customer customer = getCurrentCustomer();
        log.debug("Booking for customer: {}", customer.getUser().getUsername());

        Showtime showtime = getAndValidateShowtime(request.getShowtimeId());
        validateSeatsAvailability(showtime.getId(), request.getSeatIds());

        Booking booking = new Booking();
        List<BookingDetail> details = processSeatPricing(booking, showtime, request.getSeatIds());
        List<BookingCombo> combos = processComboPricing(booking, request.getCombos());

        BigDecimal totalPrice = calculateInitialTotal(details, combos);

        totalPrice = applyPromotion(booking, customer, request.getPromotionCode(), totalPrice);
        totalPrice = applyMembershipDiscount(customer, totalPrice);

        if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            totalPrice = BigDecimal.ZERO;
        }

        finalizeBooking(booking, customer, showtime, details, combos, totalPrice, request.getPaymentMethod());
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking saved successfully with code: {}", savedBooking.getBookingCode());

        if (savedBooking.getPayment() != null && savedBooking.getPayment().getPaymentStatus() == PaymentStatus.SUCCESS) {
            customerService.addLoyaltyPoints(customer, totalPrice);
            createNotification(customer.getUser(),
                    "Dat ve thanh cong",
                    "Booking " + savedBooking.getBookingCode() + " da duoc xac nhan va thanh toan thanh cong.",
                    NotificationType.BOOKING);
        } else {
            createNotification(customer.getUser(),
                    "Dat ve thanh cong, cho thanh toan",
                    "Booking " + savedBooking.getBookingCode() + " da duoc tao. Vui long hoan tat thanh toan de xac nhan ve.",
                    NotificationType.BOOKING);
        }

        return mapToResponse(savedBooking);
    }

    private Customer getCurrentCustomer() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new AppException("Unauthorized");
        }
        String username = ((UserDetails) principal).getUsername();
        return customerRepository.findByUserUsername(username)
                .orElseThrow(() -> new AppException("Customer not found"));
    }

    private Showtime getAndValidateShowtime(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new AppException("Showtime not found"));

        if (showtime.getStatus() != ShowtimeStatus.UPCOMING) {
            throw new AppException("This showtime is not open for booking");
        }
        return showtime;
    }

    private void validateSeatsAvailability(Long showtimeId, List<Long> seatIds) {
        List<Long> bookedSeatIds = bookingDetailRepository.findBookedSeatIdsByShowtime(showtimeId);
        for (Long seatId : seatIds) {
            if (bookedSeatIds.contains(seatId)) {
                throw new AppException("Seat " + seatId + " is already booked");
            }
        }
    }

    private List<BookingDetail> processSeatPricing(Booking booking, Showtime showtime, List<Long> seatIds) {
        List<BookingDetail> details = new ArrayList<>();
        for (Long seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new AppException("Seat not found"));
            
            BookingDetail detail = new BookingDetail();
            detail.setBooking(booking);
            detail.setSeat(seat);
            detail.setSeatCode(seat.getSeatCode()); // Lưu vết tên ghế vĩnh viễn
            
            SeatPrice sp = seatPriceRepository.findLatestPrice(
                    showtime.getRoom().getType(), 
                    seat.getType(), 
                    showtime.getStartTime().toLocalDate())
                .orElseThrow(() -> new AppException("Price not found for seat " + seat.getSeatCode()));
                
            detail.setPrice(sp.getPrice());
            details.add(detail);
        }
        return details;
    }

    private List<BookingCombo> processComboPricing(Booking booking, Map<String, Integer> comboRequests) {
        List<BookingCombo> bookingCombos = new ArrayList<>();
        if (comboRequests != null) {
            for (Map.Entry<String, Integer> entry : comboRequests.entrySet()) {
                Long comboId = Long.parseLong(entry.getKey());
                Combo combo = comboRepository.findById(comboId)
                        .orElseThrow(() -> new AppException("Combo not found"));
                
                BookingCombo bc = new BookingCombo();
                bc.setBooking(booking);
                bc.setCombo(combo);
                bc.setQuantity(entry.getValue());
                bc.setPrice(combo.getPrice());
                bookingCombos.add(bc);
            }
        }
        return bookingCombos;
    }

    private BigDecimal calculateInitialTotal(List<BookingDetail> details, List<BookingCombo> combos) {
        BigDecimal total = details.stream()
                .map(BookingDetail::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal comboTotal = combos.stream()
                .map(bc -> bc.getPrice().multiply(BigDecimal.valueOf(bc.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        return total.add(comboTotal);
    }

    private BigDecimal applyPromotion(Booking booking, Customer customer, String promoCode, BigDecimal currentTotal) {
        if (promoCode == null || promoCode.isEmpty()) return currentTotal;

        Promotion promotion = promotionRepository.findByCodeAndIsActiveTrue(promoCode)
                .orElseThrow(() -> new AppException("Invalid promotion code"));
        
        if (customer.getMembershipTier().ordinal() < promotion.getMinTier().getTier().ordinal()) {
            throw new AppException("Membership tier too low for this promotion. Required: " + promotion.getMinTier().getTier());
        }

        if (promotion.getEndDate() != null && promotion.getEndDate().isBefore(java.time.LocalDate.now())) {
            throw new AppException("Promotion expired");
        }

        booking.setPromotion(promotion);
        BigDecimal discount;
        if (promotion.getDiscountType() == DiscountType.FIXED) {
            discount = promotion.getDiscountValue();
        } else {
            discount = currentTotal.multiply(promotion.getDiscountValue()).divide(BigDecimal.valueOf(100));
        }
        log.debug("Applying promotion {}: -{}", promoCode, discount);
        return currentTotal.subtract(discount);
    }

    private BigDecimal applyMembershipDiscount(Customer customer, BigDecimal currentTotal) {
        BigDecimal discountPercent = customerService.getDiscountPercentage(customer.getMembershipTier());
        if (discountPercent.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal mDiscount = currentTotal.multiply(discountPercent).divide(BigDecimal.valueOf(100));
            log.debug("Applying membership discount for tier {}: -{}", customer.getMembershipTier(), mDiscount);
            return currentTotal.subtract(mDiscount);
        }
        return currentTotal;
    }

    private void finalizeBooking(Booking booking, Customer customer, Showtime showtime,
                                 List<BookingDetail> details, List<BookingCombo> combos,
                                 BigDecimal totalPrice, PaymentMethod paymentMethod) {
        PaymentMethod resolvedPaymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.MOMO;
        boolean autoConfirm = resolvedPaymentMethod == PaymentMethod.CASH;

        booking.setCustomer(customer);
        booking.setShowtime(showtime);
        booking.setBookingCode("BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(autoConfirm ? BookingStatus.CONFIRMED : BookingStatus.PENDING);
        booking.setDetails(details);
        booking.setCombos(combos);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(totalPrice);
        payment.setPaymentMethod(resolvedPaymentMethod);
        payment.setPaymentStatus(autoConfirm ? PaymentStatus.SUCCESS : PaymentStatus.PENDING);
        if (autoConfirm) {
            payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
            payment.setPaidAt(LocalDateTime.now());
        }
        booking.setPayment(payment);
    }

    @Override
    public List<BookingResponse> getMyBookings() {
        Customer customer = getCurrentCustomer();
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId())
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse getMyBookingByCode(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new AppException("Booking not found"));

        if (!hasAuthority("ROLE_STAFF")) {
            Customer customer = getCurrentCustomer();
            if (!booking.getCustomer().getId().equals(customer.getId())) {
                throw new AppException("Unauthorized to view this booking");
            }
        }

        return mapToResponse(booking);
    }

    private boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException("Unauthorized to view this booking");
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    @Override
    @Transactional
    public void cancelBooking(String bookingCode) {
        Customer customer = getCurrentCustomer();
        
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new AppException("Booking not found"));
                
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new AppException("Unauthorized to cancel this booking");
        }

        if (booking.getShowtime().getStartTime().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new AppException("Cannot cancel within 1 hour of showtime");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        if (booking.getPayment() != null) {
            if (booking.getPayment().getPaymentStatus() == PaymentStatus.SUCCESS) {
                booking.getPayment().setPaymentStatus(PaymentStatus.REFUNDED);
            } else {
                booking.getPayment().setPaymentStatus(PaymentStatus.FAILED);
                booking.getPayment().setTransactionId(null);
                booking.getPayment().setPaidAt(null);
            }
        }
        bookingRepository.save(booking);
        createNotification(customer.getUser(),
                "Huy ve thanh cong",
                "Booking " + bookingCode + " da duoc huy. Trang thai thanh toan da duoc cap nhat.",
                NotificationType.SYSTEM);
        log.info("Booking {} cancelled by customer {}", bookingCode, customer.getUser().getUsername());
    }

    @Override
    @Transactional
    public void checkInBooking(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new AppException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException("Cancelled booking cannot be checked in");
        }

        if (booking.getStatus() == BookingStatus.CHECKED_IN) {
            throw new AppException("Booking already checked in");
        }

        if (booking.getPayment() == null || booking.getPayment().getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new AppException("Booking has not been paid successfully");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);

        notificationAutomationService.notifyUser(
                booking.getCustomer().getUser(),
                "Check-in thanh cong",
                "Booking " + booking.getBookingCode() + " da check-in thanh cong tai rap.",
                NotificationType.SYSTEM);
    }

    private BookingResponse mapToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingCode(booking.getBookingCode());
        response.setMovieTitle(booking.getShowtime().getMovie().getTitle());
        response.setRoomName(booking.getShowtime().getRoom().getName());
        response.setShowTime(booking.getShowtime().getStartTime());
        response.setTotalPrice(booking.getTotalPrice());
        response.setStatus(booking.getStatus());
        response.setCreatedAt(booking.getCreatedAt());
        response.setPromotionCode(booking.getPromotion() != null ? booking.getPromotion().getCode() : null);
        
        if (booking.getDetails() != null) {
            response.setSeatCodes(booking.getDetails().stream()
                    .map(d -> d.getSeat().getSeatCode())
                    .collect(Collectors.toList()));
        }
        if (booking.getCombos() != null) {
            response.setComboSummary(booking.getCombos().stream()
                    .map(combo -> combo.getCombo().getName() + " x" + combo.getQuantity())
                    .collect(Collectors.toList()));
        }
        if (booking.getPayment() != null) {
            response.setPaymentMethod(booking.getPayment().getPaymentMethod());
            response.setPaymentStatus(booking.getPayment().getPaymentStatus());
            response.setTransactionId(booking.getPayment().getTransactionId());
            response.setPaidAt(booking.getPayment().getPaidAt());
        }
        return response;
    }

    private void createNotification(User user, String title, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notificationRepository.save(notification);
    }
}
