package com.example.cinema.controller;

import com.example.cinema.model.dto.response.PaymentCheckoutResponse;
import com.example.cinema.model.dto.response.PaymentResponse;
import com.example.cinema.service.booking.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<PaymentResponse>> getMyPayments() {
        return ResponseEntity.ok(paymentService.getMyPayments());
    }

    @GetMapping("/{bookingCode}")
    public ResponseEntity<PaymentResponse> getMyPayment(@PathVariable String bookingCode) {
        return ResponseEntity.ok(paymentService.getMyPayment(bookingCode));
    }

    @PostMapping("/{bookingCode}/checkout")
    public ResponseEntity<PaymentCheckoutResponse> createCheckout(@PathVariable String bookingCode) {
        return ResponseEntity.ok(paymentService.createCheckout(bookingCode));
    }

    @PostMapping("/{bookingCode}/confirm")
    public ResponseEntity<PaymentResponse> confirmMyPayment(@PathVariable String bookingCode) {
        return ResponseEntity.ok(paymentService.confirmMyPayment(bookingCode));
    }

    @PostMapping("/{bookingCode}/fail")
    public ResponseEntity<PaymentResponse> failMyPayment(@PathVariable String bookingCode) {
        return ResponseEntity.ok(paymentService.failMyPayment(bookingCode));
    }
}
