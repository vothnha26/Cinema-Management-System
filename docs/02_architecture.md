# 🏗️ Kiến trúc hệ thống – Cinema Management System

## 1. Kiểu kiến trúc

**Monolithic Application** – Một ứng dụng Spring Boot duy nhất, backend cung cấp REST API JSON, frontend là HTML/CSS/JS tĩnh tích hợp trong cùng project.

---

## 2. Pattern áp dụng

| Pattern | Mô tả áp dụng |
|---------|--------------|
| **MVC (Model-View-Controller)** | Controller nhận request → Service xử lý → View (JSON response) |
| **Layered Architecture (3 tầng)** | Presentation → Business Logic → Data Access |
| **Repository Pattern** | Spring Data JPA Repository tách biệt data access |
| **DTO Pattern** | Tách Entity nội bộ khỏi dữ liệu truyền vào/ra |
| **Strategy Pattern** | `DiscountStrategy`: mở rộng loại ưu đãi mà không sửa code cũ |
| **Dependency Injection** | Spring IoC Container quản lý toàn bộ bean |

---

## 3. Sơ đồ kiến trúc tổng thể

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT SIDE                          │
│   Browser (HTML/CSS/JS + Bootstrap 5)                       │
│   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│   │ Customer │  │  Staff   │  │ Manager  │  │  Admin   │  │
│   │  Pages   │  │  POS UI  │  │Dashboard │  │  Panel   │  │
│   └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  │
└────────┼─────────────┼─────────────┼──────────────┼────────┘
         │             │   HTTP/S (REST API)         │
         └─────────────┴─────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────┐
│                    SPRING BOOT APPLICATION                   │
│                                                             │
│  ┌────────────────────────────────────────────────────────┐ │
│  │              SECURITY LAYER (Spring Security + JWT)    │ │
│  │  JwtFilter → Authentication → Authorization by Role   │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                             │
│  ┌────────────────────────────────────────────────────────┐ │
│  │                PRESENTATION LAYER                      │ │
│  │  @RestController: MovieController, ShowtimeController  │ │
│  │  BookingController, ComboController, CustomerController│ │
│  │  StatisticsController, NotificationController          │ │
│  └────────────────────────────────────────────────────────┘ │
│                           │                                 │
│  ┌────────────────────────▼───────────────────────────────┐ │
│  │              BUSINESS LOGIC LAYER (Service)            │ │
│  │  MovieService, ShowtimeService, BookingService         │ │
│  │  CustomerService, PaymentService, PromotionService     │ │
│  │  NotificationService, StatisticsService                │ │
│  └────────────────────────────────────────────────────────┘ │
│                           │                                 │
│  ┌────────────────────────▼───────────────────────────────┐ │
│  │                DATA ACCESS LAYER (Repository)          │ │
│  │  Spring Data JPA / Hibernate ORM                       │ │
│  │  MovieRepo, ShowtimeRepo, BookingRepo, SeatRepo...     │ │
│  └────────────────────────────────────────────────────────┘ │
└───────────────────────────┬─────────────────────────────────┘
                            │ JDBC
┌───────────────────────────▼─────────────────────────────────┐
│                      MySQL 8 Database                        │
│            20 tables – Cinema Management Schema             │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. Các thành phần chính

### Frontend
- **Công nghệ**: HTML5, CSS3, JavaScript thuần, Bootstrap 5
- **Vị trí**: `src/main/resources/static/`
- **Đặc biệt**: Sơ đồ ghế render bằng JS grid (CSS Grid + JS DOM)
- **Giao tiếp API**: `fetch()` / `axios` gọi REST endpoint

### Backend
- **Framework**: Spring Boot 3.x
- **Entry point**: `CinemaApplication.java`
- **Package gốc**: `com.example.cinema`
- **Phân tầng**: Controller → Service (Interface + Impl) → Repository → Entity

### Database
- **MySQL 8**: 20 bảng, quan hệ FK, index trên các cột tìm kiếm thường xuyên
- **ORM**: Hibernate tự động tạo/update schema (hoặc dùng SQL script)

### External Services
| Service | Mục đích | Trạng thái |
|---------|---------|------------|
| Cloudinary | Lưu trữ & tối ưu hóa hình ảnh (Poster, Avatar) | Đã tích hợp |
| Email (JavaMailSender) | Gửi xác nhận booking | Tùy chọn |
| VNPay / MoMo | Thanh toán online | Mô phỏng (mock) |
| Chart.js | Biểu đồ thống kê doanh thu | Frontend library |


---

## 5. Phân quyền theo kiến trúc

```
PUBLIC (không cần đăng nhập):
  GET /api/movies, /api/showtimes, /api/combos

CUSTOMER + STAFF:
  POST /api/bookings
  GET  /api/bookings/{code}

STAFF only:
  PUT  /api/bookings/{code}/checkin

MANAGER + ADMIN:
  POST/PUT/DELETE /api/movies, /api/rooms, /api/showtimes
  GET /api/statistics/**

ADMIN only:
  /api/users/** (CRUD tài khoản)
```
