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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Pattern BOOKING_CODE_PATTERN = Pattern.compile("BKG-[A-Z0-9]{8}");

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final NotificationRepository notificationRepository;
    private final CustomerService customerService;
    private final IVietQRService vietQRService;
    private final SePayProperties sePayProperties;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              BookingRepository bookingRepository,
                              CustomerRepository customerRepository,
                              NotificationRepository notificationRepository,
                              CustomerService customerService,
                              IVietQRService vietQRService,
                              SePayProperties sePayProperties) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.notificationRepository = notificationRepository;
        this.customerService = customerService;
        this.vietQRService = vietQRService;
        this.sePayProperties = sePayProperties;
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
        validateWebhookSignature(signatureHeader);

        String bookingCode = extractBookingCode(payload);
        if (bookingCode == null) {
            throw new AppException("Booking code not found in payment payload");
        }

        Payment payment = paymentRepository.findByBookingBookingCode(bookingCode)
                .orElseThrow(() -> new AppException("Payment not found"));

        if ("FAILED".equals(normalizeStatus(payload))) {
            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                return;
            }
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setTransactionId(null);
            payment.setPaidAt(null);
            payment.getBooking().setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(payment.getBooking());
            createNotification(payment.getBooking().getCustomer().getUser(),
                    "Thanh toan that bai",
                    "Booking " + bookingCode + " thanh toan that bai tu cong thanh toan.",
                    NotificationType.SYSTEM);
            return;
        }

        confirmPaymentInternal(payment, extractTransactionId(payload), true);
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
        Customer customer = getCurrentCustomer();
        Payment payment = paymentRepository.findByBookingBookingCode(bookingCode)
                .orElseThrow(() -> new AppException("Payment not found"));

        if (!payment.getBooking().getCustomer().getId().equals(customer.getId())) {
            throw new AppException("Unauthorized");
        }

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

        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

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
        List<String> candidates = List.of(
                Objects.toString(payload.get("bookingCode"), ""),
                Objects.toString(payload.get("orderCode"), ""),
                Objects.toString(payload.get("order_code"), ""),
                Objects.toString(payload.get("orderId"), ""),
                Objects.toString(payload.get("content"), ""),
                Objects.toString(payload.get("description"), ""),
                Objects.toString(payload.get("addInfo"), ""),
                Objects.toString(payload.get("transferContent"), ""),
                Objects.toString(payload.get("transaction_content"), ""));

        for (String candidate : candidates) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            Matcher matcher = BOOKING_CODE_PATTERN.matcher(candidate.toUpperCase());
            if (matcher.find()) {
                return matcher.group();
            }
        }
        return null;
    }
}
