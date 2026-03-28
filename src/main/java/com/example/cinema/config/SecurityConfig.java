package com.example.cinema.config;

import com.example.cinema.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // PUBLIC endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/movies/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/showtimes/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/combos/**").permitAll()
                .requestMatchers("/static/**", "/", "/*.html", "/favicon.ico").permitAll()

                // CUSTOMER + STAFF
                .requestMatchers(HttpMethod.POST, "/api/bookings").hasAnyRole("CUSTOMER", "STAFF")
                .requestMatchers(HttpMethod.GET, "/api/bookings/{code}").hasAnyRole("CUSTOMER", "STAFF")

                // STAFF only
                .requestMatchers(HttpMethod.PUT, "/api/bookings/*/checkin").hasRole("STAFF")

                // MANAGER + ADMIN
                .requestMatchers("/api/movies/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/api/rooms/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/api/statistics/**").hasAnyRole("MANAGER", "ADMIN")

                // ADMIN only
                .requestMatchers("/api/users/**").hasRole("ADMIN")

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
