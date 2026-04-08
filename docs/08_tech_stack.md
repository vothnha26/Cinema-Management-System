# 🛠️ Danh sách Công nghệ (Tech Stack) – Cinema Management System

Dự án được xây dựng trên nền tảng công nghệ hiện đại, tập trung vào hiệu năng, khả năng mở rộng và bảo mật.

## 1. Core Framework & Language

- **Java 17**: Ngôn ngữ lập trình chính, tận dụng các tính năng mới như Record, Seald Class và hiệu năng tối ưu.
- **Spring Boot 3.2.4**: Framework phát triển ứng dụng Web/Microservices hàng đầu, cung cấp hệ sinh thái mạnh mẽ.

---

## 2. Lưu trữ Dữ liệu (Storage & Database)

- **MySQL 8.0+**: Hệ quản trị cơ sở dữ liệu quan hệ (RDBMS) chính, lưu trữ các thực thể nghiệp vụ.
- **Redis (Spring Data Redis)**: Hệ thống caching In-memory, dùng để tối ưu hóa hiệu năng tìm kiếm lịch chiếu và quản lý Temporary Seat Locking.
- **Cloudinary**: Dịch vụ lưu trữ Media (Poster phim, ảnh chi nhánh) trên đám mây, giúp tối ưu hóa dung lượng lưu trữ trên Server.

---

## 3. Bảo mật & Xác thực (Security)

- **Spring Security**: Framework bảo mật toàn diện cho ứng dụng Java:
    - **JSON Web Token (JJWT 0.12.3)**: Cơ chế Bearer Auth Stateless an toàn, linh hoạt.
    - **BCrypt**: Thuật toán băm mật khẩu chuẩn công nghiệp.

---

## 4. Thương mại & Tiện ích (Commerce & Utilities)

- **VietQR & SePay**: Giải pháp thanh toán QRCode tự động, đồng bộ hóa ngân hàng xác thực giao dịch qua Webhook.
- **ZXing (Google)**: Thư viện sinh mã Barcode/QRCode để hỗ trợ vé điện tử (E-Ticket).
- **TMDB API**: Kết nối lấy dữ liệu phim quốc tế chuẩn hóa.
- **ModelMapper (3.2.0)**: Chuyển đổi dữ liệu tự động giữa Entity và DTO, giảm thiểu code thừa.
- **Dotenv-java**: Quản lý biến môi trường bảo mật qua file `.env`.

---

## 5. Kiểm thử & Chất lượng (Testing & QA)

- **JUnit 5 & Mockito**: Unit test logic nghiệp vụ cô lập.
- **Spring Boot Test (MockMvc)**: Kiểm thử API Endpoints (Integration Test).
- **Selenium Java**: Kiểm thử giao diện người dùng tự động (End-to-End Test), đảm bảo luồng đặt vé hoạt động mượt mà trên browser.

---

## 6. Công cụ quản lý Phụ thuộc (Build Tools)

- **Apache Maven 3.x**: Quản lý thư viện phụ thuộc (`pom.xml`) và đóng gói dự án (JAR/WAR).
