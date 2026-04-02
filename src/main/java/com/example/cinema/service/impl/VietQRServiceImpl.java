package com.example.cinema.service.impl;

import com.example.cinema.config.VietQRProperties;
import com.example.cinema.service.VietQRService;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class VietQRServiceImpl implements VietQRService {

    private final VietQRProperties properties;

    public VietQRServiceImpl(VietQRProperties properties) {
        this.properties = properties;
    }

    @Override
    public String generateCheckoutUrl(String bookingCode, BigDecimal amount, String description) {
        try {
            String encodedDescription = URLEncoder.encode(
                    bookingCode + " " + (description != null ? description : "Thanh toan ve xem phim"),
                    StandardCharsets.UTF_8.toString());
            String encodedAccountName = URLEncoder.encode(
                    properties.getAccountName(),
                    StandardCharsets.UTF_8.toString());

            return "https://img.vietqr.io/image/"
                    + properties.getBankBin()
                    + "-"
                    + properties.getBankNumber()
                    + "-compact2.png?amount="
                    + amount.longValue()
                    + "&addInfo="
                    + encodedDescription
                    + "&accountName="
                    + encodedAccountName;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Could not generate VietQR checkout URL", e);
        }
    }
}
