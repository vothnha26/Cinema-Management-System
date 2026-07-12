package com.example.cinema.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public/config")
public class PublicConfigController {

    @Value("${sepay.bank-name:VietinBank}")
    private String bankName;

    @Value("${sepay.bank-account:109879673245}")
    private String bankAccount;

    @Value("${VIETQR_BANK_BIN:970415}")
    private String bankBin;

    @Value("${sepay.prefix:SEPAY }")
    private String sepayPrefix;

    @GetMapping("/payment")
    public Map<String, String> getPaymentConfig() {
        return Map.of(
            "bankName", bankName,
            "bankAccount", bankAccount,
            "bankBin", bankBin,
            "prefix", sepayPrefix
        );
    }
}
