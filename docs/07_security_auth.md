# 🔐 Security & Authentication – Cinema Management System

## 1. Phương thức xác thực: JWT (Stateless)

```
Client          Backend
  │   POST /api/auth/login   │
  │──────────────────────────►│
  │   {username, password}    │ → Validate → UserDetailsService
  │                           │ → BCrypt verify password
  │◄──────────────────────────│
  │   { token: "eyJ..." }     │ JWT (signed, expires: 24h)
  │                           │
  │   GET /api/bookings       │
  │   Authorization: Bearer eyJ... │
  │──────────────────────────►│
  │                           │ JwtFilter: parse token → set SecurityContext
  │◄──────────────────────────│
  │   { data: [...] }         │
```

---

## 2. JWT Structure

```
Header: { "alg": "HS256", "typ": "JWT" }
Payload: {
  "sub": "username",
  "role": "CUSTOMER",
  "userId": 42,
  "exp": 1711234567
}
Signature: HMAC-SHA256(base64(header) + "." + base64(payload), secret)
```

**Secret key** lưu trong `application.properties`:
```properties
jwt.secret=your-ultra-secret-key-min-256-bits
jwt.expiration=86400000  # 24h in ms
```

---

## 3. Spring Security Config

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // PUBLIC endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(GET, "/api/movies/**").permitAll()
                .requestMatchers(GET, "/api/showtimes/**").permitAll()
                .requestMatchers(GET, "/api/combos/**").permitAll()
                .requestMatchers("/static/**", "/", "/*.html").permitAll()

                // CUSTOMER + STAFF
                .requestMatchers(POST, "/api/bookings").hasAnyRole("CUSTOMER","STAFF")
                .requestMatchers(GET, "/api/bookings/{code}").hasAnyRole("CUSTOMER","STAFF")

                // STAFF only
                .requestMatchers(PUT, "/api/bookings/*/checkin").hasRole("STAFF")

                // MANAGER + ADMIN
                .requestMatchers("/api/movies/**").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers("/api/rooms/**").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers("/api/statistics/**").hasAnyRole("MANAGER","ADMIN")

                // ADMIN only
                .requestMatchers("/api/users/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);  // strength = 12
    }
}
```

---

## 4. JwtFilter (OncePerRequestFilter)

```java
@Component
public class JwtFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(request, response, filterChain) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                    );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

---

## 5. Password Security

- Mật khẩu được hash bằng **BCrypt** (strength 12) trước khi lưu DB.
- Không bao giờ trả password trong response.
- DTO `UserResponse` exclude field `password`.

---

## 6. CORS Config

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:8080"));
    config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    // ... register source
}
```

---

## 7. Phân quyền tóm tắt

| Endpoint | PUBLIC | CUSTOMER | STAFF | MANAGER | ADMIN |
|----------|:------:|:--------:|:-----:|:-------:|:-----:|
| GET /api/movies | ✅ | ✅ | ✅ | ✅ | ✅ |
| POST /api/bookings | ❌ | ✅ | ✅ | ❌ | ✅ |
| PUT /api/bookings/*/checkin | ❌ | ❌ | ✅ | ❌ | ✅ |
| POST /api/movies | ❌ | ❌ | ❌ | ✅ | ✅ |
| GET /api/statistics/** | ❌ | ❌ | ❌ | ✅ | ✅ |
| /api/users/** | ❌ | ❌ | ❌ | ❌ | ✅ |
