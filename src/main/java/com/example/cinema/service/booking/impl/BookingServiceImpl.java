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
import com.example.cinema.repository.room.SeatRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.service.booking.BookingService;
import com.example.cinema.service.commerce.PricingService;
import com.example.cinema.service.notification.INotificationAutomationService;
import com.example.cinema.service.notification.impl.EmailNotificationStrategy;
import com.example.cinema.service.user.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final PricingService pricingService;
    private final NotificationRepository notificationRepository;
    private final CustomerService customerService;
    private final INotificationAutomationService notificationAutomationService;
    private final EmailNotificationStrategy emailService;
    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_KEY_PREFIX = "seat_lock:";
    private static final String DEADLINE_KEY_PREFIX = "seat_deadline:";
    private static final Duration HOLD_DURATION = Duration.ofMinutes(5);
    private static final Duration MAX_HOLD_LIMIT = Duration.ofMinutes(15);

    public BookingServiceImpl(BookingRepository bookingRepository, ShowtimeRepository showtimeRepository,
                              SeatRepository seatRepository, CustomerRepository customerRepository,
                              BookingDetailRepository bookingDetailRepository, ComboRepository comboRepository,
                              PromotionRepository promotionRepository, PricingService pricingService,
                              NotificationRepository notificationRepository,
                              CustomerService customerService,
                              INotificationAutomationService notificationAutomationService,
                              EmailNotificationStrategy emailService,
                              StringRedisTemplate redisTemplate) {
        this.bookingRepository = bookingRepository;
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.customerRepository = customerRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.comboRepository = comboRepository;
        this.promotionRepository = promotionRepository;
        this.pricingService = pricingService;
        this.notificationRepository = notificationRepository;
        this.customerService = customerService;
        this.notificationAutomationService = notificationAutomationService;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void holdSeat(Long showtimeId, Long seatId, String sessionId) {
        String lockKey = LOCK_KEY_PREFIX + showtimeId + ":" + seatId;
        String deadlineKey = DEADLINE_KEY_PREFIX + showtimeId + ":" + seatId + ":" + sessionId;
        String currentLockOwner = redisTemplate.opsForValue().get(lockKey);
        if (currentLockOwner == null) {
            redisTemplate.opsForValue().set(lockKey, sessionId, HOLD_DURATION);
            redisTemplate.opsForValue().set(deadlineKey, "ACTIVE", MAX_HOLD_LIMIT);
        } else if (sessionId.equals(currentLockOwner)) {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(deadlineKey))) {
                redisTemplate.expire(lockKey, HOLD_DURATION);
            } else {
                redisTemplate.delete(lockKey);
                throw new AppException("Thời gian giữ ghế tối đa đã hết (15 phút).");
            }
        } else throw new AppException("Ghế này đang được người khác chọn");
    }

    @Override public void releaseSeat(Long showtimeId, Long seatId, String sessionId) {
        String lockKey = LOCK_KEY_PREFIX + showtimeId + ":" + seatId;
        String deadlineKey = DEADLINE_KEY_PREFIX + showtimeId + ":" + seatId + ":" + sessionId;
        if (sessionId.equals(redisTemplate.opsForValue().get(lockKey))) {
            redisTemplate.delete(lockKey); redisTemplate.delete(deadlineKey);
        }
    }

    @Override public List<Long> getMyLockedSeats(Long showtimeId, String sessionId) {
        String pattern = LOCK_KEY_PREFIX + showtimeId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null) return new ArrayList<>();
        return keys.stream().filter(k -> sessionId.equals(redisTemplate.opsForValue().get(k)))
                .map(k -> Long.parseLong(k.split(":")[2])).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Starting booking process. Request info: {}", request.getEmail());

        Customer customer = null;
        
        // 1. ƯU TIÊN KIỂM TRA TÀI KHOẢN ĐANG ĐĂNG NHẬP
        try {
            customer = getCurrentCustomer();
            log.info("Booking for logged-in customer: {}", customer.getFullName());
        } catch (Exception e) {
            log.info("No active session, checking for guest or existing phone.");
        }

        // 2. NẾU KHÔNG CÓ SESSION, KIỂM TRA SỐ ĐIỆN THOẠI TRONG DB
        if (customer == null && request.getPhone() != null && !request.getPhone().isEmpty()) {
            customer = customerRepository.findByPhone(request.getPhone()).orElse(null);
        }
        
        // 3. NẾU VẪN KHÔNG CÓ, TẠO MỚI KHÁCH VÃNG LAI
        if (customer == null) {
            log.info("Creating new guest customer record.");
            customer = new Customer();
            customer.setFullName(request.getFullName());
            customer.setPhone(request.getPhone());
            customer.setEmail(request.getEmail());
            customer.setMembershipTier(MembershipTier.STANDARD);
            customer = customerRepository.save(customer);
        }

        Showtime showtime = getAndValidateShowtime(request.getShowtimeId());
        validateSeatsAvailability(showtime.getId(), request.getSeatIds());

        Booking booking = new Booking();
        booking.setCustomer(customer);
        
        List<BookingDetail> details = processSeatPricing(booking, showtime, request.getSeatIds());
        List<BookingCombo> combos = processComboPricing(booking, request.getCombos());

        BigDecimal totalPrice = calculateInitialTotal(details, combos);
        totalPrice = applyPromotion(booking, customer, request.getPromotionCode(), totalPrice);
        if (customer.getUser() != null) { 
            totalPrice = applyMembershipDiscount(customer, totalPrice);
        }

        finalizeBooking(booking, customer, showtime, details, combos, totalPrice, request.getPaymentMethod());
        Booking savedBooking = bookingRepository.save(booking);

        request.getSeatIds().forEach(seatId -> redisTemplate.delete(LOCK_KEY_PREFIX + showtime.getId() + ":" + seatId));

        // 4. GỬI EMAIL THÔNG BÁO MÃ VÉ
        String targetEmail = (customer.getUser() != null) ? customer.getUser().getEmail() : request.getEmail();
        if (targetEmail != null && !targetEmail.isEmpty()) {
            String emailContent = String.format(
                "<p>Chúc mừng bạn đã đặt vé thành công tại <b>StarCinema</b>!</p>" +
                "<p>Mã đặt vé của bạn là: <span style='font-size: 20px; color: #E5133A; font-weight: 800;'>%s</span></p>" +
                "<p>Phim: <b>%s</b></p>" +
                "<p>Suất chiếu: <b>%s</b></p>" +
                "<p>Ghế: <b>%s</b></p>" +
                "<p>Tổng tiền: <b>%,.0fđ</b></p>" +
                "<p>Vui lòng đưa mã này cho nhân viên tại quầy để nhận vé.</p>",
                savedBooking.getBookingCode(),
                showtime.getMovie().getTitle(),
                showtime.getStartTime().toString().replace("T", " "),
                details.stream().map(BookingDetail::getSeatCode).collect(Collectors.joining(", ")),
                totalPrice
            );
            emailService.send(targetEmail, "XÁC NHẬN ĐẶT VÉ THÀNH CÔNG - " + savedBooking.getBookingCode(), emailContent);
        }

        if (customer.getUser() != null) {
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
        if (!(principal instanceof UserDetails)) throw new AppException("Unauthorized");
        return customerRepository.findByUserUsername(((UserDetails) principal).getUsername())
                .orElseThrow(() -> new AppException("Customer not found"));
    }

    private Showtime getAndValidateShowtime(Long id) {
        Showtime s = showtimeRepository.findById(id).orElseThrow(() -> new AppException("Showtime not found"));
        if (s.getStatus() != ShowtimeStatus.UPCOMING) throw new AppException("Suất chiếu không khả dụng");
        return s;
    }

    private void validateSeatsAvailability(Long showtimeId, List<Long> seatIds) {
        List<Long> booked = bookingDetailRepository.findBookedSeatIdsByShowtime(showtimeId);
        for (Long id : seatIds) if (booked.contains(id)) throw new AppException("Ghế " + id + " đã bán");
    }

    private List<BookingDetail> processSeatPricing(Booking booking, Showtime showtime, List<Long> seatIds) {
        List<BookingDetail> details = new ArrayList<>();
        for (Long id : seatIds) {
            Seat seat = seatRepository.findById(id).orElseThrow(() -> new AppException("Seat not found"));
            BookingDetail d = new BookingDetail();
            d.setBooking(booking); d.setSeat(seat); d.setSeatCode(seat.getSeatCode());
            d.setPrice(pricingService.calculateTicketPrice(showtime, seat).getFinalPrice());
            details.add(d);
        }
        return details;
    }

    private List<BookingCombo> processComboPricing(Booking booking, Map<String, Integer> comboRequests) {
        List<BookingCombo> list = new ArrayList<>();
        if (comboRequests != null) {
            for (Map.Entry<String, Integer> entry : comboRequests.entrySet()) {
                Combo c = comboRepository.findById(Long.parseLong(entry.getKey())).orElseThrow(() -> new AppException("Combo not found"));
                BookingCombo bc = new BookingCombo();
                bc.setBooking(booking); bc.setCombo(c); bc.setQuantity(entry.getValue()); bc.setPrice(c.getPrice());
                list.add(bc);
            }
        }
        return list;
    }

    private BigDecimal calculateInitialTotal(List<BookingDetail> details, List<BookingCombo> combos) {
        BigDecimal total = details.stream().map(BookingDetail::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cTotal = combos.stream().map(bc -> bc.getPrice().multiply(BigDecimal.valueOf(bc.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.add(cTotal);
    }

    private BigDecimal applyPromotion(Booking booking, Customer customer, String code, BigDecimal current) {
        if (code == null || code.isEmpty()) return current;
        Promotion p = promotionRepository.findByCodeAndIsActiveTrue(code).orElseThrow(() -> new AppException("Mã không hợp lệ"));
        if (customer.getUser() != null && p.getMinTier() != null) {
            if (customer.getMembershipTier().ordinal() < p.getMinTier().getTier().ordinal()) throw new AppException("Hạng không đủ");
        }
        booking.setPromotion(p);
        BigDecimal disc = (p.getDiscountType() == DiscountType.FIXED) ? p.getDiscountValue() : current.multiply(p.getDiscountValue()).divide(BigDecimal.valueOf(100));
        return current.subtract(disc);
    }

    private BigDecimal applyMembershipDiscount(Customer c, BigDecimal current) {
        BigDecimal pct = customerService.getDiscountPercentage(c.getMembershipTier());
        return current.subtract(current.multiply(pct).divide(BigDecimal.valueOf(100)));
    }

    private void finalizeBooking(Booking b, Customer c, Showtime s, List<BookingDetail> det, List<BookingCombo> com, BigDecimal price, PaymentMethod pm) {
        PaymentMethod method = pm != null ? pm : PaymentMethod.MOMO;
        boolean isStaff = method == PaymentMethod.CASH || method == PaymentMethod.CARD;
        b.setCustomer(c); b.setShowtime(s); b.setBookingCode("BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        b.setTotalPrice(price); b.setStatus(isStaff ? BookingStatus.CHECKED_IN : BookingStatus.PENDING);
        b.setDetails(det); b.setCombos(com);
        Payment p = new Payment(); p.setBooking(b); p.setAmount(price); p.setPaymentMethod(method);
        p.setPaymentStatus(isStaff ? PaymentStatus.SUCCESS : PaymentStatus.PENDING);
        b.setPayment(p);
    }

    @Override public List<BookingResponse> getMyBookings() { return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(getCurrentCustomer().getId()).stream().map(this::mapToResponse).collect(Collectors.toList()); }
    @Override public BookingResponse getMyBookingByCode(String code) { return mapToResponse(bookingRepository.findByBookingCode(code).orElseThrow(() -> new AppException("Not found"))); }
    @Override @Transactional public void cancelBooking(String code) { Booking b = bookingRepository.findByBookingCode(code).orElseThrow(() -> new AppException("Not found")); b.setStatus(BookingStatus.CANCELLED); bookingRepository.save(b); }
    @Override @Transactional public void checkInBooking(String code) { Booking b = bookingRepository.findByBookingCode(code).orElseThrow(() -> new AppException("Not found")); b.setStatus(BookingStatus.CHECKED_IN); bookingRepository.save(b); }

    private BookingResponse mapToResponse(Booking b) {
        BookingResponse res = new BookingResponse();
        res.setBookingCode(b.getBookingCode());
        res.setMovieTitle(b.getShowtime().getMovie().getTitle());
        res.setTotalPrice(b.getTotalPrice());
        res.setStatus(b.getStatus());
        res.setSeatCodes(b.getDetails().stream().map(d -> d.getSeat().getSeatCode()).collect(Collectors.toList()));
        return res;
    }

    private void createNotification(User user, String title, String msg, NotificationType type) {
        if (user == null) return;
        Notification n = new Notification(); n.setUser(user); n.setTitle(title); n.setMessage(msg); n.setType(type);
        notificationRepository.save(n);
    }
}
