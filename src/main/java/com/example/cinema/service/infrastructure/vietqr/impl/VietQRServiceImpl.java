package com.example.cinema.service.infrastructure.vietqr.impl;

import com.example.cinema.service.infrastructure.vietqr.IVietQRService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class VietQRServiceImpl implements IVietQRService {

    private final com.example.cinema.config.SePayProperties sePayProperties;

    public VietQRServiceImpl(com.example.cinema.config.SePayProperties sePayProperties) {
        this.sePayProperties = sePayProperties;
    }

    @Override
    public String generateCheckoutUrl(String bookingCode, BigDecimal amount, String description) {
        // Sử dụng Prefix từ cấu hình (mặc định là SEPAY ) kết hợp với mã đặt vé
        String fullContent = sePayProperties.getPrefix() + bookingCode;
        
        // Cấu trúc URL QR chính thức của SePay:
        // https://qr.sepay.vn/img?bank=<BANK_BIN>&acc=<ACCOUNT_NO>&template=<TEMPLATE>&amount=<AMOUNT>&des=<DESCRIPTION>
        return String.format("https://qr.sepay.vn/img?bank=%s&acc=%s&template=%s&amount=%s&des=%s",
                sePayProperties.getBankBin(),
                sePayProperties.getBankAccount(),
                sePayProperties.getQrTemplate(),
                amount.toPlainString(),
                URLEncoder.encode(fullContent, StandardCharsets.UTF_8));
    }
}
