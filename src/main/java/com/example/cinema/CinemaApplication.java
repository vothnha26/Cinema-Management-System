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
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            User admin = userRepository.findByUsername("admin").orElse(new User());
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setEmail("admin@starcinema.com");
            admin.setRole(Role.ADMIN);
            admin.setStatus(true);
            if (admin.getId() == null) {
                admin.setCreatedAt(LocalDateTime.now());
            }
            userRepository.save(admin);
            System.out.println(">>> ADMIN USER READY: admin / 123456");
        };
    }
}
