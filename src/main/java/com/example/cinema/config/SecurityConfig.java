package com.example.cinema.config;

import com.example.cinema.security.JwtFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                .csrf(AbstractHttpConfigurer::disable)
                .cors(org.springframework.security.config.Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // 1. PUBLIC ENDPOINTS
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/verify-otp", "/api/auth/reset-password", "/api/auth/forgot-password").permitAll()
                        .requestMatchers("/api/public/**", "/api/payments/**", "/ws-cinema/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/movies/**", "/api/showtimes/**", "/api/combos/**", "/api/promotions/**").permitAll()
                        .requestMatchers("/api/bookings/**").permitAll()
                        .requestMatchers("/static/**", "/", "/*.html", "/dashboard/**", "/favicon.ico", "/error", "/payment/**", "/js/**", "/css/**", "/images/**").permitAll()

                        // 2. SHARED MANAGER & ADMIN (Operation)
                        .requestMatchers("/api/tmdb/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers("/api/audit/**", "/api/statistics/**", "/api/scheduling/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        
                        // 3. MOVIE & COMBO CATALOG (Admin edits, Manager updates priority)
                        .requestMatchers(HttpMethod.POST, "/api/movies", "/api/combos").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/movies/**", "/api/combos/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/movies/*/priority").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/movies/**", "/api/combos/**").hasAuthority("ROLE_ADMIN")
                        
                        // 4. BRANCH OPS (Showtimes, Rooms, Promotions, Branch Movie Priority)
                        .requestMatchers("/api/showtimes/**", "/api/rooms/**", "/api/promotions/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/movies/branch/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/combos/branch/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")

                        // 5. STAFF & POS FLOW
                        .requestMatchers("/api/staff/**").hasAnyAuthority("ROLE_STAFF", "ROLE_MANAGER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/bookings/hold-seat", "/api/bookings/release-seat").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/bookings/my-locked-seats").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/bookings/{code}").permitAll() // Cho phép public để khách vãng lai check trạng thái thanh toán
                        .requestMatchers(HttpMethod.GET, "/api/bookings/me").hasAuthority("ROLE_CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/*/cancel").hasAuthority("ROLE_CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/*/checkin").hasAnyAuthority("ROLE_STAFF", "ROLE_MANAGER", "ROLE_ADMIN")

                        // 6. ADMIN ONLY (System & Users)
                        .requestMatchers("/api/admin/staffs/assignments").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers("/api/admin/branch-movies/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers("/api/admin/branches/**", "/api/admin/branches", "/api/admin/pricing/**", "/api/admin/pricing").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers("/api/admin/pricing-rules/**", "/api/admin/pricing-rules").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers("/api/users/**").hasAuthority("ROLE_ADMIN")

                        .anyRequest().authenticated());
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

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
