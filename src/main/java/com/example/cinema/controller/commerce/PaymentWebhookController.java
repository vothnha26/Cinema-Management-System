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
        
        // Dùng System.out để chắc chắn hiện lên console dù log level thế nào
        System.out.println("================================================");
        System.out.println(">>> SEPAY WEBHOOK INCOMING!");
        System.out.println(">>> Auth Header: " + auth);
        System.out.println(">>> Content: " + payload.get("content"));
        System.out.println(">>> Amount: " + payload.get("transferAmount"));
        System.out.println("================================================");

        log.info("Headers - Authorization: {}", auth);
        log.info("Body Payload: {}", payload);

        // 1. KIỂM TRA BẢO MẬT (API KEY)
        String secret = sePayProperties.getSecret();
        if (secret != null && !secret.isEmpty()) {
            String receivedToken = auth != null ? auth.replace("Apikey ", "").replace("apikey ", "").trim() : "";
            String expectedToken = secret.trim();

            if (!receivedToken.equals(expectedToken)) {
                System.err.println("!!! UNAUTHORIZED WEBHOOK !!!");
                System.err.println(">>> Received (trimmed): " + (receivedToken.length() > 4 ? receivedToken.substring(0, 4) + "***" : "invalid"));
                System.err.println(">>> Expected (trimmed): " + (expectedToken.length() > 4 ? expectedToken.substring(0, 4) + "***" : "invalid"));
                return ResponseEntity.status(401).body("Invalid API Key");
            }
        }

        // 2. GỌI SERVICE XỬ LÝ
        try {
            paymentService.handleWebhook(payload, null);
            System.out.println(">>> WEBHOOK PROCESSED SUCCESSFULLY!");
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.err.println("!!! ERROR PROCESSING WEBHOOK: " + e.getMessage());
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
