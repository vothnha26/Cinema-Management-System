package com.example.cinema.service.booking.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.request.BookingRequest;
import com.example.cinema.model.dto.response.BookingResponse;
import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.*;
import com.example.cinema.repository.booking.BookingDetailRepository;
import com.example.cinema.repository.booking.BookingRepository;
import com.example.cinema.repository.booking.PaymentRepository;
import com.example.cinema.repository.commerce.ComboRepository;
import com.example.cinema.repository.commerce.PromotionRepository;
import com.example.cinema.repository.notification.NotificationRepository;
import com.example.cinema.repository.room.SeatRepository;
import com.example.cinema.repository.showtime.ShowtimeRepository;
import com.example.cinema.repository.user.MembershipLevelRepository;
import com.example.cinema.repository.user.MembershipBenefitRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.service.booking.BookingService;
import com.example.cinema.service.commerce.PricingService;
import com.example.cinema.service.notification.INotificationAutomationService;
import com.example.cinema.service.notification.impl.EmailNotificationStrategy;
import com.example.cinema.service.user.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
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
    private final MembershipLevelRepository membershipLevelRepository;
    private final MembershipBenefitRepository membershipBenefitRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final ComboRepository comboRepository;
    private final PromotionRepository promotionRepository;
    private final PaymentRepository paymentRepository;
    private final PricingService pricingService;
    private final NotificationRepository notificationRepository;
    private final CustomerService customerService;
    private final INotificationAutomationService notificationAutomationService;
    private final EmailNotificationStrategy emailService;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    private static final String LOCK_KEY_PREFIX = "seat_lock:";
    private static final String DEADLINE_KEY_PREFIX = "seat_deadline:";
    private static final String PENDING_BOOKING_PREFIX = "pending_booking:";
    private static final Duration HOLD_DURATION = Duration.ofMinutes(15);

    public BookingServiceImpl(BookingRepository bookingRepository, ShowtimeRepository showtimeRepository,
                              SeatRepository seatRepository, CustomerRepository customerRepository,
                              MembershipLevelRepository membershipLevelRepository,
                              MembershipBenefitRepository membershipBenefitRepository,
                              BookingDetailRepository bookingDetailRepository, ComboRepository comboRepository,
                              PromotionRepository promotionRepository, PaymentRepository paymentRepository,
                              PricingService pricingService,
                              NotificationRepository notificationRepository,
                              CustomerService customerService,
                              INotificationAutomationService notificationAutomationService,
                              EmailNotificationStrategy emailService,
                              StringRedisTemplate redisTemplate,
                              SimpMessagingTemplate messagingTemplate,
                              ObjectMapper objectMapper) {
        this.bookingRepository = bookingRepository;
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.customerRepository = customerRepository;
        this.membershipLevelRepository = membershipLevelRepository;
        this.membershipBenefitRepository = membershipBenefitRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.comboRepository = comboRepository;
        this.promotionRepository = promotionRepository;
        this.paymentRepository = paymentRepository;
        this.pricingService = pricingService;
        this.notificationRepository = notificationRepository;
        this.customerService = customerService;
        this.notificationAutomationService = notificationAutomationService;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void holdSeat(Long showtimeId, Long seatId, String sessionId) {
        String lockKey = LOCK_KEY_PREFIX + showtimeId + ":" + seatId;
        String deadlineKey = DEADLINE_KEY_PREFIX + showtimeId + ":" + seatId + ":" + sessionId;
        String currentLockOwner = redisTemplate.opsForValue().get(lockKey);
        if (currentLockOwner == null) {
            redisTemplate.opsForValue().set(lockKey, sessionId, HOLD_DURATION);
            redisTemplate.opsForValue().set(deadlineKey, "ACTIVE", HOLD_DURATION);
            broadcastSeatStatus(showtimeId, seatId, "HOLD", sessionId);
        } else if (sessionId.equals(currentLockOwner)) {
            redisTemplate.expire(lockKey, HOLD_DURATION);
            redisTemplate.expire(deadlineKey, HOLD_DURATION);
        } else throw new AppException("Ghế này đang được người khác chọn");
    }

    @Override public void releaseSeat(Long showtimeId, Long seatId, String sessionId) {
        String lockKey = LOCK_KEY_PREFIX + showtimeId + ":" + seatId;
        String deadlineKey = DEADLINE_KEY_PREFIX + showtimeId + ":" + seatId + ":" + sessionId;
        if (sessionId.equals(redisTemplate.opsForValue().get(lockKey))) {
            redisTemplate.delete(lockKey); redisTemplate.delete(deadlineKey);
            broadcastSeatStatus(showtimeId, seatId, "RELEASE", sessionId);
        }
    }

    private void broadcastSeatStatus(Long showtimeId, Long seatId, String action, String sessionId) {
        Map<String, Object> message = new HashMap<>();
        message.put("seatId", seatId);
        message.put("action", action);
        message.put("sessionId", sessionId);
        messagingTemplate.convertAndSend("/topic/showtime/" + showtimeId + "/seats", message);
    }

    @Override public List<Long> getMyLockedSeats(Long showtimeId, String sessionId) {
        String pattern = LOCK_KEY_PREFIX + showtimeId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null) return new ArrayList<>();
        return keys.stream().filter(k -> sessionId.equals(redisTemplate.opsForValue().get(k)))
                .map(k -> {
                    String[] parts = k.split(":");
                    return Long.parseLong(parts[parts.length-1]);
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating temporary booking in Redis: email={}, phone={}", request.getEmail(), request.getPhone());

        Showtime showtime = getAndValidateShowtime(request.getShowtimeId());
        validateSeatsAvailability(showtime.getId(), request.getSeatIds());

        // Tạo mã Booking ngẫu nhiên
        String bookingCode = "BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Tính giá sơ bộ để hiển thị cho UI (không lưu)
        BigDecimal total = calculateEstimatedPrice(request, showtime);

        BookingResponse res = new BookingResponse();
        res.setBookingCode(bookingCode);
        res.setTotalPrice(total);
        res.setMovieTitle(showtime.getMovie().getTitle());
        res.setRoomName(showtime.getRoom().getName());
        res.setShowTime(showtime.getStartTime());
        res.setStatus(BookingStatus.PENDING);
        res.setPaymentMethod(request.getPaymentMethod());

        // Lưu data GỐC (Request) và data HIỂN THỊ (Response) vào Redis
        try {
            Map<String, Object> redisData = new HashMap<>();
            redisData.put("request", request);
            redisData.put("response", res);
            String json = objectMapper.writeValueAsString(redisData);
            redisTemplate.opsForValue().set(PENDING_BOOKING_PREFIX + bookingCode, json, HOLD_DURATION);
        } catch (Exception e) {
            throw new AppException("Lỗi lưu dữ liệu tạm thời: " + e.getMessage());
        }

        return res;
    }

    private BigDecimal calculateEstimatedPrice(BookingRequest request, Showtime showtime) {
        BigDecimal total = BigDecimal.ZERO;
        Customer tempCust = null;
        try { tempCust = getCurrentCustomer(); } catch (Exception e) {}
        
        for (Long sId : request.getSeatIds()) {
            Seat s = seatRepository.findById(sId).orElseThrow();
            total = total.add(pricingService.calculateTicketPrice(showtime, s, tempCust).getFinalPrice());
        }
        if (request.getCombos() != null) {
            for (Map.Entry<String, Integer> entry : request.getCombos().entrySet()) {
                Combo c = comboRepository.findById(Long.parseLong(entry.getKey())).orElseThrow();
                total = total.add(c.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
            }
        }
        return total;
    }

    @Override
    @Transactional
    public BookingResponse finalizeBooking(String bookingCode, String transactionId) {
        log.info("Finalizing booking from Redis to DB: {}", bookingCode);
        
        String json = redisTemplate.opsForValue().get(PENDING_BOOKING_PREFIX + bookingCode);
        if (json == null) {
            return bookingRepository.findByBookingCode(bookingCode)
                    .map(this::mapToResponse)
                    .orElseThrow(() -> new AppException("Yêu cầu đặt vé đã quá hạn hoặc không tồn tại."));
        }

        BookingRequest request;
        try { 
            Map<String, Object> redisData = objectMapper.readValue(json, Map.class);
            request = objectMapper.convertValue(redisData.get("request"), BookingRequest.class);
        }
        catch (Exception e) { throw new AppException("Lỗi đọc dữ liệu tạm"); }

        Showtime showtime = getAndValidateShowtime(request.getShowtimeId());
        
        // Tìm hoặc tạo khách hàng
        Customer customer = findOrCreateCustomer(request);

        Booking booking = new Booking();
        booking.setBookingCode(bookingCode);
        booking.setShowtime(showtime);
        booking.setCustomer(customer);
        booking.setStatus(BookingStatus.CONFIRMED);

        List<BookingDetail> details = processSeatPricing(booking, showtime, request.getSeatIds(), customer);
        List<BookingCombo> combos = processComboPricing(booking, request.getCombos());

        BigDecimal ticketTotal = details.stream().map(BookingDetail::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal comboTotal = combos.stream().map(bc -> bc.getPrice().multiply(BigDecimal.valueOf(bc.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal finalTotalPrice = applyPromotion(booking, customer, request.getPromotionCode(), ticketTotal.add(comboTotal));
        booking.setTotalPrice(finalTotalPrice);
        booking.setDetails(details);
        booking.setCombos(combos);

        Booking savedBooking = bookingRepository.save(booking);

        // Tạo bản ghi Payment chính thức
        Payment p = new Payment();
        p.setBooking(savedBooking);
        p.setAmount(finalTotalPrice);
        p.setPaymentMethod(request.getPaymentMethod());
        p.setPaymentStatus(PaymentStatus.SUCCESS);
        p.setTransactionId(transactionId != null ? transactionId : "TXN-" + System.currentTimeMillis());
        p.setPaidAt(LocalDateTime.now());
        paymentRepository.save(p);
        savedBooking.setPayment(p);

        // Gửi Email & Thông báo
        sendConfirmationNotifications(savedBooking, customer, showtime);

        // Giải phóng Redis
        cleanupRedisData(bookingCode, showtime.getId(), request.getSeatIds());

        return mapToResponse(savedBooking);
    }

    private Customer findOrCreateCustomer(BookingRequest request) {
        String phone = request.getPhone() != null ? request.getPhone().trim() : "0000000000";
        return customerRepository.findByPhone(phone).stream()
                .findFirst()
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setFullName(request.getFullName() != null ? request.getFullName().trim() : "Khách vãng lai");
                    c.setPhone(phone);
                    c.setEmail(request.getEmail());
                    return customerRepository.save(c);
                });
    }

    private void cleanupRedisData(String bookingCode, Long showtimeId, List<Long> seatIds) {
        redisTemplate.delete(PENDING_BOOKING_PREFIX + bookingCode);
        for (Long sId : seatIds) {
            redisTemplate.delete(LOCK_KEY_PREFIX + showtimeId + ":" + sId);
            broadcastSeatStatus(showtimeId, sId, "BOOKED", "SYSTEM-AUTO");
        }
    }

    private void sendConfirmationNotifications(Booking b, Customer c, Showtime s) {
        try {
            String targetEmail = (c.getUser() != null) ? c.getUser().getEmail() : b.getCustomer().getEmail();
            if (targetEmail != null && !targetEmail.isEmpty()) {
                String content = String.format("<p>Mã đặt vé: <b>%s</b></p><p>Phim: %s</p><p>Suất chiếu: %s</p><p>Tổng tiền: %,.0fđ</p>",
                        b.getBookingCode(), s.getMovie().getTitle(), s.getStartTime(), b.getTotalPrice());
                emailService.send(targetEmail, "XÁC NHẬN ĐẶT VÉ THÀNH CÔNG - " + b.getBookingCode(), content);
            }
        } catch (Exception e) { log.error("Mail error: {}", e.getMessage()); }
        
        if (c.getUser() != null) {
            customerService.addLoyaltyPoints(c, b.getTotalPrice());
            createNotification(c.getUser(), "Thanh toán thành công", "Mã đơn hàng: " + b.getBookingCode(), NotificationType.BOOKING);
        }
    }

    private Customer getCurrentCustomer() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) throw new AppException("Unauthorized");
        return customerRepository.findByUserUsername(((UserDetails) principal).getUsername())
                .orElseThrow(() -> new AppException("Customer not found"));
    }

    private Showtime getAndValidateShowtime(Long id) {
        Showtime s = showtimeRepository.findById(id).orElseThrow(() -> new AppException("Showtime not found"));
        if (s.getStatus() == ShowtimeStatus.CANCELLED) throw new AppException("Suất chiếu đã bị hủy");
        return s;
    }

    private void validateSeatsAvailability(Long showtimeId, List<Long> seatIds) {
        List<Long> booked = bookingDetailRepository.findBookedSeatIdsByShowtime(showtimeId);
        for (Long id : seatIds) if (booked.contains(id)) throw new AppException("Ghế " + id + " đã được bán");
    }

    private List<BookingDetail> processSeatPricing(Booking booking, Showtime showtime, List<Long> seatIds, Customer customer) {
        List<BookingDetail> details = new ArrayList<>();
        for (Long id : seatIds) {
            Seat seat = seatRepository.findById(id).orElseThrow(() -> new AppException("Seat not found"));
            BookingDetail d = new BookingDetail();
            d.setBooking(booking); d.setSeat(seat); d.setSeatCode(seat.getSeatCode());
            d.setPrice(pricingService.calculateTicketPrice(showtime, seat, customer).getFinalPrice());
            details.add(d);
        }
        return details;
    }

    private List<BookingCombo> processComboPricing(Booking booking, Map<String, Integer> comboRequests) {
        List<BookingCombo> list = new ArrayList<>();
        if (comboRequests != null) {
            for (Map.Entry<String, Integer> entry : comboRequests.entrySet()) {
                Combo c = comboRepository.findById(Long.parseLong(entry.getKey())).orElseThrow();
                int qty = entry.getValue();
                if (c.getStockQuantity() < qty) throw new AppException("Combo " + c.getName() + " hết hàng");
                c.setStockQuantity(c.getStockQuantity() - qty);
                comboRepository.save(c);
                BookingCombo bc = new BookingCombo();
                bc.setBooking(booking); bc.setCombo(c); bc.setQuantity(qty); bc.setPrice(c.getPrice());
                list.add(bc);
            }
        }
        return list;
    }

    private BigDecimal applyPromotion(Booking booking, Customer customer, String code, BigDecimal current) {
        if (code == null || code.isEmpty()) return current;
        Promotion p = promotionRepository.findByCodeAndIsActiveTrue(code).orElseThrow(() -> new AppException("Mã không hợp lệ"));
        booking.setPromotion(p);
        BigDecimal disc = (p.getDiscountType() == DiscountType.FIXED) ? p.getDiscountValue() : current.multiply(p.getDiscountValue()).divide(BigDecimal.valueOf(100));
        return current.subtract(disc);
    }

    @Override public List<BookingResponse> getMyBookings() { return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(getCurrentCustomer().getId()).stream().map(this::mapToResponse).collect(Collectors.toList()); }
    @Override public BookingResponse getMyBookingByCode(String code) { return mapToResponse(bookingRepository.findByBookingCode(code).orElseThrow(() -> new AppException("Không tìm thấy đơn hàng"))); }
    @Override @Transactional public void cancelBooking(String code) { Booking b = bookingRepository.findByBookingCode(code).orElseThrow(); b.setStatus(BookingStatus.CANCELLED); bookingRepository.save(b); }
    @Override @Transactional public void checkInBooking(String code) { Booking b = bookingRepository.findByBookingCode(code).orElseThrow(); b.setStatus(BookingStatus.CHECKED_IN); bookingRepository.save(b); }

    private BookingResponse mapToResponse(Booking b) {
        BookingResponse res = new BookingResponse();
        res.setId(b.getId()); res.setBookingCode(b.getBookingCode()); res.setTotalPrice(b.getTotalPrice());
        res.setStatus(b.getStatus()); res.setCreatedAt(b.getCreatedAt());
        if (b.getShowtime() != null) { res.setMovieTitle(b.getShowtime().getMovie().getTitle()); res.setRoomName(b.getShowtime().getRoom().getName()); res.setShowTime(b.getShowtime().getStartTime()); }
        if (b.getDetails() != null) res.setSeatCodes(b.getDetails().stream().map(BookingDetail::getSeatCode).collect(Collectors.toList()));
        if (b.getPayment() != null) { res.setPaymentMethod(b.getPayment().getPaymentMethod()); res.setPaymentStatus(b.getPayment().getPaymentStatus()); res.setTransactionId(b.getPayment().getTransactionId()); res.setPaidAt(b.getPayment().getPaidAt()); }
        return res;
    }

    private void createNotification(User user, String title, String msg, NotificationType type) {
        if (user == null || user.getId() == null) return;
        Notification n = new Notification(); n.setUser(user); n.setTitle(title); n.setMessage(msg); n.setType(type);
        notificationRepository.save(n);
    }
}
