# 📦 Cấu trúc Source Code – Cinema Management System (Elite Standard)

## 1. Cấu trúc thư mục (Package Structure)

Hệ thống được tổ chức theo kiến trúc phân tầng kết hợp với các mẫu thiết kế (Design Patterns) chuyên sâu:

```
src/main/java/com/example/cinema/
│
├── config/                    # Cấu hình hệ thống & Elite Interceptors
│   ├── SecurityConfig.java    # Spring Security & JWT
│   ├── AppConfig.java         # ModelMapper & RestTemplate Beans
│   ├── LogAction.java         # Custom Annotation cho Audit Log
│   └── AuditLogAspect.java    # AOP Aspect xử lý ghi nhật ký
│
├── controller/                # REST Controllers (Presentation)
│   ├── MovieController.java
│   ├── SchedulingController.java # AI Scheduling API
│   ├── PricingController.java    # Price Configuration API
│   └── ...
│
├── model/
│   ├── entity/                # JPA Entities (Database Mapping)
│   │   ├── AuditLog.java      # Bảng nhật ký mới
│   │   └── ...
│   ├── dto/                   # Data Transfer Objects
│   │   ├── request/           # Builder Pattern thường dùng cho Request
│   │   └── response/          # Cấu trúc Response chuẩn hóa
│   └── enums/                 # Elite Enums (AgeRating, RoomType, etc.)
│
├── service/                   # Business Logic Layer (Interface)
│   ├── MovieService.java
│   ├── SchedulingService.java # AI Algorithm Interface
│   ├── BuzzAnalysisService.java # External Data Interface
│   ├── pricing/               # 💎 Decorator Pattern Implementation
│   │   ├── PriceCalculator.java
│   │   ├── BasePriceCalculator.java
│   │   ├── PriceDecorator.java
│   │   ├── RoomTypeDecorator.java
│   │   └── SeatTypeDecorator.java
│   ├── strategy/              # 🎯 Strategy Pattern Implementation
│   │   ├── SeatLayoutStrategy.java
│   │   ├── StandardLayoutStrategy.java
│   │   ├── ImaxLayoutStrategy.java
│   │   └── SeatLayoutFactory.java
│   └── impl/                  # Service Implementations
│       ├── MovieServiceImpl.java (Facade Pattern)
│       ├── SchedulingServiceImpl.java (AI Engine)
│       └── ...
│
├── repository/                # Data Access Layer
└── security/                  # Security Logic (JWT, UserDetails)
```

---

## 2. Elite Coding Standards

### 2.1. Naming Conventions
- **Decorator:** Tên lớp kết thúc bằng `Decorator` (ví dụ: `RoomTypeDecorator`).
- **Strategy:** Tên lớp kết thúc bằng `Strategy` (ví dụ: `ImaxLayoutStrategy`).
- **Factory:** Tên lớp kết thúc bằng `Factory`.
- **AOP:** Các phương thức ghi nhật ký sử dụng annotation `@LogAction`.

### 2.2. Dependency Injection
- **Bắt buộc:** Luôn sử dụng Constructor Injection.
- **Quy tắc:** Phụ thuộc vào Interface thay vì Implementation cụ thể (DIP).

### 2.3. DTO Mapping
- Sử dụng **ModelMapper** trung tâm (cấu hình trong `AppConfig.java`) để chuyển đổi tự động giữa Entity và DTO, đảm bảo SRP cho Service.

---

## 3. Quản lý cấu hình nhạy cảm (DevOps)

- **Tệp `.env`**: Lưu trữ các biến môi trường nhạy cảm (TMDB_API_KEY, GMAIL_USER, CLOUDINARY_SECRET).
- **Placeholder**: `application.properties` sử dụng cú pháp `${VARIABLE_NAME}` để tham chiếu.
- **Bảo mật**: Tệp `.env` được đưa vào `.gitignore` để tránh rò rỉ mã nguồn.
