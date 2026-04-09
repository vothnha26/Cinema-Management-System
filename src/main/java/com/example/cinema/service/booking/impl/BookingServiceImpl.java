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
import com.example.cinema.repository.user.MembershipLevelRepository;
import com.example.cinema.repository.user.MembershipBenefitRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.service.booking.BookingService;
import com.example.cinema.service.commerce.PricingService;
import com.example.cinema.service.notification.INotificationAutomationService;
import com.example.cinema.service.notification.impl.EmailNotificationStrategy;
import com.example.cinema.service.user.CustomerService;
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
    private final PricingService pricingService;
    private final NotificationRepository notificationRepository;
    private final CustomerService customerService;
    private final INotificationAutomationService notificationAutomationService;
    private final EmailNotificationStrategy emailService;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String LOCK_KEY_PREFIX = "seat_lock:";
    private static final String DEADLINE_KEY_PREFIX = "seat_deadline:";
    private static final Duration HOLD_DURATION = Duration.ofMinutes(5);
    private static final Duration MAX_HOLD_LIMIT = Duration.ofMinutes(15);

    public BookingServiceImpl(BookingRepository bookingRepository, ShowtimeRepository showtimeRepository,
                              SeatRepository seatRepository, CustomerRepository customerRepository,
                              MembershipLevelRepository membershipLevelRepository,
                              MembershipBenefitRepository membershipBenefitRepository,
                              BookingDetailRepository bookingDetailRepository, ComboRepository comboRepository,
                              PromotionRepository promotionRepository, PricingService pricingService,
                              NotificationRepository notificationRepository,
                              CustomerService customerService,
                              INotificationAutomationService notificationAutomationService,
                              EmailNotificationStrategy emailService,
                              StringRedisTemplate redisTemplate,
                              SimpMessagingTemplate messagingTemplate) {
        this.bookingRepository = bookingRepository;
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.customerRepository = customerRepository;
        this.membershipLevelRepository = membershipLevelRepository;
        this.membershipBenefitRepository = membershipBenefitRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.comboRepository = comboRepository;
        this.promotionRepository = promotionRepository;
        this.pricingService = pricingService;
        this.notificationRepository = notificationRepository;
        this.customerService = customerService;
        this.notificationAutomationService = notificationAutomationService;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void holdSeat(Long showtimeId, Long seatId, String sessionId) {
        String lockKey = LOCK_KEY_PREFIX + showtimeId + ":" + seatId;
        String deadlineKey = DEADLINE_KEY_PREFIX + showtimeId + ":" + seatId + ":" + sessionId;
        String currentLockOwner = redisTemplate.opsForValue().get(lockKey);
        if (currentLockOwner == null) {
            redisTemplate.opsForValue().set(lockKey, sessionId, HOLD_DURATION);
            redisTemplate.opsForValue().set(deadlineKey, "ACTIVE", MAX_HOLD_LIMIT);
            broadcastSeatStatus(showtimeId, seatId, "HOLD", sessionId);
        } else if (sessionId.equals(currentLockOwner)) {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(deadlineKey))) {
                redisTemplate.expire(lockKey, HOLD_DURATION);
            } else {
                redisTemplate.delete(lockKey);
                broadcastSeatStatus(showtimeId, seatId, "RELEASE", sessionId);
                throw new AppException("Thời gian giữ ghế tối đa đã hết (15 phút).");
            }
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
                .map(k -> Long.parseLong(k.split(":")[2])).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating booking for request: email={}, phone={}, fullName={}", 
                request.getEmail(), request.getPhone(), request.getFullName());

        Customer customer = null;
        
        try {
            customer = getCurrentCustomer();
        } catch (Exception e) {
            log.info("No active session, proceeding as guest or checking phone.");
        }

        String phone = request.getPhone() != null ? request.getPhone().trim() : "";
        if (customer == null && !phone.isEmpty() && !phone.equals("0000000000")) {
            List<Customer> customers = customerRepository.findByPhone(phone);
            if (!customers.isEmpty()) {
                customer = customers.stream()
                        .filter(c -> c.getUser() != null)
                        .findFirst()
                        .orElse(customers.get(0));
                log.info("Found existing member by phone: id={}", customer.getId());
            }
        }
        
        if (customer == null) {
            customer = new Customer();
            customer.setFullName((request.getFullName() == null || request.getFullName().trim().isEmpty()) ? "Khách vãng lai" : request.getFullName().trim());
            customer.setPhone(phone.isEmpty() ? "0000000000" : phone);
            customer.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
            customer.setMembershipLevel(null);
            customer = customerRepository.save(customer);
        }

        Showtime showtime = getAndValidateShowtime(request.getShowtimeId());
        validateSeatsAvailability(showtime.getId(), request.getSeatIds());

        Booking booking = new Booking();
        booking.setCustomer(customer);
        
        List<BookingDetail> details = processSeatPricing(booking, showtime, request.getSeatIds(), customer);
        List<BookingCombo> combos = processComboPricing(booking, request.getCombos());

        // 1. TÍNH TIỀN VÉ GỐC (Chỉ bao gồm phụ thu)
        BigDecimal ticketTotal = details.stream().map(BookingDetail::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 2. ÁP DỤNG CHIẾT KHẤU HỘI VIÊN (Tập trung)
        BigDecimal membershipDiscount = BigDecimal.ZERO;
        if (customer != null && customer.getMembershipLevel() != null) {
            final BigDecimal currentTicketTotal = ticketTotal; // Phải là final để dùng trong lambda
            membershipDiscount = membershipBenefitRepository
                    .findByMembershipLevelAndBenefitType(customer.getMembershipLevel(), "DISCOUNT")
                    .map(b -> {
                        try {
                            BigDecimal val = new BigDecimal(b.getBenefitValue());
                            // Nếu giá trị < 100, coi là giảm theo %, nếu > 100 coi là giảm tiền mặt trực tiếp
                            if (val.compareTo(new BigDecimal("100")) < 0) {
                                return currentTicketTotal.multiply(val).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                            } else {
                                return val;
                            }
                        } catch (Exception e) { return BigDecimal.ZERO; }
                    }).orElse(BigDecimal.ZERO);

            if (membershipDiscount.compareTo(BigDecimal.ZERO) > 0) {
                ticketTotal = ticketTotal.subtract(membershipDiscount);
                log.info("Applied membership discount for {}: -{}", customer.getMembershipLevel().getName(), membershipDiscount);
            }
        }

        // 3. TÍNH TIỀN COMBO
        BigDecimal comboTotal = combos.stream().map(bc -> bc.getPrice().multiply(BigDecimal.valueOf(bc.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal initialTotal = ticketTotal.add(comboTotal);

        // 4. ÁP DỤNG VOUCHER
        BigDecimal finalTotalPrice = applyPromotion(booking, customer, request.getPromotionCode(), initialTotal);

        finalizeBooking(booking, customer, showtime, details, combos, finalTotalPrice, request.getPaymentMethod());
        Booking savedBooking = bookingRepository.save(booking);

        if (request.getPaymentMethod() == PaymentMethod.CASH || request.getPaymentMethod() == PaymentMethod.CARD) {
            for (Long seatId : request.getSeatIds()) {
                redisTemplate.delete(LOCK_KEY_PREFIX + showtime.getId() + ":" + seatId);
                broadcastSeatStatus(showtime.getId(), seatId, "BOOKED", "POS-STAFF");
            }
        }

        try {
            String targetEmail = (customer.getUser() != null) ? customer.getUser().getEmail() : request.getEmail();
            if (targetEmail != null && !targetEmail.isEmpty()) {
                String emailContent = String.format("<p>Mã đặt vé: <b>%s</b></p><p>Phim: %s</p><p>Tổng tiền: %,.0fđ</p>", 
                    savedBooking.getBookingCode(), showtime.getMovie().getTitle(), finalTotalPrice);
                emailService.send(targetEmail, "XÁC NHẬN ĐẶT VÉ - " + savedBooking.getBookingCode(), emailContent);
            }
        } catch (Exception e) { log.error("Mail error: {}", e.getMessage()); }

        if (customer != null && customer.getUser() != null && customer.getUser().getId() != null) {
            if (savedBooking.getPayment().getPaymentStatus() == PaymentStatus.SUCCESS) {
                customerService.addLoyaltyPoints(customer, finalTotalPrice);
            }
            createNotification(customer.getUser(), "Đặt vé thành công", "Mã: " + savedBooking.getBookingCode(), NotificationType.BOOKING);
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
        if (s.getStatus() != ShowtimeStatus.UPCOMING && s.getStatus() != ShowtimeStatus.SHOWING) {
            throw new AppException("Suất chiếu không khả dụng (Đã kết thúc hoặc bị hủy)");
        }
        return s;
    }

    private void validateSeatsAvailability(Long showtimeId, List<Long> seatIds) {
        List<Long> booked = bookingDetailRepository.findBookedSeatIdsByShowtime(showtimeId);
        for (Long id : seatIds) if (booked.contains(id)) throw new AppException("Ghế " + id + " đã bán");
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
                Combo c = comboRepository.findById(Long.parseLong(entry.getKey())).orElseThrow(() -> new AppException("Combo not found"));
                int quantity = entry.getValue();
                if (c.getStockQuantity() < quantity) throw new AppException("Combo " + c.getName() + " hết hàng");
                c.setStockQuantity(c.getStockQuantity() - quantity);
                comboRepository.save(c);
                BookingCombo bc = new BookingCombo();
                bc.setBooking(booking); bc.setCombo(c); bc.setQuantity(quantity); bc.setPrice(c.getPrice());
                list.add(bc);
            }
        }
        return list;
    }

    private BigDecimal applyPromotion(Booking booking, Customer customer, String code, BigDecimal current) {
        if (code == null || code.isEmpty()) return current;
        Promotion p = promotionRepository.findByCodeAndIsActiveTrue(code).orElseThrow(() -> new AppException("Mã không hợp lệ"));
        
        if (p.getMinLevel() != null) {
            int customerPriority = 0;
            if (customer.getMembershipLevel() != null) {
                customerPriority = customer.getMembershipLevel().getPriority();
            } else if (customer.getUser() != null) {
                customerPriority = 1;
            }

            if (customerPriority < p.getMinLevel().getPriority())
                throw new AppException("Hạng thành viên của bạn không đủ điều kiện áp dụng mã này (Yêu cầu: " + p.getMinLevel().getName() + ")");
        }
        booking.setPromotion(p);
        BigDecimal disc = (p.getDiscountType() == DiscountType.FIXED) ? p.getDiscountValue() : current.multiply(p.getDiscountValue()).divide(BigDecimal.valueOf(100));
        return current.subtract(disc);
    }

    private void finalizeBooking(Booking b, Customer c, Showtime s, List<BookingDetail> det, List<BookingCombo> com, BigDecimal price, PaymentMethod pm) {
        PaymentMethod method = pm != null ? pm : PaymentMethod.BANK_TRANSFER;
        
        // Chỉ CASH (Tiền mặt tại quầy) mới hoàn tất ngay. 
        // CARD (Thanh toán thẻ/QR tại quầy) phải PENDING để khách quét mã.
        boolean autoConfirm = method == PaymentMethod.CASH;
        
        b.setCustomer(c); 
        b.setShowtime(s); 
        b.setBookingCode("BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        b.setTotalPrice(price); 
        b.setStatus(autoConfirm ? BookingStatus.CHECKED_IN : BookingStatus.PENDING);
        b.setDetails(det); 
        b.setCombos(com);
        
        Payment p = new Payment(); 
        p.setBooking(b); 
        p.setAmount(price); 
        p.setPaymentMethod(method);
        p.setPaymentStatus(autoConfirm ? PaymentStatus.SUCCESS : PaymentStatus.PENDING);
        b.setPayment(p);
    }

    @Override public List<BookingResponse> getMyBookings() { return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(getCurrentCustomer().getId()).stream().map(this::mapToResponse).collect(Collectors.toList()); }
    @Override public BookingResponse getMyBookingByCode(String code) { return mapToResponse(bookingRepository.findByBookingCode(code).orElseThrow(() -> new AppException("Not found"))); }
    @Override @Transactional public void cancelBooking(String code) { Booking b = bookingRepository.findByBookingCode(code).orElseThrow(() -> new AppException("Not found")); b.setStatus(BookingStatus.CANCELLED); bookingRepository.save(b); }
    @Override @Transactional public void checkInBooking(String code) { Booking b = bookingRepository.findByBookingCode(code).orElseThrow(() -> new AppException("Not found")); b.setStatus(BookingStatus.CHECKED_IN); bookingRepository.save(b); }

    private BookingResponse mapToResponse(Booking b) {
        BookingResponse res = new BookingResponse();
        res.setId(b.getId()); res.setBookingCode(b.getBookingCode()); res.setTotalPrice(b.getTotalPrice());
        res.setStatus(b.getStatus()); res.setCreatedAt(b.getCreatedAt());
        if (b.getShowtime() != null) { res.setMovieTitle(b.getShowtime().getMovie().getTitle()); res.setRoomName(b.getShowtime().getRoom().getName()); res.setShowTime(b.getShowtime().getStartTime()); }
        if (b.getDetails() != null) res.setSeatCodes(b.getDetails().stream().map(BookingDetail::getSeatCode).collect(Collectors.toList()));
        if (b.getPayment() != null) { res.setPaymentMethod(b.getPayment().getPaymentMethod()); res.setPaymentStatus(b.getPayment().getPaymentStatus()); res.setTransactionId(b.getPayment().getTransactionId()); res.setPaidAt(b.getPayment().getPaidAt()); }
        if (b.getCombos() != null) res.setComboSummary(b.getCombos().stream().map(bc -> bc.getCombo().getName() + " (x" + bc.getQuantity() + ")").collect(Collectors.toList()));
        if (b.getPromotion() != null) res.setPromotionCode(b.getPromotion().getCode());
        return res;
    }

    private void createNotification(User user, String title, String msg, NotificationType type) {
        if (user == null || user.getId() == null) return;
        Notification n = new Notification(); n.setUser(user); n.setTitle(title); n.setMessage(msg); n.setType(type);
        notificationRepository.save(n);
    }
}
