package com.example.cinema.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VietQRProperties {

    @Value("${vietqr.bank-bin:970422}")
    private String bankBin;

    @Value("${vietqr.bank-number:1900123456789}")
    private String bankNumber;

    @Value("${vietqr.account-name:STAR CINEMA}")
    private String accountName;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public String getBankBin() {
        return bankBin;
    }

    public String getBankNumber() {
        return bankNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
