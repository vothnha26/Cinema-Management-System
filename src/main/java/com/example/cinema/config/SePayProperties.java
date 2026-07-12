package com.example.cinema.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SePayProperties {
    @Value("${sepay.secret:}")
    private String secret;

    @Value("${sepay.callback-path:/api/public/payments/webhook}")
    private String callbackPath;

    @Value("${sepay.bank-account:109879673245}")
    private String bankAccount;

    @Value("${sepay.bank-bin:970415}")
    private String bankBin;

    @Value("${sepay.bank-name:VietinBank}")
    private String bankName;

    @Value("${sepay.qr-template:compact2}")
    private String qrTemplate;

    @Value("${sepay.prefix:SEPAY }")
    private String prefix;

    @Value("${sepay.base-url:http://localhost:8082}")
    private String baseUrl;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getCallbackPath() {
        return callbackPath;
    }

    public void setCallbackPath(String callbackPath) {
        this.callbackPath = callbackPath;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getBankBin() {
        return bankBin;
    }

    public void setBankBin(String bankBin) {
        this.bankBin = bankBin;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getQrTemplate() {
        return qrTemplate;
    }

    public void setQrTemplate(String qrTemplate) {
        this.qrTemplate = qrTemplate;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
