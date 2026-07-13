package com.example.cinema;

import com.example.cinema.model.entity.User;
import com.example.cinema.model.enums.Role;
import com.example.cinema.repository.user.UserRepository;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class CinemaApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        
        SpringApplication.run(CinemaApplication.class, args);
    }

    @Bean
    public org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Tự động sửa lỗi dòng migration bị hỏng (failed migration) trong flyway_schema_history
            flyway.repair();
            // Tiếp tục migrate
            flyway.migrate();
        };
    }

    @Bean
    @org.springframework.core.annotation.Order(1)
    public CommandLineRunner initDefaultUsers(
            UserRepository userRepository, 
            com.example.cinema.repository.user.CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Tạo tài khoản Admin
            User admin = userRepository.findByUsername("admin@starcinema.com")
                    .or(() -> userRepository.findByEmail("admin@starcinema.com"))
                    .orElse(new User());
            admin.setUsername("admin@starcinema.com");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setEmail("admin@starcinema.com");
            admin.setRole(Role.ADMIN);
            admin.setStatus(true);
            if (admin.getId() == null) {
                admin.setCreatedAt(LocalDateTime.now());
            }
            userRepository.save(admin);
            System.out.println(">>> ADMIN USER READY: admin@starcinema.com / 123456");

            // 2. Tạo tài khoản Customer
            User customerUser = userRepository.findByUsername("user@starcinema.com")
                    .or(() -> userRepository.findByEmail("user@starcinema.com"))
                    .orElse(new User());
            if (customerUser.getId() == null) {
                customerUser.setUsername("user@starcinema.com");
                customerUser.setPassword(passwordEncoder.encode("123456"));
                customerUser.setEmail("user@starcinema.com");
                customerUser.setRole(Role.CUSTOMER);
                customerUser.setStatus(true);
                customerUser.setCreatedAt(LocalDateTime.now());
                userRepository.save(customerUser);

                com.example.cinema.model.entity.Customer customerEntity = new com.example.cinema.model.entity.Customer();
                customerEntity.setUser(customerUser);
                customerEntity.setFullName("Star Cinema Customer");
                customerEntity.setEmail("user@starcinema.com");
                customerEntity.setPoints(0);
                customerEntity.setTotalSpending(java.math.BigDecimal.ZERO);
                customerRepository.save(customerEntity);
                System.out.println(">>> CUSTOMER USER READY: user@starcinema.com / 123456");
            } else if (!"user@starcinema.com".equals(customerUser.getUsername())) {
                customerUser.setUsername("user@starcinema.com");
                userRepository.save(customerUser);
                System.out.println(">>> CUSTOMER USER UPDATED TO GMAIL USERNAME: user@starcinema.com / 123456");
            } else {
                System.out.println(">>> CUSTOMER USER ALREADY EXISTS AND READY: user@starcinema.com / 123456");
            }
        };
    }
}
