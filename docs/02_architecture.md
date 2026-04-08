# 🏗️ Kiến trúc hệ thống – Cinema Management System

## 1. Mô hình phân tầng (Layered Architecture)

Hệ thống tuân thủ mô hình phân tầng tiêu chuẩn của Spring Boot để đảm bảo tính SRP (Single Responsibility Principle) và dễ bảo trì:

1.  **Presentation Layer (Controller)**: Tiếp nhận Request, điều phối dữ liệu qua DTO và trả về ResponseEntity.
2.  **Domain/Business Layer (Service)**: Xử lý logic nghiệp vụ. Tại đây áp dụng các Design Pattern để giải quyết các bài toán phức tạp (Tính giá, Khuyến mãi).
3.  **Persistence Layer (Repository)**: Sử dụng Spring Data JPA để tương tác với MySQL.
4.  **Database/External Layer**: MySQL, Redis (Caching), Cloudinary (Media), SePay/VietQR (Payment).

---

## 2. Các Design Pattern đã áp dụng (Bản đồ thực tế)

Dự án này ưu tiên áp dụng Design Pattern thay cho logic `if-else` phức tạp để đảm bảo tính mở rộng (OCP - Open/Closed Principle).

### 2.1. Strategy Pattern
Dùng để xử lý các thuật toán có nhiều biến thể có thể thay thế cho nhau.
- **`DiscountStrategy`**: Xử lý các loại khuyến mãi khác nhau (Giảm % hoặc Giảm tiền mặt).
    - *Triển khai thực tế*: `PercentageDiscountStrategy`, `FixedDiscountStrategy`.
- **`AIWeightingStrategy`**: Xử lý các quy tắc tính trọng số cho phim trong AI Scheduling.
    - *Yếu tố trọng số*: `OriginCountryWeight` (VN film boost), `BuzzScoreWeight` (TMDB), `PriorityWeight`.

### 2.2. Decorator Pattern
Dùng để "gắn thêm" các quy tắc tính toán lên một đối tượng cơ sở mà không làm thay đổi cấu trúc của nó.
- **`PriceDecorator`**: Tính toán giá vé cuối cùng động dựa trên `PricingRule`.
    - *Quy trình*: Khởi tạo `BasePriceCalculator` -> Wrap qua các Decorator tương ứng với các `PricingRule` thỏa mãn điều kiện (`Additive`, `Percentage`, `Fixed`).

### 2.3. Factory Pattern
Dùng để quản lý việc khởi tạo các Strategy một cách tập trung.
- **`DiscountStrategyFactory`**: Dựa vào mã khuyến mãi (`PromotionType`) để trả về class Strategy phù hợp (`Percentage` hoặc `Fixed`).

### 2.4. Facade Pattern
Dùng để cung cấp một interface đơn giản cho một hệ thống con (subsystem) phức tạp.
- **`CustomerBookingFacade`**: Đóng gói quy trình phức tạp: `Lock ghế` -> `Tính giá` -> `Áp khuyến mãi` -> `Tạo Booking` -> `Sinh QR Thanh toán`.
- **`NotificationFacade`**: Đóng gói việc gửi thông báo đa kênh (`System Notification`, `Email Confirmation`) khi có sự kiện `Booking Success` hoặc `Member Level Up`.

### 2.5. AOP (Aspect-Oriented Programming)
Dùng để xử lý các logic xuyên suốt (cross-cutting concerns).
- **`AuditLogAspect`**: Tự động bắt các phương thức được đánh dấu `@LogAction` để ghi lại lịch sử thao tác của Admin/Manager vào bảng `audit_log`.

---

## 3. Quản lý tính trạng (State Management)

Hệ thống sử dụng các Enums và quy trình chuyển đổi trạng thái nghiêm ngặt cho Booking và Payment:
- **BookingStatus**: `PENDING` -> `PAID` / `EXPIRED` -> `COMPLETED` / `CANCELLED`.
- **PaymentStatus**: `UNPAID` -> `SUCCESS` / `FAILED`.

Cơ chế **Transaction Management** được sử dụng triệt để trong các tác vụ `BookingServiceImpl` để đảm bảo tính toàn vẹn dữ liệu (không có tình trạng đặt trùng ghế).
