package com.example.cinema.service.booking;

import com.example.cinema.model.dto.response.PaymentCheckoutResponse;
import com.example.cinema.model.enums.PaymentMethod;
import java.math.BigDecimal;
import java.util.Map;

public interface IPaymentStrategy {
    PaymentMethod getSupportedMethod();
    PaymentCheckoutResponse generateCheckout(String bookingCode, BigDecimal amount);
    String extractBookingCode(Map<String, Object> payload);
    String extractTransactionId(Map<String, Object> payload);
}
