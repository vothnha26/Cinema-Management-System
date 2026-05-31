package com.example.cinema.service.booking.impl;

import com.example.cinema.model.dto.response.PaymentCheckoutResponse;
import com.example.cinema.model.enums.PaymentMethod;
import com.example.cinema.model.enums.PaymentStatus;
import com.example.cinema.service.booking.IPaymentStrategy;
import com.example.cinema.service.infrastructure.vietqr.IVietQRService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class VietQRPaymentStrategy implements IPaymentStrategy {
    private final IVietQRService vietQRService;
    private static final Pattern BOOKING_CODE_PATTERN = Pattern.compile("BKG-?[A-Z0-9]+");

    public VietQRPaymentStrategy(IVietQRService vietQRService) {
        this.vietQRService = vietQRService;
    }

    @Override public PaymentMethod getSupportedMethod() { return PaymentMethod.BANK_TRANSFER; }

    @Override
    public PaymentCheckoutResponse generateCheckout(String bookingCode, BigDecimal amount) {
        PaymentCheckoutResponse res = new PaymentCheckoutResponse();
        res.setBookingCode(bookingCode);
        res.setAmount(amount);
        res.setPaymentMethod(getSupportedMethod());
        res.setPaymentStatus(PaymentStatus.PENDING);
        
        String qrUrl = vietQRService.generateCheckoutUrl(bookingCode, amount, "Thanh toan ve StarCinema " + bookingCode);
        res.setQrCodeUrl(qrUrl);
        res.setCheckoutUrl(qrUrl);
        res.setMessage("Quét mã QR để thanh toán.");
        return res;
    }

    @Override
    public String extractBookingCode(Map<String, Object> payload) {
        String content = Objects.toString(payload.get("content"), "");
        Matcher matcher = BOOKING_CODE_PATTERN.matcher(content.toUpperCase());
        if (matcher.find()) {
            String code = matcher.group();
            return code.contains("-") ? code : "BKG-" + code.substring(3);
        }
        
        List<String> fields = List.of("bookingCode", "addInfo", "description");
        for (String field : fields) {
            String val = Objects.toString(payload.get(field), "");
            if (val.isEmpty()) continue;
            matcher = BOOKING_CODE_PATTERN.matcher(val.toUpperCase());
            if (matcher.find()) {
                String code = matcher.group();
                return code.contains("-") ? code : "BKG-" + code.substring(3);
            }
        }
        return null;
    }

    @Override
    public String extractTransactionId(Map<String, Object> payload) {
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
}
