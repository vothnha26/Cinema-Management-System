package com.example.cinema.service;

import com.example.cinema.model.dto.response.PaymentCheckoutResponse;
import com.example.cinema.model.dto.response.PaymentResponse;

import java.util.List;
import java.util.Map;

public interface PaymentService {
    List<PaymentResponse> getMyPayments();
    PaymentResponse getMyPayment(String bookingCode);
    PaymentCheckoutResponse createCheckout(String bookingCode);
    PaymentResponse confirmMyPayment(String bookingCode);
    PaymentResponse failMyPayment(String bookingCode);
    void handleWebhook(Map<String, Object> payload, String signatureHeader);
}
