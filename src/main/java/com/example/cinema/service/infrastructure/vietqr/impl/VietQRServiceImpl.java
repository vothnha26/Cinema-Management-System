package com.example.cinema.service.infrastructure.vietqr.impl;

import com.example.cinema.service.infrastructure.vietqr.IVietQRService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class VietQRServiceImpl implements IVietQRService {

    // Thông tin ngân hàng của rạp (Giả định cho môi trường demo)
    private static final String BANK_ID = "MB"; // Ngân hàng Quân Đội
    private static final String ACCOUNT_NO = "123456789"; // Số tài khoản demo
    private static final String TEMPLATE = "compact";

    @Override
    public String generateCheckoutUrl(String bookingCode, BigDecimal amount, String description) {
        String encodedDescription = URLEncoder.encode(description + " " + bookingCode, StandardCharsets.UTF_8);
        
        // Cấu trúc URL VietQR: https://img.vietqr.io/image/<BANK_ID>-<ACCOUNT_NO>-<TEMPLATE>.png
        return String.format("https://img.vietqr.io/image/%s-%s-%s.png?amount=%s&addInfo=%s",
                BANK_ID, ACCOUNT_NO, TEMPLATE, amount.toPlainString(), encodedDescription);
    }
}
