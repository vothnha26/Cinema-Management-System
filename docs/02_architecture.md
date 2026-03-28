# 🏗️ Kiến trúc hệ thống – Cinema Management System (Elite Version)

## 1. Kiểu kiến trúc

**Monolithic Application** – Một ứng dụng Spring Boot duy nhất, backend cung cấp REST API JSON, frontend là HTML/CSS/JS tĩnh tích hợp trong cùng project. Hệ thống được thiết kế theo hướng **Modulith** (Modular Monolith) để dễ dàng tách thành Microservices sau này.

---

## 2. Elite Patterns áp dụng (SOLID Compliance)

Hệ thống tuân thủ nghiêm ngặt nguyên tắc SOLID và áp dụng các mẫu thiết kế linh hoạt:

| Pattern | Vị trí áp dụng | Mục tiêu SOLID |
|---------|--------------|----------------|
| **Strategy Pattern** | `SeatLayoutStrategy` | **OCP**: Cho phép thêm thuật toán tạo ghế cho loại phòng mới (4DX, ScreenX) mà không sửa code cũ. |
| **Decorator Pattern** | `PriceCalculator` | **OCP**: Bọc thêm các lớp phụ phí (IMAX, VIP, Happy Hour, Weekend) vào giá vé một cách linh hoạt. |
| **Builder Pattern** | `Promotion`, `StatisticsResponse` | **SRP**: Tách biệt việc xây dựng các đối tượng phức tạp có nhiều thuộc tính tùy chọn. |
| **Facade Pattern** | `MovieServiceImpl` | **SRP**: Đóng vai trò bộ điều phối, gom nhóm các service chuyên trách (Media, Metadata). |
| **Aspect Oriented (AOP)** | `AuditLogAspect` | **SRP**: Tách biệt logic ghi nhật ký (Cross-cutting concern) ra khỏi logic nghiệp vụ chính. |
| **Factory Pattern** | `SeatLayoutFactory` | **DIP**: Quản lý việc khởi tạo các Strategy dựa trên loại thực thể. |

---

## 3. Sơ đồ kiến trúc tổng thể

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT SIDE                          │
│   Browser (HTML/CSS/JS + Bootstrap 5 + Chart.js)            │
│   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│   │ Customer │  │  Staff   │  │ Manager  │  │  Admin   │    │
│   │  Pages   │  │  POS UI  │  │Dashboard │  │  Panel   │    │
│   └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘    │
└────────┼─────────────┼─────────────┼─────────────┼──────────┘
         │             │   HTTP/S (REST API)       │
         └─────────────┴─────────────┴─────────────┴────────────┐
                                │                               │
┌───────────────────────────────▼───────────────────────────────┐
│                    SPRING BOOT APPLICATION                    │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │              SECURITY LAYER (Spring Security + JWT)     │  │
│  │  JwtFilter → Authentication → Authorization by Role     │  │
│  └─────────────────────────────────────────────────────────┘  │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │              ASPECT LAYER (Audit & Logging)             │  │
│  │  @AuditAction Interceptor → Async Database Logging      │  │
│  └─────────────────────────────────────────────────────────┘  │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │                PRESENTATION LAYER                       │  │
│  │  @RestController: Movie, Showtime, Scheduling, Pricing  │  │
│  │  Combo, Promotion, Statistics, AuditLog                 │  │
│  └─────────────────────────────────────────────────────────┘  │
│                           │                                   │
│  ┌────────────────────────▼────────────────────────────────┐  │
│  │              BUSINESS LOGIC LAYER (Service)             │  │
│  │  Elite Patterns: Strategy, Decorator, Builder, Facade   │  │
│  │  Logic: AI Scheduling, Conflict Detection, Price Engine │  │
│  └─────────────────────────────────────────────────────────┘  │
│                           │                                   │
│  ┌────────────────────────▼────────────────────────────────┐  │
│  │                DATA ACCESS LAYER (Repository)           │  │
│  │  Spring Data JPA / Hibernate ORM                        │  │
│  │  20+ Entity Mappings, Custom Native Queries             │  │
│  └─────────────────────────────────────────────────────────┘  │
└───────────────────────────────┬───────────────────────────────┘
                                │ JDBC
┌───────────────────────────────▼───────────────────────────────┐
│                      MySQL 8 Database                         │
│            Elite Cinema Schema (Normalized & Indexed)         │
└───────────────────────────────────────────────────────────────┘
```

---

## 4. Các thành phần chính

### Core Logic (Elite Implementation)
- **AI Scheduling Engine**: Tích hợp **TMDB API** để lấy chỉ số `popularity`. Thuật toán tự động phân bổ 70% giờ vàng cho phim Hot và đảm bảo dãn cách giờ bắt đầu giữa các phòng.
- **Advanced Pricing**: Hệ thống tính giá cộng dồn sử dụng Decorators (Base + Room + Seat + Day + Time).
- **Automated Audit**: Ghi lại mọi hành động CREATE/UPDATE/DELETE của Manager một cách tự động thông qua AOP.

### External Services
| Service | Mục đích | Trạng thái |
|---------|---------|------------|
| **TMDB API** | Lấy dữ liệu xu hướng phim (Buzz Score) toàn cầu | Đã tích hợp |
| **Cloudinary** | Lưu trữ & tối ưu hóa hình ảnh (Poster, Avatar) | Đã tích hợp |
| **Gmail SMTP** | Gửi thông báo và mật khẩu ứng dụng | Đã cấu hình |
| **Chart.js** | Hiển thị biểu đồ doanh thu thực tế trên Dashboard | Đã tích hợp |

---

## 5. Phân quyền API

```
PUBLIC:
  GET /api/movies, /api/showtimes, /api/combos, /api/actors/search

CUSTOMER + STAFF:
  POST /api/bookings, GET /api/bookings/{code}

MANAGER:
  Full CRUD: /api/movies, /api/rooms, /api/showtimes, /api/pricing
  AI: /api/scheduling/suggest, /api/scheduling/apply
  Stats: /api/statistics/overview
  Audit: /api/audit-logs (View only)

ADMIN:
  Full System Access + User Management (/api/users)
```
