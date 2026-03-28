# 📦 Cấu trúc Source Code – Cinema Management System

## 1. Cấu trúc thư mục

```
d:\Fullit\projects\NNLTTT\Final\CINEMA_MANAGEMENT_SYSTEM\
│
├── src/
│   ├── main/
│   │   ├── java/com/example/cinema/
│   │   │   │
│   │   │   ├── config/                    # Cấu hình ứng dụng
│   │   │   │   ├── SecurityConfig.java    # Spring Security, CORS, JWT filter
│   │   │   │   ├── JwtConfig.java         # JWT secret, expiration
│   │   │   │   └── AppConfig.java         # ModelMapper bean, etc.
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── entity/                # JPA Entities (ánh xạ bảng DB)
│   │   │   │   │   ├── Movie.java
│   │   │   │   │   ├── Genre.java
│   │   │   │   │   ├── Director.java
│   │   │   │   │   ├── Actor.java
│   │   │   │   │   ├── Room.java
│   │   │   │   │   ├── Seat.java
│   │   │   │   │   ├── SeatPrice.java
│   │   │   │   │   ├── Showtime.java
│   │   │   │   │   ├── Booking.java
│   │   │   │   │   ├── BookingDetail.java
│   │   │   │   │   ├── BookingCombo.java
│   │   │   │   │   ├── Combo.java
│   │   │   │   │   ├── Payment.java
│   │   │   │   │   ├── Promotion.java
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Customer.java
│   │   │   │   │   └── Notification.java
│   │   │   │   │
│   │   │   │   ├── dto/                   # Request / Response DTOs
│   │   │   │   │   ├── request/
│   │   │   │   │   │   ├── BookingRequest.java
│   │   │   │   │   │   ├── ShowtimeRequest.java
│   │   │   │   │   │   ├── MovieRequest.java
│   │   │   │   │   │   └── LoginRequest.java
│   │   │   │   │   └── response/
│   │   │   │   │       ├── BookingResponse.java
│   │   │   │   │       ├── MovieResponse.java
│   │   │   │   │       ├── SeatMapResponse.java
│   │   │   │   │       └── ApiResponse.java   # Wrapper chung
│   │   │   │   │
│   │   │   │   └── enums/                 # Enum types
│   │   │   │       ├── Role.java
│   │   │   │       ├── BookingStatus.java
│   │   │   │       ├── ShowtimeStatus.java
│   │   │   │       ├── SeatType.java
│   │   │   │       ├── RoomType.java
│   │   │   │       ├── MembershipTier.java
│   │   │   │       └── PaymentMethod.java
│   │   │   │
│   │   │   ├── repository/                # Spring Data JPA Repositories
│   │   │   │   ├── MovieRepository.java
│   │   │   │   ├── ShowtimeRepository.java
│   │   │   │   ├── BookingRepository.java
│   │   │   │   ├── BookingDetailRepository.java
│   │   │   │   ├── SeatRepository.java
│   │   │   │   ├── SeatPriceRepository.java
│   │   │   │   ├── CustomerRepository.java
│   │   │   │   ├── PromotionRepository.java
│   │   │   │   ├── PaymentRepository.java
│   │   │   │   └── NotificationRepository.java
│   │   │   │
│   │   │   ├── service/                   # Service layer
│   │   │   │   ├── MovieService.java      # Interface
│   │   │   │   ├── ShowtimeService.java
│   │   │   │   ├── BookingService.java
│   │   │   │   ├── CustomerService.java
│   │   │   │   ├── PaymentService.java
│   │   │   │   ├── PromotionService.java
│   │   │   │   ├── SeatPriceService.java
│   │   │   │   ├── NotificationService.java
│   │   │   │   ├── StatisticsService.java
│   │   │   │   └── impl/                  # Implementations
│   │   │   │       ├── MovieServiceImpl.java
│   │   │   │       ├── BookingServiceImpl.java
│   │   │   │       └── ...
│   │   │   │
│   │   │   ├── controller/                # REST Controllers
│   │   │   │   ├── MovieController.java
│   │   │   │   ├── ShowtimeController.java
│   │   │   │   ├── BookingController.java
│   │   │   │   ├── ComboController.java
│   │   │   │   ├── CustomerController.java
│   │   │   │   ├── PromotionController.java
│   │   │   │   ├── NotificationController.java
│   │   │   │   ├── StatisticsController.java
│   │   │   │   └── AuthController.java
│   │   │   │
│   │   │   ├── security/                  # Spring Security
│   │   │   │   ├── JwtUtil.java           # Generate / validate JWT
│   │   │   │   ├── JwtFilter.java         # OncePerRequestFilter
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   │
│   │   │   ├── exception/                 # Exception handling
│   │   │   │   ├── AppException.java      # Custom runtime exception
│   │   │   │   └── GlobalExceptionHandler.java # @ControllerAdvice
│   │   │   │
│   │   │   ├── util/                      # Helper/Utility
│   │   │   │   ├── BookingCodeGenerator.java
│   │   │   │   └── DateTimeUtil.java
│   │   │   │
│   │   │   └── CinemaApplication.java     # @SpringBootApplication
│   │   │
│   │   └── resources/
│   │       ├── application.properties     # Cấu hình DB, JWT, server
│   │       ├── application-dev.properties
│   │       └── static/                    # Frontend files
│   │           ├── index.html
│   │           ├── css/
│   │           │   └── style.css
│   │           ├── js/
│   │           │   ├── api.js             # fetch wrapper, base URL
│   │           │   ├── auth.js            # Login, lưu token
│   │           │   ├── movies.js          # Trang danh sách phim
│   │           │   ├── booking.js         # Trang đặt vé, sơ đồ ghế
│   │           │   └── dashboard.js       # Thống kê Chart.js
│   │           └── pages/
│   │               ├── movies.html
│   │               ├── booking.html
│   │               ├── admin.html
│   │               └── dashboard.html
│   │
│   └── test/
│       └── java/com/example/cinema/
│           ├── service/                   # Unit tests
│           │   ├── BookingServiceTest.java
│           │   └── CustomerServiceTest.java
│           └── controller/                # Integration tests
│               └── BookingControllerTest.java
│
├── pom.xml
├── GUIDE.md
└── docs/                                  # Tài liệu dự án (thư mục này)
    ├── 01_overview.md
    ├── 02_architecture.md
    └── ...
```

---

## 2. Naming Conventions

| Thành phần | Convention | Ví dụ |
|-----------|-----------|-------|
| Entity | PascalCase | `Movie`, `BookingDetail` |
| Repository | `{Entity}Repository` | `MovieRepository` |
| Service Interface | `{Entity}Service` | `BookingService` |
| Service Impl | `{Entity}ServiceImpl` | `BookingServiceImpl` |
| Controller | `{Entity}Controller` | `MovieController` |
| DTO Request | `{Action}Request` | `BookingRequest` |
| DTO Response | `{Entity}Response` | `BookingResponse` |
| Enum | PascalCase | `BookingStatus`, `SeatType` |
| DB Tables | snake_case | `booking_details`, `seat_prices` |
| DB Columns | snake_case | `total_price`, `is_active` |
| API Endpoints | kebab-case | `/api/showtimes/{id}/seats` |

---

## 3. ApiResponse Wrapper (chuẩn hóa response)

```java
@Data @Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private int code;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
            .success(true).code(200).data(data).build();
    }

    public static <T> ApiResponse<T> error(String message, int code) {
        return ApiResponse.<T>builder()
            .success(false).message(message).code(code).build();
    }
}
```
