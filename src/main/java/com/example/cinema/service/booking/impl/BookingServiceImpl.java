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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;
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
    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_KEY_PREFIX = "seat_lock:";

    public BookingServiceImpl(BookingRepository bookingRepository, ShowtimeRepository showtimeRepository,
                              SeatRepository seatRepository, CustomerRepository customerRepository,
                              BookingDetailRepository bookingDetailRepository, ComboRepository comboRepository,
                              PromotionRepository promotionRepository, SeatPriceRepository seatPriceRepository,
                              NotificationRepository notificationRepository,
                              CustomerService customerService,
                              INotificationAutomationService notificationAutomationService,
                              StringRedisTemplate redisTemplate) {
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
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void holdSeat(Long showtimeId, Long seatId, String sessionId) {
        String key = LOCK_KEY_PREFIX + showtimeId + ":" + seatId;
        
        // Sử dụng setIfAbsent (NX) của Redis để khóa nguyên tử
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, sessionId, Duration.ofMinutes(5));
        
        if (Boolean.FALSE.equals(success)) {
            String currentLockOwner = redisTemplate.opsForValue().get(key);
            if (!sessionId.equals(currentLockOwner)) {
                throw new AppException("Ghế này đang được người khác chọn");
            }
            // Nếu là chính mình đang giữ, gia hạn thêm 5 phút
            redisTemplate.expire(key, Duration.ofMinutes(5));
        }
    }

    @Override
    public void releaseSeat(Long showtimeId, Long seatId, String sessionId) {
        String key = LOCK_KEY_PREFIX + showtimeId + ":" + seatId;
        String currentLockOwner = redisTemplate.opsForValue().get(key);
        if (sessionId.equals(currentLockOwner)) {
            redisTemplate.delete(key);
        }
    }

    @Override
    public List<Long> getMyLockedSeats(Long showtimeId, String sessionId) {
        String pattern = LOCK_KEY_PREFIX + showtimeId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null) return new ArrayList<>();

        List<Long> mySeats = new ArrayList<>();
        for (String key : keys) {
            if (sessionId.equals(redisTemplate.opsForValue().get(key))) {
                String[] parts = key.split(":");
                mySeats.add(Long.parseLong(parts[2]));
            }
        }
        return mySeats;
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Starting booking process for request: {}", request);

        Customer customer = null;
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            customer = customerRepository.findByPhone(request.getPhone()).orElse(null);
        }
        
        if (customer == null) {
            try {
                customer = getCurrentCustomer();
            } catch (Exception e) {
                log.debug("Proceeding as guest guest");
            }
        }

        Showtime showtime = getAndValidateShowtime(request.getShowtimeId());
        validateSeatsAvailability(showtime.getId(), request.getSeatIds());

        Booking booking = new Booking();
        booking.setCustomer(customer);
        
        List<BookingDetail> details = processSeatPricing(booking, showtime, request.getSeatIds());
        List<BookingCombo> combos = processComboPricing(booking, request.getCombos());

        BigDecimal totalPrice = calculateInitialTotal(details, combos);

        totalPrice = applyPromotion(booking, customer, request.getPromotionCode(), totalPrice);
        if (customer != null) {
            totalPrice = applyMembershipDiscount(customer, totalPrice);
        }

        finalizeBooking(booking, customer, showtime, details, combos, totalPrice, request.getPaymentMethod());
        Booking savedBooking = bookingRepository.save(booking);

        // Sau khi đặt thành công, giải phóng các khóa ghế trong Redis
        request.getSeatIds().forEach(seatId -> {
            String key = LOCK_KEY_PREFIX + showtime.getId() + ":" + seatId;
            redisTemplate.delete(key);
        });

        if (customer != null) {
            if (savedBooking.getPayment().getPaymentStatus() == PaymentStatus.SUCCESS) {
                customerService.addLoyaltyPoints(customer, totalPrice);
            }
            createNotification(customer.getUser(), "Đặt vé thành công", 
                "Booking " + savedBooking.getBookingCode() + " đã được xác nhận.", NotificationType.BOOKING);
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
                throw new AppException("Ghế " + seatId + " đã có người đặt");
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
            detail.setSeatCode(seat.getSeatCode());
            SeatPrice sp = seatPriceRepository.findLatestPrice(
                    showtime.getRoom().getType(), seat.getType(), showtime.getStartTime().toLocalDate())
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
                Combo combo = comboRepository.findById(comboId).orElseThrow(() -> new AppException("Combo not found"));
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
        BigDecimal total = details.stream().map(BookingDetail::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cTotal = combos.stream().map(bc -> bc.getPrice().multiply(BigDecimal.valueOf(bc.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.add(cTotal);
    }

    private BigDecimal applyPromotion(Booking booking, Customer customer, String promoCode, BigDecimal currentTotal) {
        if (promoCode == null || promoCode.isEmpty()) return currentTotal;
        Promotion promotion = promotionRepository.findByCodeAndIsActiveTrue(promoCode).orElseThrow(() -> new AppException("Mã không hợp lệ"));
        if (customer != null && promotion.getMinTier() != null) {
            if (customer.getMembershipTier().ordinal() < promotion.getMinTier().getTier().ordinal()) {
                throw new AppException("Hạng thành viên không đủ điều kiện");
            }
        }
        booking.setPromotion(promotion);
        BigDecimal discount = (promotion.getDiscountType() == DiscountType.FIXED) ? promotion.getDiscountValue() : currentTotal.multiply(promotion.getDiscountValue()).divide(BigDecimal.valueOf(100));
        return currentTotal.subtract(discount);
    }

    private BigDecimal applyMembershipDiscount(Customer customer, BigDecimal currentTotal) {
        BigDecimal pct = customerService.getDiscountPercentage(customer.getMembershipTier());
        return currentTotal.subtract(currentTotal.multiply(pct).divide(BigDecimal.valueOf(100)));
    }

    private void finalizeBooking(Booking booking, Customer customer, Showtime showtime, List<BookingDetail> details, List<BookingCombo> combos, BigDecimal totalPrice, PaymentMethod paymentMethod) {
        PaymentMethod pm = paymentMethod != null ? paymentMethod : PaymentMethod.MOMO;
        boolean isStaff = pm == PaymentMethod.CASH || pm == PaymentMethod.CARD;
        booking.setCustomer(customer);
        booking.setShowtime(showtime);
        booking.setBookingCode("BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(isStaff ? BookingStatus.CONFIRMED : BookingStatus.PENDING);
        booking.setDetails(details);
        booking.setCombos(combos);
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(totalPrice);
        payment.setPaymentMethod(pm);
        payment.setPaymentStatus(isStaff ? PaymentStatus.SUCCESS : PaymentStatus.PENDING);
        booking.setPayment(payment);
    }

    @Override public List<BookingResponse> getMyBookings() { return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(getCurrentCustomer().getId()).stream().map(this::mapToResponse).collect(Collectors.toList()); }
    @Override public BookingResponse getMyBookingByCode(String code) { return mapToResponse(bookingRepository.findByBookingCode(code).orElseThrow(() -> new AppException("Not found"))); }
    
    @Override @Transactional public void cancelBooking(String code) { 
        Booking b = bookingRepository.findByBookingCode(code).orElseThrow(() -> new AppException("Not found"));
        b.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(b);
    }

    @Override @Transactional public void checkInBooking(String code) {
        Booking b = bookingRepository.findByBookingCode(code).orElseThrow(() -> new AppException("Not found"));
        b.setStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(b);
    }

    private BookingResponse mapToResponse(Booking booking) {
        BookingResponse res = new BookingResponse();
        res.setBookingCode(booking.getBookingCode());
        res.setMovieTitle(booking.getShowtime().getMovie().getTitle());
        res.setTotalPrice(booking.getTotalPrice());
        res.setStatus(booking.getStatus());
        res.setSeatCodes(booking.getDetails().stream().map(d -> d.getSeat().getSeatCode()).collect(Collectors.toList()));
        return res;
    }

    private void createNotification(User user, String title, String msg, NotificationType type) {
        if (user == null) return;
        Notification n = new Notification();
        n.setUser(user); n.setTitle(title); n.setMessage(msg); n.setType(type);
        notificationRepository.save(n);
    }
}
