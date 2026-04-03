package com.example.cinema.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * TestDotenvInitializer: Nạp các biến môi trường từ .env vào System Properties
 * trước khi Application Context của Test khởi chạy.
 */
public class TestDotenvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    @Override
    public void initialize(@org.springframework.lang.NonNull ConfigurableApplicationContext applicationContext) {
        Dotenv dotenv = Dotenv.configure()
                .directory(".") // Root project
                .ignoreIfMissing()
                .load();
        
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
        
        System.out.println(">>> [TEST] .env variables loaded successfully.");
    }
}
