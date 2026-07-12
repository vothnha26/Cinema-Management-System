package com.example.cinema.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.lang.NonNull;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // Kích hoạt một đơn giản memory-based message broker để gửi tin nhắn tới client
        config.enableSimpleBroker("/topic");
        // Tiền tố cho các tin nhắn từ client gửi tới server thông qua @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Đăng ký endpoint để client kết nối tới WebSocket
        registry.addEndpoint("/ws-cinema")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Hỗ trợ fallback cho các trình duyệt không hỗ trợ WebSocket
    }
}
