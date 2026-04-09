package com.example.cinema.service.booking.impl;

import com.example.cinema.config.SePayProperties;
import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.response.BookingResponse;
import com.example.cinema.model.dto.response.PaymentCheckoutResponse;
import com.example.cinema.model.dto.response.PaymentResponse;
import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.BookingStatus;
import com.example.cinema.model.enums.NotificationType;
import com.example.cinema.model.enums.PaymentMethod;
import com.example.cinema.model.enums.PaymentStatus;
import com.example.cinema.repository.booking.BookingRepository;
import com.example.cinema.repository.booking.PaymentRepository;
import com.example.cinema.repository.notification.NotificationRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.service.booking.BookingService;
import com.example.cinema.service.booking.PaymentService;
import com.example.cinema.service.infrastructure.vietqr.IVietQRService;
import com.example.cinema.service.user.CustomerService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentServiceImpl.class);
    private static final Pattern BOOKING_CODE_PATTERN = Pattern.compile("BKG-?[A-Z0-9]+");
    private static final String LOCK_KEY_PREFIX = "seat_lock:";

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final NotificationRepository notificationRepository;
    private final CustomerService customerService;
    private final IVietQRService vietQRService;
    private final SePayProperties sePayProperties;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private final BookingService bookingService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private static final String PENDING_BOOKING_PREFIX = "pending_booking:";

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              BookingRepository bookingRepository,
                              CustomerRepository customerRepository,
                              NotificationRepository notificationRepository,
                              CustomerService customerService,
                              IVietQRService vietQRService,
                              SePayProperties sePayProperties,
                              StringRedisTemplate redisTemplate,
                              SimpMessagingTemplate messagingTemplate,
                              BookingService bookingService,
                              com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.notificationRepository = notificationRepository;
        this.customerService = customerService;
        this.vietQRService = vietQRService;
        this.sePayProperties = sePayProperties;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.bookingService = bookingService;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<PaymentResponse> getMyPayments() {
        Customer customer = getCurrentCustomer();
        return paymentRepository.findByBookingCustomerIdOrderByPaidAtDesc(customer.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public PaymentResponse getMyPayment(String bookingCode) {
        // Thử tìm trong DB
        Optional<Payment> p = paymentRepository.findByBookingBookingCode(bookingCode);
        if (p.isPresent()) return mapToResponse(p.get());

        // Thử tìm trong Redis (cho khách đang thanh toán)
        String json = redisTemplate.opsForValue().get(PENDING_BOOKING_PREFIX + bookingCode);
        if (json != null) {
            try {
                Map<String, Object> redisData = objectMapper.readValue(json, Map.class);
                BookingResponse bResp = objectMapper.convertValue(redisData.get("response"), BookingResponse.class);
                
                PaymentResponse resp = new PaymentResponse();
                resp.setBookingCode(bResp.getBookingCode());
                resp.setAmount(bResp.getTotalPrice());
                resp.setMovieTitle(bResp.getMovieTitle());
                resp.setPaymentStatus(PaymentStatus.PENDING);
                resp.setPaymentMethod(bResp.getPaymentMethod());
                return resp;
            } catch (Exception e) { log.error("Redis read error: {}", e.getMessage()); }
        }
        
        throw new AppException("Không tìm thấy thông tin thanh toán");
    }

    @Override
    @Transactional
    public PaymentCheckoutResponse createCheckout(String bookingCode) {
        // 1. Thử tìm trong DB trước
        Optional<Payment> existingPayment = paymentRepository.findByBookingBookingCode(bookingCode);
        if (existingPayment.isPresent()) {
            Payment p = existingPayment.get();
            return mapToCheckoutResponse(p.getBooking().getBookingCode(), p.getAmount(), p.getPaymentMethod(), p.getPaymentStatus());
        }

        // 2. Thử tìm trong Redis
        String json = redisTemplate.opsForValue().get(PENDING_BOOKING_PREFIX + bookingCode);
        if (json == null) throw new AppException("Đơn hàng không tồn tại hoặc đã hết hạn.");

        try {
            Map<String, Object> redisData = objectMapper.readValue(json, Map.class);
            BookingResponse responseDto = objectMapper.convertValue(redisData.get("response"), BookingResponse.class);
            
            return mapToCheckoutResponse(responseDto.getBookingCode(), responseDto.getTotalPrice(), responseDto.getPaymentMethod(), PaymentStatus.PENDING);
        } catch (Exception e) {
            log.error("Error reading redis data for checkout: {}", e.getMessage());
            throw new AppException("Lỗi xử lý thông tin đơn hàng.");
        }
    }

    private PaymentCheckoutResponse mapToCheckoutResponse(String code, java.math.BigDecimal amount, PaymentMethod method, PaymentStatus status) {
        PaymentCheckoutResponse response = new PaymentCheckoutResponse();
        response.setBookingCode(code);
        response.setAmount(amount);
        response.setPaymentMethod(method);
        response.setPaymentStatus(status);

        if (status == PaymentStatus.SUCCESS) {
            response.setMessage("Thanh toán thành công.");
            return response;
        }

        String qrUrl = vietQRService.generateCheckoutUrl(code, amount, "Thanh toan ve StarCinema " + code);
        response.setQrCodeUrl(qrUrl);
        response.setCheckoutUrl(qrUrl);
        response.setMessage("Quét mã QR để thanh toán.");
        return response;
    }

    @Override
    @Transactional
    public PaymentResponse confirmMyPayment(String bookingCode) {
        log.info("Manually confirming payment for code: {}", bookingCode);
        BookingResponse finalized = bookingService.finalizeBooking(bookingCode, "MANUAL-" + System.currentTimeMillis());
        
        Payment p = paymentRepository.findByBookingBookingCode(bookingCode)
                .orElseThrow(() -> new AppException("Xác nhận thanh toán thất bại."));
        return mapToResponse(p);
    }

    @Override
    @Transactional
    public void handleWebhook(Map<String, Object> payload, String signatureHeader) {
        if (signatureHeader != null) validateWebhookSignature(signatureHeader);
        String bookingCode = extractBookingCode(payload);
        if (bookingCode == null) return;

        log.info("Webhook received SUCCESS for booking: {}", bookingCode);
        bookingService.finalizeBooking(bookingCode, extractTransactionId(payload));
    }

    @Override
    @Transactional
    public PaymentResponse failMyPayment(String bookingCode) {
        log.info("Marking payment as FAILED for booking: {}", bookingCode);
        
        // Xóa data tạm trong Redis nếu có
        redisTemplate.delete(PENDING_BOOKING_PREFIX + bookingCode);
        
        // Nếu đã có trong DB thì cập nhật trạng thái
        Optional<Payment> pOpt = paymentRepository.findByBookingBookingCode(bookingCode);
        if (pOpt.isPresent()) {
            Payment p = pOpt.get();
            p.setPaymentStatus(PaymentStatus.FAILED);
            p.getBooking().setStatus(BookingStatus.CANCELLED);
            paymentRepository.save(p);
            return mapToResponse(p);
        }
        
        // Nếu chưa có trong DB (khách vãng lai), trả về response giả lập hoặc báo lỗi
        PaymentResponse resp = new PaymentResponse();
        resp.setBookingCode(bookingCode);
        resp.setPaymentStatus(PaymentStatus.FAILED);
        return resp;
    }

    private Customer getCurrentCustomer() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) {
            throw new AppException("Unauthorized");
        }

        return customerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException("Customer not found"));
    }

    private Payment getOwnedPayment(String bookingCode) {
        Payment payment = paymentRepository.findByBookingBookingCode(bookingCode)
                .orElseThrow(() -> new AppException("Payment not found for code: " + bookingCode));

        Customer bookingCustomer = payment.getBooking().getCustomer();

        // TRƯỜNG HỢP 1: Booking thuộc về một Hội viên đã đăng ký tài khoản (User)
        if (bookingCustomer != null && bookingCustomer.getUser() != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            // Nếu là Hội viên, bắt buộc phải đăng nhập đúng tài khoản mới được xem
            if (principal instanceof UserDetails userDetails) {
                Customer currentCustomer = customerRepository.findByUserUsername(userDetails.getUsername())
                        .orElse(null);
                
                if (currentCustomer == null || !bookingCustomer.getId().equals(currentCustomer.getId())) {
                    throw new AppException("Giao dịch này thuộc về tài khoản khác. Vui lòng đăng nhập đúng tài khoản.");
                }
            } else {
                // Nếu chưa đăng nhập mà xem booking của Hội viên -> Yêu cầu đăng nhập
                throw new AppException("Giao dịch này thuộc về một Hội viên. Vui lòng đăng nhập để tiếp tục.");
            }
        }
        
        // TRƯỜNG HỢP 2: Booking của Khách vãng lai (GUEST)
        // Cho phép truy cập công khai qua bookingCode (vì mã này được sinh ngẫu nhiên khó đoán)
        
        return payment;
    }
    private Payment confirmPaymentInternal(Payment payment, String transactionId, boolean autoSource) {
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return payment;
        }
        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new AppException("Thanh toan nay da duoc hoan tien");
        }
        if (payment.getPaymentStatus() == PaymentStatus.FAILED || payment.getBooking().getStatus() == BookingStatus.CANCELLED) {
            throw new AppException("Booking nay khong con hop le de xac nhan thanh toan");
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(transactionId != null && !transactionId.isBlank()
                ? transactionId
                : "TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        payment.setPaidAt(LocalDateTime.now());
        
        // LƯU PAYMENT VÀO CSDL
        paymentRepository.save(payment);

        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Báo cho các client khác qua WebSocket về ghế
        if (booking.getShowtime() != null && booking.getDetails() != null) {
            Long showtimeId = booking.getShowtime().getId();
            for (BookingDetail detail : booking.getDetails()) {
                if (detail.getSeat() != null) {
                    broadcastSeatStatus(showtimeId, detail.getSeat().getId(), "BOOKED");
                }
            }
        }

        // THÊM: Báo cho trang payment.html của khách hàng này
        try {
            log.info("Broadcasting payment success to /topic/payment/{}", booking.getBookingCode());
            messagingTemplate.convertAndSend("/topic/payment/" + booking.getBookingCode(), Map.of("status", "SUCCESS"));
        } catch (Exception e) {
            log.error("Failed to broadcast payment success: {}", e.getMessage());
        }

        // Xử lý điểm thưởng và thông báo chỉ khi có Customer
        if (booking.getCustomer() != null) {
            customerService.addLoyaltyPoints(booking.getCustomer(), payment.getAmount());
            if (booking.getCustomer().getUser() != null && booking.getCustomer().getUser().getId() != null) {
                createNotification(booking.getCustomer().getUser(),
                        "Thanh toan thanh cong",
                        "Booking " + booking.getBookingCode() + " da duoc thanh toan thanh cong" + (autoSource ? " va xac nhan tu dong." : "."),
                        NotificationType.BOOKING);
            }
        }

        return payment;
    }

    private void broadcastSeatStatus(Long showtimeId, Long seatId, String action) {
        Map<String, Object> message = new HashMap<>();
        message.put("seatId", seatId);
        message.put("action", action);
        message.put("sessionId", "SYSTEM-PAYMENT");
        try {
            messagingTemplate.convertAndSend("/topic/showtime/" + showtimeId + "/seats", message);
        } catch (Exception e) {
            // Log error but don't break payment transaction
        }
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setBookingCode(payment.getBooking().getBookingCode());
        response.setMovieTitle(payment.getBooking().getShowtime().getMovie().getTitle());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setTransactionId(payment.getTransactionId());
        response.setPaidAt(payment.getPaidAt());
        return response;
    }

    private void createNotification(User user, String title, String message, NotificationType type) {
        if (user == null) return;
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notificationRepository.save(notification);
    }

    private void validateWebhookSignature(String signatureHeader) {
        String expected = sePayProperties.getSecret();
        if (expected == null || expected.isBlank()) {
            return;
        }
        if (signatureHeader == null || !expected.equals(signatureHeader)) {
            // Chấp nhận cả trường hợp Header Authorization kiểu 'Apikey ...'
            String expectedAuth = "Apikey " + expected;
            if (signatureHeader.equals(expectedAuth)) return;
            
            throw new AppException("Invalid payment webhook signature");
        }
    }

    private String normalizeStatus(Map<String, Object> payload) {
        List<String> values = new ArrayList<>();
        values.add(Objects.toString(payload.get("status"), ""));
        values.add(Objects.toString(payload.get("transaction_status"), ""));
        values.add(Objects.toString(payload.get("state"), ""));
        values.add(Objects.toString(payload.get("result"), ""));

        String joined = String.join(" ", values).toUpperCase();
        if (joined.contains("FAIL") || joined.contains("ERROR") || joined.contains("CANCEL")) {
            return "FAILED";
        }
        return "SUCCESS";
    }

    private String extractTransactionId(Map<String, Object> payload) {
        List<String> candidates = List.of(
                Objects.toString(payload.get("referenceCode"), ""),
                Objects.toString(payload.get("transaction_id"), ""),
                Objects.toString(payload.get("transactionId"), ""),
                Objects.toString(payload.get("id"), ""));

        return candidates.stream()
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }

    private String extractBookingCode(Map<String, Object> payload) {
        String content = Objects.toString(payload.get("content"), "");
        System.out.println(">>> Analyzing content for Booking Code: " + content);
        
        // Sử dụng Pattern đã định nghĩa (linh hoạt BKG-?...)
        Matcher matcher = BOOKING_CODE_PATTERN.matcher(content.toUpperCase());
        if (matcher.find()) {
            String code = matcher.group();
            // CHUẨN HÓA: Nếu mã bị dính liền (BKG123), thêm lại dấu gạch ngang (BKG-123) để khớp database
            if (!code.contains("-")) {
                code = "BKG-" + code.substring(3);
            }
            System.out.println(">>> FOUND AND NORMALIZED CODE: " + code);
            return code;
        }
        
        // Kiểm tra thêm các trường khác nếu có (addInfo, bookingCode...)
        List<String> fields = List.of("bookingCode", "addInfo", "description");
        for (String field : fields) {
            String val = Objects.toString(payload.get(field), "");
            if (val.isEmpty()) continue;
            matcher = BOOKING_CODE_PATTERN.matcher(val.toUpperCase());
            if (matcher.find()) {
                String code = matcher.group();
                if (!code.contains("-")) code = "BKG-" + code.substring(3);
                return code;
            }
        }
        return null;
    }
}
