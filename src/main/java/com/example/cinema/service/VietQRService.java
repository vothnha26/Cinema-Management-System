package com.example.cinema.service;

import java.math.BigDecimal;

public interface VietQRService {
    String generateCheckoutUrl(String bookingCode, BigDecimal amount, String description);
}
