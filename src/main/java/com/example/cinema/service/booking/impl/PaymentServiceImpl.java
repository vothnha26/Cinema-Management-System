package com.example.cinema.service.booking.impl;

import com.example.cinema.config.SePayProperties;
import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.response.PaymentCheckoutResponse;
import com.example.cinema.model.dto.response.PaymentResponse;
import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.BookingStatus;
import com.example.cinema.model.enums.NotificationType;
import com.example.cinema.model.enums.PaymentStatus;
import com.example.cinema.repository.booking.BookingRepository;
import com.example.cinema.repository.booking.PaymentRepository;
import com.example.cinema.repository.notification.NotificationRepository;
import com.example.cinema.repository.user.CustomerRepository;
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

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              BookingRepository bookingRepository,
                              CustomerRepository customerRepository,
                              NotificationRepository notificationRepository,
                              CustomerService customerService,
                              IVietQRService vietQRService,
                              SePayProperties sePayProperties,
                              StringRedisTemplate redisTemplate,
                              SimpMessagingTemplate messagingTemplate) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.notificationRepository = notificationRepository;
        this.customerService = customerService;
        this.vietQRService = vietQRService;
        this.sePayProperties = sePayProperties;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
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
        return mapToResponse(getOwnedPayment(bookingCode));
    }

    @Override
    @Transactional
    public PaymentCheckoutResponse createCheckout(String bookingCode) {
        Payment payment = getOwnedPayment(bookingCode);

        PaymentCheckoutResponse response = new PaymentCheckoutResponse();
        response.setBookingCode(payment.getBooking().getBookingCode());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPaymentStatus(payment.getPaymentStatus());

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            response.setMessage("Thanh toan cho booking nay da hoan tat.");
            return response;
        }

        if (payment.getPaymentStatus() == PaymentStatus.FAILED || payment.getBooking().getStatus() == BookingStatus.CANCELLED) {
            throw new AppException("Booking nay khong con o trang thai cho thanh toan");
        }

        String qrCodeUrl = vietQRService.generateCheckoutUrl(
                payment.getBooking().getBookingCode(),
                payment.getAmount(),
                "Thanh toan ve xem phim");
        response.setQrCodeUrl(qrCodeUrl);
        response.setCheckoutUrl(qrCodeUrl);
        response.setReturnUrl(sePayProperties.getBaseUrl() + "/payment/return?bookingCode=" + payment.getBooking().getBookingCode());
        response.setMessage("Quet ma QR de hoan tat thanh toan. He thong se tu cap nhat khi nhan duoc callback tu cong thanh toan.");
        return response;
    }

    @Override
    @Transactional
    public PaymentResponse confirmMyPayment(String bookingCode) {
        Payment payment = paymentRepository.findByBookingBookingCode(bookingCode)
                .orElseThrow(() -> new AppException("Payment not found"));

        // Nếu khách hàng đã đăng nhập, kiểm tra quyền sở hữu
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            Customer customer = customerRepository.findByUserUsername(userDetails.getUsername())
                    .orElseThrow(() -> new AppException("Customer not found"));

            if (payment.getBooking().getCustomer() != null &&
                !payment.getBooking().getCustomer().getId().equals(customer.getId())) {
                throw new AppException("Unauthorized");
            }
        }

        return mapToResponse(confirmPaymentInternal(payment, null, false));
    }

    @Override
    @Transactional
    public PaymentResponse failMyPayment(String bookingCode) {
        Payment payment = getOwnedPayment(bookingCode);

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new AppException("Giao dich nay da thanh toan thanh cong, khong the danh dau that bai");
        }

        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setPaidAt(null);
        payment.setTransactionId(null);

        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        createNotification(booking.getCustomer().getUser(),
                "Thanh toan that bai",
                "Booking " + booking.getBookingCode() + " da duoc huy do thanh toan khong thanh cong.",
                NotificationType.SYSTEM);

        return mapToResponse(payment);
    }

    @Override
    @Transactional
    public void handleWebhook(Map<String, Object> payload, String signatureHeader) {
        log.info("--- [Service] Start processing payment webhook ---");
        
        // 1. Kiểm tra bảo mật (nếu có signature)
        if (signatureHeader != null) {
            log.info("Checking webhook signature...");
            validateWebhookSignature(signatureHeader);
        }

        // 2. Trích xuất mã đặt vé từ nội dung chuyển khoản (content)
        String bookingCode = extractBookingCode(payload);
        log.info("Extracted Booking Code: {}", bookingCode);
        
        if (bookingCode == null) {
            log.warn("SePay Webhook: No booking code found in content '{}'. Payload: {}", 
                payload.get("content"), payload);
            return; 
        }

        Payment payment = paymentRepository.findByBookingBookingCode(bookingCode)
                .orElseThrow(() -> new AppException("Payment not found for code: " + bookingCode));
        log.info("Found corresponding payment in DB. Total required: {}", payment.getAmount());

        // 3. Kiểm tra loại giao dịch (Chỉ xử lý tiền vào 'in')
        String transferType = Objects.toString(payload.get("transferType"), "").toLowerCase();
        log.info("Transaction Type: {}", transferType);
        
        if (!"in".equals(transferType)) {
            log.info("SePay Webhook: Ignored non-income transaction type: {}", transferType);
            return;
        }

        // 4. Kiểm tra số tiền (Phải chuyển đủ hoặc thừa mới xác nhận)
        Object amountObj = payload.get("transferAmount");
        double receivedAmount = amountObj != null ? Double.parseDouble(amountObj.toString()) : 0;
        double requiredAmount = payment.getAmount().doubleValue();
        log.info("Received Amount: {}, Required Amount: {}", receivedAmount, requiredAmount);

        if (receivedAmount < requiredAmount) {
            log.warn("SePay Webhook: UNDERPAYMENT for {}. Required: {}, Received: {}", 
                bookingCode, requiredAmount, receivedAmount);
            return;
        }

        // 5. Xác nhận thanh toán thành công
        String transactionId = Objects.toString(payload.get("id"), "SP-" + System.currentTimeMillis());
        log.info("Confirming payment with Transaction ID: {}", transactionId);
        
        confirmPaymentInternal(payment, transactionId, true);
        log.info("--- [Service] Webhook processed SUCCESSFULLY for booking {} ---", bookingCode);
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
                .orElseThrow(() -> new AppException("Payment not found"));

        Customer bookingCustomer = payment.getBooking().getCustomer();
        
        // Nếu booking này thuộc về một Hội viên (có user_id)
        if (bookingCustomer != null && bookingCustomer.getUser() != null) {
            Customer currentCustomer = getCurrentCustomer();
            if (!bookingCustomer.getId().equals(currentCustomer.getId())) {
                throw new AppException("Unauthorized: This booking belongs to another member");
            }
        }
        // Nếu là khách vãng lai (không có user_id), cho phép truy cập qua bookingCode (đã pass qua URL)
        
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
            if (booking.getCustomer().getUser() != null) {
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
