package com.example.cinema.controller.commerce;

import com.example.cinema.service.booking.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/payments")
public class PaymentWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);
    private final PaymentService paymentService;
    private final com.example.cinema.config.SePayProperties sePayProperties;

    public PaymentWebhookController(PaymentService paymentService,
                                   com.example.cinema.config.SePayProperties sePayProperties) {
        this.paymentService = paymentService;
        this.sePayProperties = sePayProperties;
    }

    // Endpoint này để bạn dán vào trình duyệt kiểm tra xem ngrok đã thông tới app chưa
    @GetMapping("/webhook")
    public String testWebhook() {
        return "StarCinema Webhook is ONLINE! Please use POST for SePay.";
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleSePayWebhook(@RequestBody Map<String, Object> payload, 
                                               @RequestHeader(value = "Authorization", required = false) String auth) {
        
        log.info("================================================");
        log.info(">>> SEPAY WEBHOOK INCOMING!");
        log.info(">>> Auth Header: {}", auth);
        log.info(">>> Content: {}", payload.get("content"));
        log.info(">>> Amount: {}", payload.get("transferAmount"));
        log.info("================================================");

        // 1. KIỂM TRA BẢO MẬT (API KEY)
        String secret = sePayProperties.getSecret();
        if (secret != null && !secret.isEmpty()) {
            String receivedToken = auth != null ? auth.replace("Apikey ", "").replace("apikey ", "").trim() : "";
            String expectedToken = secret.trim();

            if (!receivedToken.equals(expectedToken)) {
                log.warn("!!! UNAUTHORIZED WEBHOOK !!!");
                log.warn(">>> Received (trimmed): {}", (receivedToken.length() > 4 ? receivedToken.substring(0, 4) + "***" : "invalid"));
                log.warn(">>> Expected (trimmed): {}", (expectedToken.length() > 4 ? expectedToken.substring(0, 4) + "***" : "invalid"));
                return ResponseEntity.status(401).body("Invalid API Key");
            }
        }

        // 2. GỌI SERVICE XỬ LÝ
        try {
            paymentService.handleWebhook(payload, null);
            log.info(">>> WEBHOOK PROCESSED SUCCESSFULLY!");
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("!!! ERROR PROCESSING WEBHOOK: ", e);
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
