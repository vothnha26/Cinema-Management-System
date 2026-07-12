package com.example.cinema.service.booking.impl;

import com.example.cinema.exception.AppException;
import com.example.cinema.model.constant.AppConstants;
import com.example.cinema.model.dto.request.BookingRequest;
import com.example.cinema.model.dto.response.BookingResponse;
import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.*;
import com.example.cinema.service.booking.*;
import com.example.cinema.service.booking.infrastructure.BookingRepositoryFacade;
import com.example.cinema.service.booking.infrastructure.ProductCatalogFacade;
import com.example.cinema.service.booking.mapper.BookingMapper;
import com.example.cinema.service.commerce.PricingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    private final BookingRepositoryFacade bookingRepo;
    private final ProductCatalogFacade catalogRepo;
    private final PricingService pricingService;
    private final ISeatLockService seatLockService;
    private final IBookingNotificationService notificationService;
    private final BookingMapper bookingMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PENDING_BOOKING_PREFIX = AppConstants.REDIS_PENDING_BOOKING_PREFIX;
    private static final Duration HOLD_DURATION = Duration.ofMinutes(AppConstants.REDIS_HOLD_DURATION_MINUTES);

    public BookingServiceImpl(BookingRepositoryFacade bookingRepo, 
                              ProductCatalogFacade catalogRepo,
                              PricingService pricingService,
                              ISeatLockService seatLockService,
                              IBookingNotificationService notificationService,
                              BookingMapper bookingMapper,
                              StringRedisTemplate redisTemplate,
                              ObjectMapper objectMapper) {
        this.bookingRepo = bookingRepo;
        this.catalogRepo = catalogRepo;
        this.pricingService = pricingService;
        this.seatLockService = seatLockService;
        this.notificationService = notificationService;
        this.bookingMapper = bookingMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override public void holdSeat(Long showtimeId, Long seatId, String sessionId) { seatLockService.holdSeat(showtimeId, seatId, sessionId); }
    @Override public void releaseSeat(Long showtimeId, Long seatId, String sessionId) { seatLockService.releaseSeat(showtimeId, seatId, sessionId); }
    @Override public List<Long> getMyLockedSeats(Long showtimeId, String sessionId) { return seatLockService.getMyLockedSeats(showtimeId, sessionId); }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating temporary booking in Redis: {}", request.getEmail());
        Showtime showtime = getAndValidateShowtime(request.getShowtimeId());
        validateSeatsAvailability(showtime.getId(), request.getSeatIds());

        String bookingCode = AppConstants.BOOKING_CODE_PREFIX + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BigDecimal total = calculateEstimatedPrice(request, showtime);

        BookingResponse res = new BookingResponse();
        res.setBookingCode(bookingCode); res.setTotalPrice(total);
        res.setMovieTitle(showtime.getMovie().getTitle()); res.setRoomName(showtime.getRoom().getName());
        res.setShowTime(showtime.getStartTime()); res.setStatus(BookingStatus.PENDING);
        res.setPaymentMethod(request.getPaymentMethod());

        try {
            Map<String, Object> redisData = new HashMap<>();
            redisData.put("request", request); redisData.put("response", res);
            redisTemplate.opsForValue().set(PENDING_BOOKING_PREFIX + bookingCode, objectMapper.writeValueAsString(redisData), HOLD_DURATION);
        } catch (Exception e) { throw new AppException("Lỗi lưu dữ liệu tạm"); }
        return res;
    }

    @Override
    @Transactional
    public BookingResponse createPOSBooking(BookingRequest request) {
        log.info("Creating POS booking: {}", request.getPhone());
        Showtime showtime = getAndValidateShowtime(request.getShowtimeId());
        validateSeatsAvailability(showtime.getId(), request.getSeatIds());

        String bookingCode = AppConstants.POS_CODE_PREFIX + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Customer customer = findOrCreateCustomer(request);

        Booking booking = buildBookingBase(bookingCode, showtime, customer, request);
        Booking savedBooking = bookingRepo.save(booking);

        savePayment(savedBooking, booking.getTotalPrice(), request.getPaymentMethod(), AppConstants.POS_CODE_PREFIX + System.currentTimeMillis());
        seatLockService.clearLocks(showtime.getId(), request.getSeatIds());
        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponse finalizeBooking(String bookingCode, String transactionId) {
        log.info("Finalizing booking: {}", bookingCode);
        String json = redisTemplate.opsForValue().get(PENDING_BOOKING_PREFIX + bookingCode);
        if (json == null) return bookingRepo.findByCode(bookingCode).map(bookingMapper::toResponse).orElseThrow(() -> new AppException("Yêu cầu quá hạn."));

        BookingRequest request;
        try { request = objectMapper.convertValue(objectMapper.readValue(json, Map.class).get("request"), BookingRequest.class); }
        catch (Exception e) { throw new AppException("Lỗi đọc dữ liệu tạm"); }

        Showtime showtime = getAndValidateShowtime(request.getShowtimeId());
        Customer customer = findOrCreateCustomer(request);

        Booking booking = buildBookingBase(bookingCode, showtime, customer, request);
        Booking savedBooking = bookingRepo.save(booking);

        savePayment(savedBooking, booking.getTotalPrice(), request.getPaymentMethod(), transactionId != null ? transactionId : AppConstants.TXN_CODE_PREFIX + System.currentTimeMillis());
        notificationService.sendBookingConfirmation(savedBooking);
        redisTemplate.delete(PENDING_BOOKING_PREFIX + bookingCode);
        seatLockService.clearLocks(showtime.getId(), request.getSeatIds());
        return bookingMapper.toResponse(savedBooking);
    }

    private Booking buildBookingBase(String code, Showtime showtime, Customer customer, BookingRequest request) {
        Booking b = new Booking(); b.setBookingCode(code); b.setShowtime(showtime); b.setCustomer(customer); b.setStatus(BookingStatus.CONFIRMED);
        List<BookingDetail> details = processSeatPricing(b, showtime, request.getSeatIds(), customer);
        List<BookingCombo> combos = processComboPricing(b, request.getCombos());
        BigDecimal subtotal = details.stream().map(BookingDetail::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(combos.stream().map(bc -> bc.getPrice().multiply(BigDecimal.valueOf(bc.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add));
        b.setTotalPrice(applyPromotion(b, customer, request.getPromotionCode(), subtotal));
        b.setDetails(details); b.setCombos(combos);
        return b;
    }

    private void savePayment(Booking b, BigDecimal amount, PaymentMethod method, String txId) {
        Payment p = new Payment(); p.setBooking(b); p.setAmount(amount); p.setPaymentMethod(method);
        p.setPaymentStatus(PaymentStatus.SUCCESS); p.setTransactionId(txId); p.setPaidAt(LocalDateTime.now());
        bookingRepo.savePayment(p); b.setPayment(p);
    }

    private BigDecimal calculateEstimatedPrice(BookingRequest request, Showtime showtime) {
        BigDecimal total = BigDecimal.ZERO;
        Customer tempCust = null; try { tempCust = getCurrentCustomer(); } catch (Exception e) {}
        for (Long sId : request.getSeatIds()) {
            Seat s = catalogRepo.findSeat(sId).orElseThrow();
            total = total.add(pricingService.calculateTicketPrice(showtime, s, tempCust).getFinalPrice());
        }
        if (request.getCombos() != null) {
            Branch branch = showtime.getRoom().getBranch();
            for (Map.Entry<String, Integer> entry : request.getCombos().entrySet()) {
                Combo c = catalogRepo.findCombo(Long.parseLong(entry.getKey())).orElseThrow();
                BranchCombo bc = catalogRepo.findBranchCombo(branch, c).orElseThrow();
                total = total.add(bc.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
            }
        }
        return total;
    }

    private Customer findOrCreateCustomer(BookingRequest request) {
        try { return getCurrentCustomer(); } catch (Exception e) {}
        String phone = (request.getPhone() != null && !request.getPhone().trim().isEmpty()) ? request.getPhone().trim() : null;
        if (phone != null) {
            Optional<Customer> existing = bookingRepo.findCustomerByPhone(phone);
            if (existing.isPresent()) return existing.get();
        }
        Customer c = new Customer();
        c.setFullName(request.getFullName() != null ? request.getFullName().trim() : "Khách vãng lai");
        c.setPhone(phone); c.setEmail(request.getEmail());
        return bookingRepo.saveCustomer(c);
    }

    private Showtime getAndValidateShowtime(Long id) {
        Showtime s = catalogRepo.findShowtime(id).orElseThrow(() -> new AppException("Showtime not found"));
        if (s.getStatus() == ShowtimeStatus.CANCELLED) throw new AppException("Suất chiếu đã bị hủy");
        return s;
    }

    private void validateSeatsAvailability(Long showtimeId, List<Long> seatIds) {
        List<Long> booked = bookingRepo.findBookedSeatIds(showtimeId);
        for (Long id : seatIds) if (booked.contains(id)) throw new AppException("Ghế " + id + " đã được bán");
    }

    private List<BookingDetail> processSeatPricing(Booking booking, Showtime showtime, List<Long> seatIds, Customer customer) {
        List<BookingDetail> details = new ArrayList<>();
        for (Long id : seatIds) {
            Seat seat = catalogRepo.findSeat(id).orElseThrow();
            BookingDetail d = new BookingDetail(); d.setBooking(booking); d.setSeat(seat); d.setSeatCode(seat.getSeatCode());
            d.setPrice(pricingService.calculateTicketPrice(showtime, seat, customer).getFinalPrice());
            details.add(d);
        }
        return details;
    }

    private List<BookingCombo> processComboPricing(Booking booking, Map<String, Integer> comboRequests) {
        List<BookingCombo> list = new ArrayList<>();
        if (comboRequests != null) {
            Branch branch = booking.getShowtime().getRoom().getBranch();
            for (Map.Entry<String, Integer> entry : comboRequests.entrySet()) {
                Combo c = catalogRepo.findCombo(Long.parseLong(entry.getKey())).orElseThrow();
                BranchCombo bc_item = catalogRepo.findBranchCombo(branch, c).orElseThrow();
                if (bc_item.getStockQuantity() < entry.getValue()) throw new AppException("Combo " + c.getName() + " đã hết hàng");
                bc_item.setStockQuantity(bc_item.getStockQuantity() - entry.getValue());
                catalogRepo.saveBranchCombo(bc_item);
                BookingCombo bc = new BookingCombo(); bc.setBooking(booking); bc.setCombo(c); bc.setQuantity(entry.getValue()); bc.setPrice(bc_item.getPrice());
                list.add(bc);
            }
        }
        return list;
    }

    private BigDecimal applyPromotion(Booking booking, Customer customer, String code, BigDecimal current) {
        if (code == null || code.isEmpty()) return current;
        Promotion p = catalogRepo.findPromotion(code).orElseThrow(() -> new AppException("Mã không hợp lệ"));
        booking.setPromotion(p);
        BigDecimal disc = (p.getDiscountType() == DiscountType.FIXED) ? p.getDiscountValue() : current.multiply(p.getDiscountValue()).divide(BigDecimal.valueOf(100));
        return current.subtract(disc);
    }

    @Override public List<BookingResponse> getMyBookings() { return bookingRepo.findByCustomerId(getCurrentCustomer().getId()).stream().map(bookingMapper::toResponse).collect(Collectors.toList()); }
    @Override public List<BookingResponse> getAllBookings() { return bookingRepo.findAll().stream().map(bookingMapper::toResponse).collect(Collectors.toList()); }
    @Override
    @Transactional(readOnly = true)
    public BookingResponse getMyBookingByCode(String code) {
        Optional<Booking> dbBooking = bookingRepo.findByCode(code);
        if (dbBooking.isPresent()) {
            return bookingMapper.toResponse(dbBooking.get());
        }

        // Nếu không có trong DB, thử tìm trong Redis (cho online booking đang chờ)
        String json = redisTemplate.opsForValue().get(PENDING_BOOKING_PREFIX + code);
        if (json != null) {
            try {
                return objectMapper.convertValue(objectMapper.readValue(json, Map.class).get("response"), BookingResponse.class);
            } catch (Exception e) {
                log.error("Redis error when fetching pending booking: {}", e.getMessage());
            }
        }
        throw new AppException("Mã đặt vé không tồn tại hoặc đã hết hạn.", 404);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse lookupBooking(String code) {
        return getMyBookingByCode(code); // Re-use the same logic
    }

    @Override @Transactional public void cancelBooking(String code) { Booking b = bookingRepo.findByCode(code).orElseThrow(() -> new AppException("Mã đặt vé không tồn tại", 404)); b.setStatus(BookingStatus.CANCELLED); bookingRepo.save(b); }
    @Override @Transactional public void checkInBooking(String code) { Booking b = bookingRepo.findByCode(code).orElseThrow(() -> new AppException("Mã đặt vé không tồn tại", 404)); b.setStatus(BookingStatus.CHECKED_IN); bookingRepo.save(b); }

    private Customer getCurrentCustomer() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) throw new AppException("Unauthorized");
        String username = ((UserDetails) principal).getUsername();
        return bookingRepo.findCustomerByUsername(username)
                .orElseThrow(() -> new AppException("Không tìm thấy thông tin khách hàng liên kết với tài khoản này."));
    }
}
