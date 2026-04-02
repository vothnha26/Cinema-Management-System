package com.example.cinema.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SePayProperties {

    @Value("${sepay.secret:}")
    private String secret;

    @Value("${sepay.callback-path:/api/payments/webhook}")
    private String callbackPath;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public String getSecret() {
        return secret;
    }

    public String getCallbackPath() {
        return callbackPath;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
