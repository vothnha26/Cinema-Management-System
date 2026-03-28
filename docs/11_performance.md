# 📊 Performance & Tóm tắt – Cinema Management System

## 1. Performance (Cơ bản – phù hợp học thuật)

### 1.1. Tối ưu Database

```sql
-- Index trên cột tìm kiếm thường xuyên
CREATE INDEX idx_showtime_movie_date ON showtimes(movie_id, start_time);
CREATE INDEX idx_booking_detail_seat ON booking_details(seat_id, booking_id);
CREATE UNIQUE INDEX idx_booking_code ON bookings(booking_code);
CREATE INDEX idx_notification_user ON notifications(user_id, is_read);
CREATE INDEX idx_seat_price_lookup ON seat_prices(room_type, seat_type, is_active);
```

### 1.2. Tối ưu JPA / Hibernate

```java
// ❌ N+1 Problem – BAD
List<Showtime> showtimes = showtimeRepo.findAll();
showtimes.forEach(s -> s.getMovie().getTitle()); // N queries thêm!

// ✅ Join Fetch – GOOD
@Query("SELECT s FROM Showtime s JOIN FETCH s.movie WHERE s.status = :status")
List<Showtime> findByStatusWithMovie(ShowtimeStatus status);

// ✅ DTO Projection (chỉ lấy cột cần thiết)
@Query("SELECT new com.example.cinema.dto.ShowtimeSummary(s.id, s.startTime, m.title) ...")
```

### 1.3. Lazy Loading (mặc định tất cả quan hệ)

```java
@ManyToOne(fetch = FetchType.LAZY)  // Mặc định, chỉ load khi cần
@OneToMany(fetch = FetchType.LAZY)  // Không load toàn bộ list khi không dùng
```

### 1.4. Pagination

```java
// Không trả toàn bộ danh sách phim (có thể hàng nghìn bản ghi)
Page<Movie> findAll(Pageable pageable);

// Controller
@GetMapping("/api/movies")
public ApiResponse<?> getMovies(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {
    return ApiResponse.ok(movieService.findAll(PageRequest.of(page, size)));
}
```

---

## 2. Performance nâng cao (Ngoài phạm vi môn học – tham khảo)

| Giải pháp | Mục đích | Khi nào dùng |
|-----------|---------|-------------|
| **Cloudinary** | Tối ưu hóa kích thước ảnh, CDN phân phối | Luôn dùng cho Movie Posters, Avatars |
| **Redis Cache** | Cache danh sách phim, suất chiếu | Khi traffic lớn, dữ liệu ít thay đổi |

| **Connection Pool (HikariCP)** | Tái dụng DB connection | Spring Boot dùng mặc định |
| **CDN** | Phân phối poster/trailer | Khi cần tốc độ global |
| **Message Queue (Kafka/RabbitMQ)** | Gửi email/notification async | Khi không muốn block request |
| **Load Balancing** | Phân tải nhiều instance | Khi scale horizontal |

---

## 3. 🔥 Tóm lại – Nhìn nhanh toàn bộ project

```
┌─────────────────────────────────────────────────────┐
│              CINEMA MANAGEMENT SYSTEM               │
│                                                     │
│  Problem: Quản lý rạp phim end-to-end               │
│  Users: Customer, Staff, Manager, Admin             │
│                                                     │
│  ┌──────────────────────────────────────────────┐   │
│  │  FRONTEND: HTML/CSS/JS + Bootstrap 5         │   │
│  │  (Sơ đồ ghế JS Grid, Chart.js thống kê)     │   │
│  └─────────────────────┬────────────────────────┘   │
│                        │ REST API (JSON)             │
│  ┌─────────────────────▼────────────────────────┐   │
│  │  BACKEND: Spring Boot 3 (Java 17)            │   │
│  │  Security: Spring Security + JWT             │   │
│  │  Architecture: Layered (Controller→Service   │   │
│  │                →Repository→Entity)           │   │
│  │  SOLID: Đầy đủ 5 nguyên tắc                 │   │
│  └─────────────────────┬────────────────────────┘   │
│                        │ JPA/Hibernate              │
│  ┌─────────────────────▼────────────────────────┐   │
│  │  DATABASE: MySQL 8 – 20 bảng                 │   │
│  │  movies, showtimes, bookings, payments,      │   │
│  │  promotions, notifications, seat_prices...   │   │
│  └──────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

### Checklist "Đọc hiểu" 1 project nhanh

| # | Câu hỏi | Trả lời (Cinema) |
|---|---------|-----------------|
| 1 | Project giải quyết gì? | Quản lý rạp phim end-to-end |
| 2 | User là ai? | Customer, Staff, Manager, Admin |
| 3 | Kiến trúc? | Monolith, Layered MVC |
| 4 | Request đi đâu? | Controller → Service → Repository → DB |
| 5 | Logic ở đâu? | Tất cả trong Service layer |
| 6 | DB có bao nhiêu bảng? | 20 bảng, MySQL 8 |
| 7 | Auth thế nào? | Spring Security + JWT + BCrypt |
| 8 | Frontend gì? | HTML/JS thuần + Bootstrap 5 |
| 9 | Test ra sao? | JUnit 5 + Mockito (Unit), MockMvc (Integration) |
| 10 | Deploy thế nào? | JAR / Docker Compose |

---

## 4. Tài liệu liên quan

| File | Nội dung |
|------|---------|
| [01_overview.md](01_overview.md) | Tổng quan & Use Case |
| [02_architecture.md](02_architecture.md) | Kiến trúc hệ thống |
| [03_data_flow.md](03_data_flow.md) | Flow xử lý request |
| [04_business_logic.md](04_business_logic.md) | Logic nghiệp vụ |
| [05_database.md](05_database.md) | ERD & Schema 20 bảng |
| [06_code_structure.md](06_code_structure.md) | Cấu trúc source |
| [07_security_auth.md](07_security_auth.md) | Security & JWT |
| [08_tech_stack.md](08_tech_stack.md) | Tech stack & pom.xml |
| [09_testing.md](09_testing.md) | Testing strategy |
| [10_deployment.md](10_deployment.md) | Deploy & Docker |
| **11_performance.md** | **Performance & Tóm lại** |
| [GUIDE.md](../GUIDE.md) | Hướng dẫn dự án gốc |
