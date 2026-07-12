package com.example.cinema.service.infrastructure.vietqr;

import java.math.BigDecimal;

public interface IVietQRService {
    String generateCheckoutUrl(String bookingCode, BigDecimal amount, String description);
}
