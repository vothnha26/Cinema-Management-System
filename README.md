# 🎬 StarCinema - Hệ Thống Quản Lý Rạp Chiếu Phim Elite

Hệ thống quản lý rạp chiếu phim hiện đại được xây dựng trên nền tảng **Spring Boot 3.2** và **Java 17**, áp dụng nghiêm ngặt các nguyên tắc thiết kế sạch (**SOLID**) và các mẫu thiết kế (**Design Patterns**) chuyên sâu. Dự án tích hợp các công nghệ tiên tiến như Redis, Gemini AI và thanh toán tự động qua SePay/VietQR.

---

## 🚀 1. Giới thiệu đề tài
Hệ thống không chỉ hỗ trợ khách hàng đặt vé trực tuyến mượt mà mà còn là một giải pháp quản trị toàn diện:
- **Trải nghiệm người dùng:** Đặt vé real-time, đồng bộ trạng thái ghế ngồi qua WebSocket.
- **Quản trị thông minh:** Xếp lịch chiếu (AI Scheduling), quản lý hạng thành viên động.
- **Thanh toán linh hoạt:** Tích hợp mã QR động, xác thực thanh toán tự động (Webhook).
- **Kiến trúc bền vững:** Dễ dàng mở rộng và bảo trì nhờ tuân thủ SOLID.

---

## 🛠️ 2. Công nghệ sử dụng (Tech Stack)

### Backend
- **Core:** Java 17, Spring Boot 3.2.4
- **Security:** Spring Security & JWT (Phân quyền Admin/Manager/Staff/Customer)
- **Data:** MySQL 8, Spring Data JPA, Hibernate
- **Performance:** **Redis** (Xử lý Seat Locking real-time & Cache đơn hàng tạm thời)
- **Communication:** Spring WebSocket (STOMP), Spring Mail (Gửi vé điện tử)
- **AI Integration:** **Gemini AI** (Tối ưu hóa suất chiếu & Tư vấn dữ liệu)

### Frontend
- **Interface:** HTML5, Vanilla CSS (Surgical Customization), JavaScript
- **Frameworks:** Bootstrap 5, FontAwesome 6, SweetAlert2
- **Real-time:** SockJS & Stomp Client

### Third-party Integrations
- **Media:** Cloudinary (Quản lý hình ảnh phim)
- **Data Source:** TMDB API (Lấy thông tin phim & Buzz Score toàn cầu)
- **Payment:** SePay / VietQR (Thanh toán qua mã QR tự động)

---

## 🏗️ 3. Kiến trúc hệ thống & Design Patterns

Hệ thống được thiết kế theo mô hình **Layered Architecture** kết hợp với các mẫu thiết kế:

- **Strategy Pattern:** 
    - `PricingStrategy`: Xử lý linh hoạt các quy tắc giá (Ngày lễ, Hội viên, Loại ghế).
    - `SeatLayoutStrategy`: Tạo sơ đồ phòng chiếu (Standard, IMAX, Gold Class).
    - `SchedulingStrategy`: Thuật toán xếp lịch chiếu dựa trên trọng số AI.
- **Facade Pattern:** 
    - `MediaFacade`: Giao tiếp tập trung với Cloudinary.
    - `TMDBFacade`: Tích hợp dữ liệu phim từ TMDB.
- **Decorator Pattern:** Cộng dồn linh hoạt các loại khuyến mãi (`Promotion`).
- **Factory Pattern:** Khởi tạo đối tượng `DiscountStrategy` và `LayoutStrategy`.
- **Observer Pattern:** Thông báo trạng thái suất chiếu và kết quả đặt vé.

---

## 📋 4. Chức năng chính

### 👤 Khách hàng
- Xem lịch chiếu theo chi nhánh, phim, ngày.
- **Đặt vé nhanh (Quick Booking):** 4 bước cascading mượt mà.
- **Real-time Seat Selection:** Ghế được khóa trong 15 phút (Redis) để tránh đặt trùng.
- Quản lý lịch sử giao dịch và tích lũy điểm hội viên.

### 🛡️ Nhân viên (POS)
- Bán vé trực tiếp tại quầy, tra cứu vé qua mã đặt chỗ (Lookup thông minh Redis/DB).
- Soát vé (Check-in) nhanh chóng qua hệ thống.

### 👑 Quản lý (Admin/Manager)
- Quản lý toàn bộ danh mục (Phim, Suất chiếu, Phòng, Chi nhánh, Combo).
- **AI Scheduling:** Tự động đề xuất suất chiếu tối ưu.
- **Advanced Pricing:** Thiết lập quy tắc giá động.
- **Analytics:** Báo cáo doanh thu, hiệu suất bắp nước.
- **Audit Log:** Theo dõi vết hoạt động của toàn bộ hệ thống.

---

## 📂 5. Cấu trúc dự án
```text
CINEMA_MANAGEMENT_SYSTEM
├── src/main/java/com/example/cinema
│   ├── config          # Security, Redis, AI, Seeding logic
│   ├── controller      # REST Controllers (phân theo module)
│   ├── model           # Entity, DTOs, Enums, Mappers
│   ├── repository      # Data Access Layer
│   ├── service         # Business Logic (Modules: movie, booking, pricing, ai...)
│   └── util            # Helper classes (QR, Cloudinary, Security)
├── report              # 📄 Tài liệu chi tiết: UML, ERD, Đặc tả Use Case
├── data                # JSON templates cho phòng chiếu
└── src/main/resources/static # Frontend Assets
```

---

## ⚙️ 6. Hướng dẫn cài đặt (Setup)

### Yêu cầu
- JDK 17, MySQL 8, Redis Server (cổng 6379).

### Các bước thực hiện
1. **Database:** Tạo database `cinema_db` trong MySQL.
2. **Environment:** Sao chép `.env.example` thành `.env` và điền các API Key:
   - `TMDB_API_KEY`, `CLOUDINARY_*`, `GMAIL_*`, `GEMINI_API_KEY`.
3. **Build & Run:**
   ```bash
   mvn clean compile
   mvn spring-boot:run
   ```
4. **Tài khoản mặc định:**
   - **Admin:** `admin` / `123456`
   - Hệ thống sẽ tự động chạy `DataSeeder` và `BigDataSeeder` để tạo dữ liệu phim và rạp mẫu.

---
*Chi tiết về kiến trúc và phát triển, xem tại:* [GUIDE.md](./GUIDE.md) | [Report Folder](./report/)

---
*Phát triển bởi Đội ngũ StarCinema.*
