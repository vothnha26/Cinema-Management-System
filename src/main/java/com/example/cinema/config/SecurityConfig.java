package com.example.cinema.config;

import com.example.cinema.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/**
 * Cấu hình Spring Security:
 * - Stateless (JWT), tắt CSRF (vì dùng token, không dùng cookie session).
 * - Phân quyền URL theo Role: ADMIN, STAFF, MANAGER, CUSTOMER.
 * - Public: trang tĩnh HTML/JS/CSS, API auth, API public (phim, suất chiếu).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
                corsConfiguration.setAllowedOrigins(List.of("*"));
                corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                corsConfiguration.setAllowedHeaders(List.of("*"));
                return corsConfiguration;
            }))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // --- Public: Tài nguyên tĩnh và trang HTML ---
                .requestMatchers("/", "/*.html", "/js/**", "/css/**", "/images/**", "/favicon.ico").permitAll()

                // --- Public: API xác thực ---
                .requestMatchers("/api/auth/**").permitAll()

                // --- Public: API đọc dữ liệu phim, suất chiếu (cho customer xem) ---
                .requestMatchers("/api/movies/**", "/api/showtimes/**", "/api/genres/**", "/api/public/**", "/api/combos/**").permitAll()

                // --- Admin only ---
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // --- Staff + Admin ---
                .requestMatchers("/api/staff/**").hasAnyRole("STAFF", "ADMIN")

                // --- Manager + Admin ---
                .requestMatchers("/api/manager/**").hasAnyRole("MANAGER", "ADMIN")

                // --- Tất cả API khác cần đăng nhập ---
                .anyRequest().authenticated()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

