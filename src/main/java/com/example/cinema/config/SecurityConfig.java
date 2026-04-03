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
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/movies", "/api/movies/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/showtimes", "/api/showtimes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/combos", "/api/combos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/promotions", "/api/promotions/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments/webhook").permitAll()
                        .requestMatchers("/static/**", "/", "/*.html", "/favicon.ico", "/error", "/payment/**",
                                "/js/**", "/css/**", "/images/**")
                        .permitAll()

                        // TMDB API (Dành cho Manager/Admin tìm phim)
                        .requestMatchers("/api/tmdb/**").hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")

                        // Manager / admin features
                        .requestMatchers(HttpMethod.POST, "/api/movies").hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/movies/**").hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/movies/**").hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")
                        .requestMatchers("/api/statistics", "/api/statistics/**").hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")
                        .requestMatchers("/api/manager/**").hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")

                        // Staff / admin features
                        .requestMatchers("/api/staff/**").hasAnyAuthority("ROLE_STAFF", "ROLE_ADMIN")

                        // Customer / staff booking flow
                        .requestMatchers(HttpMethod.POST, "/api/bookings")
                        .hasAnyAuthority("ROLE_CUSTOMER", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/bookings/{code}")
                        .hasAnyAuthority("ROLE_CUSTOMER", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/bookings/me").hasAuthority("ROLE_CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/*/cancel").hasAuthority("ROLE_CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/*/checkin").hasAuthority("ROLE_STAFF")

                        // Customer membership / payment / notification flow
                        .requestMatchers("/api/customers/me", "/api/customers/me/**").hasAuthority("ROLE_CUSTOMER")
                        .requestMatchers("/api/notifications/**").hasAuthority("ROLE_CUSTOMER")
                        .requestMatchers("/api/payments/me", "/api/payments/*").hasAuthority("ROLE_CUSTOMER")

                        // Admin only
                        .requestMatchers("/api/users/**", "/api/admin/**").hasAuthority("ROLE_ADMIN")

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
