package com.example.cinema.service.booking.impl;

import com.example.cinema.config.SePayProperties;
import com.example.cinema.exception.AppException;
import com.example.cinema.model.dto.response.BookingResponse;
import com.example.cinema.model.dto.response.PaymentCheckoutResponse;
import com.example.cinema.model.dto.response.PaymentResponse;
import com.example.cinema.model.entity.*;
import com.example.cinema.model.enums.BookingStatus;
import com.example.cinema.model.enums.PaymentMethod;
import com.example.cinema.model.enums.PaymentStatus;
import com.example.cinema.repository.booking.BookingRepository;
import com.example.cinema.repository.booking.PaymentRepository;
import com.example.cinema.repository.user.CustomerRepository;
import com.example.cinema.service.booking.BookingService;
import com.example.cinema.service.booking.IPaymentStrategy;
import com.example.cinema.service.booking.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final SePayProperties sePayProperties;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final BookingService bookingService;
    private final ObjectMapper objectMapper;
    private final Map<PaymentMethod, IPaymentStrategy> paymentStrategies;

    private static final String PENDING_BOOKING_PREFIX = "pending_booking:";

    public PaymentServiceImpl(PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            CustomerRepository customerRepository,
            SePayProperties sePayProperties,
            StringRedisTemplate redisTemplate,
            SimpMessagingTemplate messagingTemplate,
            BookingService bookingService,
            ObjectMapper objectMapper,
            List<IPaymentStrategy> strategies) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.sePayProperties = sePayProperties;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.bookingService = bookingService;
        this.objectMapper = objectMapper;
        this.paymentStrategies = strategies.stream()
                .collect(Collectors.toMap(IPaymentStrategy::getSupportedMethod, s -> s));
    }

    @Override
    public List<PaymentResponse> getMyPayments() {
        return paymentRepository.findByBookingCustomerIdOrderByPaidAtDesc(getCurrentCustomer().getId())
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public PaymentResponse getMyPayment(String bookingCode) {
        Optional<Payment> p = paymentRepository.findByBookingBookingCode(bookingCode);
        if (p.isPresent())
            return mapToResponse(p.get());

        String json = redisTemplate.opsForValue().get(PENDING_BOOKING_PREFIX + bookingCode);
        if (json != null) {
            try {
                BookingResponse bResp = objectMapper
                        .convertValue(objectMapper.readValue(json, Map.class).get("response"), BookingResponse.class);
                PaymentResponse resp = new PaymentResponse();
                resp.setBookingCode(bResp.getBookingCode());
                resp.setAmount(bResp.getTotalPrice());
                resp.setMovieTitle(bResp.getMovieTitle());
                resp.setPaymentStatus(PaymentStatus.PENDING);
                resp.setPaymentMethod(bResp.getPaymentMethod());
                return resp;
            } catch (Exception e) {
                log.error("Redis read error: {}", e.getMessage());
            }
        }
        throw new AppException("Không tìm thấy thông tin thanh toán");
    }

    @Override
    @Transactional
    public PaymentCheckoutResponse createCheckout(String bookingCode) {
        // 1. Check DB
        Optional<Payment> existing = paymentRepository.findByBookingBookingCode(bookingCode);
        if (existing.isPresent()) {
            Payment p = existing.get();
            if (p.getPaymentStatus() == PaymentStatus.SUCCESS) {
                PaymentCheckoutResponse res = new PaymentCheckoutResponse();
                res.setBookingCode(bookingCode);
                res.setPaymentStatus(PaymentStatus.SUCCESS);
                res.setMessage("Thanh toán thành công.");
                return res;
            }
            return getStrategy(p.getPaymentMethod()).generateCheckout(bookingCode, p.getAmount());
        }

        // 2. Check Redis
        String json = redisTemplate.opsForValue().get(PENDING_BOOKING_PREFIX + bookingCode);
        if (json == null)
            throw new AppException("Đơn hàng không tồn tại hoặc đã quá hạn.");

        try {
            BookingResponse bResp = objectMapper.convertValue(objectMapper.readValue(json, Map.class).get("response"),
                    BookingResponse.class);
            return getStrategy(bResp.getPaymentMethod()).generateCheckout(bookingCode, bResp.getTotalPrice());
        } catch (Exception e) {
            throw new AppException("Lỗi xử lý đơn hàng.");
        }
    }

    @Override
    @Transactional
    public PaymentResponse confirmMyPayment(String bookingCode) {
        bookingService.finalizeBooking(bookingCode, "MANUAL-" + System.currentTimeMillis());
        return mapToResponse(paymentRepository.findByBookingBookingCode(bookingCode).orElseThrow());
    }

    @Override
    @Transactional
    public void handleWebhook(Map<String, Object> payload, String signatureHeader) {
        if (signatureHeader != null)
            validateWebhookSignature(signatureHeader);

        // Hiện tại SePay/VietQR là phương thức duy nhất hỗ trợ Webhook tự động
        IPaymentStrategy strategy = paymentStrategies.get(PaymentMethod.BANK_TRANSFER);
        String bookingCode = strategy.extractBookingCode(payload);
        if (bookingCode == null)
            return;

        log.info("Webhook success for booking: {}", bookingCode);
        bookingService.finalizeBooking(bookingCode, strategy.extractTransactionId(payload));

        // Thông báo cho Client qua WebSocket
        messagingTemplate.convertAndSend("/topic/payment/" + bookingCode, Map.of("status", "SUCCESS"));
    }

    @Override
    @Transactional
    public PaymentResponse failMyPayment(String bookingCode) {
        redisTemplate.delete(PENDING_BOOKING_PREFIX + bookingCode);
        Optional<Payment> pOpt = paymentRepository.findByBookingBookingCode(bookingCode);
        if (pOpt.isPresent()) {
            Payment p = pOpt.get();
            p.setPaymentStatus(PaymentStatus.FAILED);
            p.getBooking().setStatus(BookingStatus.CANCELLED);
            paymentRepository.save(p);
            return mapToResponse(p);
        }
        PaymentResponse resp = new PaymentResponse();
        resp.setBookingCode(bookingCode);
        resp.setPaymentStatus(PaymentStatus.FAILED);
        return resp;
    }

    private IPaymentStrategy getStrategy(PaymentMethod method) {
        IPaymentStrategy strategy = paymentStrategies.get(method);
        if (strategy == null)
            throw new AppException("Phương thức thanh toán " + method + " chưa được hỗ trợ.");
        return strategy;
    }

    private Customer getCurrentCustomer() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails userDetails))
            throw new AppException("Unauthorized");
        return customerRepository.findByUserUsername(userDetails.getUsername()).orElseThrow();
    }

    private PaymentResponse mapToResponse(Payment p) {
        PaymentResponse r = new PaymentResponse();
        r.setId(p.getId());
        r.setBookingCode(p.getBooking().getBookingCode());
        r.setMovieTitle(p.getBooking().getShowtime().getMovie().getTitle());
        r.setAmount(p.getAmount());
        r.setPaymentMethod(p.getPaymentMethod());
        r.setPaymentStatus(p.getPaymentStatus());
        r.setTransactionId(p.getTransactionId());
        r.setPaidAt(p.getPaidAt());
        return r;
    }

    private void validateWebhookSignature(String signatureHeader) {
        String expected = sePayProperties.getSecret();
        if (expected == null || expected.isBlank())
            return;
        if (!expected.equals(signatureHeader) && !signatureHeader.equals("Apikey " + expected)) {
            throw new AppException("Invalid payment webhook signature");
        }
    }
}
