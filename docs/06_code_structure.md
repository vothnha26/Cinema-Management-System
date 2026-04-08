# 🏗️ Cấu trúc mã nguồn – Cinema Management System

Dự án được tổ chức theo cấu trúc tiêu chuẩn Maven/Spring Boot, phân chia rõ ràng theo Domain-Driven Design (DDD) ở tầng Service để dễ dàng mở rộng và bảo trì.

## 1. Cấu trúc Package chính

Toàn bộ code Java nằm trong package `com.example.cinema`:

| Package | Nhiệm vụ |
|:---|:---|
| `config` | Chứa các file cấu hình: Security, Cloudinary, SePay, và các Seeder dữ liệu lớn. |
| `controller` | Chứa các REST Controller, được phân chia theo đối tượng người dùng (Admin, Staff, Public API). |
| `exception` | Quản lý xử lý lỗi tập trung (`GlobalExceptionHandler`) và các Custom Exception. |
| `model` | Chứa các Định nghĩa dữ liệu: `entity` (Database), `dto` (Data Transfer Object), `enums` (Trạng thái). |
| `repository` | Các interface Spring Data JPA thực hiện thao tác CRUD trên database. |
| `security` | Triển khai **JWT Authentication**: Filter, Token Provider, UserDetailsService. |
| `service` | Logic nghiệp vụ cốt lõi, được chia thành các Domain (Booking, Commerce, Showtime...). |
| `util` | Các lớp tiện ích: Tạo QR Code, định dạng tiền tệ, xử lý ngày tháng. |

---

## 2. Chi tiết tầng Service (Business Domain)

Điểm đặc biệt của codebase này là việc chia nhỏ Service theo nghiệp vụ để áp dụng Design Patterns:

- **`service.showtime`**: 
    - `strategy`: Chứa các thuật toán lập lịch chiếu khác nhau.
- **`service.commerce.pricing`**:
    - Chứa các `Decorator` (Additive, Percentage) để tính toán giá vé động.
    - Chứa `PricingRuleMatcher` để tìm quy tắc giá phù hợp.
- **`service.booking`**: 
    - `impl`: Chứa `BookingServiceImpl` (Xử lý transactional) và `CustomerBookingFacade` (Đóng gói luồng khách hàng).
- **`service.infrastructure`**: Xử lý các dịch vụ hạ tầng như Media (Cloudinary) và Email.

---

## 3. Quy trình phát triển (Backend Workflow)

Hệ thống tuân thủ quy trình xử lý dữ liệu nghiêm ngặt:
1.  **Request**: Đi qua `JwtAuthenticationFilter` để xác thực quyền truy cập.
2.  **Controller**: Nhận dữ liệu (DTO), gọi Service tương ứng qua Interface (DIP - Dependency Inversion).
3.  **Service**: Thực thi logic. Nếu cần tính giá, gọi qua `PricingService`. Nếu đặt vé, thực hiện qua `CustomerBookingFacade`.
4.  **Audit**: `AuditLogAspect` tự động bắt các thay đổi dữ liệu trái phép của Admin.
5.  **Response**: Trả về dữ liệu DTO chuẩn hóa cho Frontend thông qua Wrapper thống nhất.

---

## 4. Quản lý Tài nguyên (Resources)

- `src/main/resources`:
    - `application.properties`: Cấu hình database, redis và logging.
    - `templates/`: (Nếu có) Chứa các mẫu email thông báo.
    - `logs/`: Lưu trữ file log hệ thống theo ngày.
