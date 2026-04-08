# 🎬 StarCinema - Movie Management System

Hệ thống quản lý rạp phim hiện đại được xây dựng trên nền tảng Spring Boot 3 & REST API, áp dụng nghiêm ngặt các nguyên tắc thiết kế sạch (SOLID) và các mẫu thiết kế (Design Patterns) chuyên sâu.

---

## 🚀 1. Mục tiêu kiến trúc

Dự án không chỉ là một ứng dụng quản lý rạp phim thông thường mà là một hệ thống mẫu (Template) về kiến trúc phần mềm bền vững:
- **SOLID Compliance:** Tuân thủ 100% các nguyên tắc thiết kế sạch.
- **Design Patterns Driven:** Áp dụng Strategy, Facade, Decorator và Observer để đảm bảo tính mở rộng cao.
- **AI-Driven Scheduling:** Tối ưu hóa suất chiếu dựa trên dữ liệu thực tế (TMDB Popularity) và các quy tắc kinh doanh.
- **Flexible Membership:** Hệ thống hạng thành viên động, dễ dàng mở rộng quyền lợi mà không cần thay đổi database.

---

## 🛠️ 2. Công nghệ & Thư viện (Tech Stack)

| Thành phần | Công nghệ |
| :--- | :--- |
| **Core Framework** | Java 17, Spring Boot 3.2.4 |
| **Persistence** | MySQL 8, Spring Data JPA, Hibernate |
| **Caching/Locking** | Redis (Xử lý Seat Locking trong thời gian thực) |
| **Security** | Spring Security & JWT (JSON Web Token) |
| **Media Facade** | Cloudinary (Quản lý hình ảnh/poster phim) |
| **External Data** | TMDB API (The Movie Database) - Phân tích Buzz Score |
| **Integrations** | ZXing (Phát sinh mã QR vé), Spring Mail (Gửi email xác nhận) |
| **Testing** | JUnit 5, Mockito, Selenium (E2E) |

---

## 🏗️ 3. Đặc tả Kiến trúc & Design Patterns

### 3.1. SOLID Principles in Practice
- **SRP (Single Responsibility):** Tách biệt logic mapping (ModelMapper), validation (Spring Validation) và business logic.
- **OCP (Open/Closed):** Sử dụng **Strategy Pattern** để thêm mới các loại ghế, loại phòng hoặc quy tắc tính giá mà không sửa đổi code cũ.
- **DIP (Dependency Inversion):** Các Controller chỉ phụ thuộc vào `Interface`, không phụ thuộc trực tiếp vào `ServiceImpl`.

### 3.2. Design Patterns áp dụng
- **Strategy Pattern:**
    - `SchedulingStrategy`: Chiến lược xếp lịch (Ưu tiên doanh thu, phim gia đình, hoặc phim hành động).
    - `SeatLayoutStrategy`: Chiến lược tạo sơ đồ ghế cho các loại phòng khác nhau (IMAX, GoldClass, 2D).
- **Facade Pattern:**
    - `MediaFacade`: Đơn giản hóa việc tương tác với Cloudinary.
    - `TMDBFacade`: Tụ hợp các dịch vụ gọi API từ TMDB để lấy thông tin phim và Buzz Scores.
- **Decorator Pattern:** Sử dụng để tùy biến linh hoạt các loại khuyến mãi (Promotion) có thể cộng dồn hoặc không.

---

## 📊 4. Cấu trúc Cơ sở dữ liệu (ERD)

Hệ thống được thiết kế theo mô hình **Master-Detail** để tối ưu hóa việc mở rộng:

### 🌟 Hệ thống Membership (Nâng cấp)
- `membership_levels`: Quản lý các hạng thành viên (Standard, Silver, Gold, VIP).
- `membership_benefits`: Định nghĩa các quyền lợi cụ thể (Giảm giá vé, Tặng bắp nước, Ưu tiên chọn ghế). Một level có nhiều benefit.

### 🎥 Hệ thống Suất chiếu AI
- Tích hợp `buzz_scores` từ TMDB để tính toán trọng số:
  `Weight = (BuzzScore + Rating * 2) + (Priority * 10) - (UsageCount * 20)`

---

## ⚙️ 5. Hướng dẫn cài đặt (Setup & Run Guide)

### Yêu cầu hệ thống
- JDK 17+
- MySQL 8.0+
- Redis Server (Đang chạy tại localhost:6379)
- Maven 3.8+

### Các bước triển khai
1. **Clone repository:**
   ```bash
   git clone [url-du-an]
   cd cinema-management-system
   ```
2. **Cấu hình môi trường:**
   Tạo file `.env` hoặc chỉnh sửa `src/main/resources/application.properties`:
   - `spring.datasource.url`: URL kết nối MySQL.
   - `tmdb.api.key`: API Key từ [TMDB](https://www.themoviedb.org/).
   - `cloudinary.cloud_name`, `cloudinary.api_key`, `cloudinary.api_secret`: Thông tin từ Cloudinary.
3. **Build dự án:**
   ```bash
   mvn clean compile
   ```
4. **Chạy ứng dụng:**
   ```bash
   mvn spring-boot:run
   ```
5. **Dữ liệu mẫu (Seeding):**
   Hệ thống sẽ tự động chạy `DataSeeder` và `BigDataSeeder` trong lần đầu tiên khởi chạy để tạo tài khoản Admin và danh sách phim mẫu.

---

## 📋 6. Quy trình phát triển (Developer Guidelines)

Mọi tính năng mới phải tuân theo quy trình **🎖️ StarCinema Elite Engineering Mandates**:
1. **Sequence Diagram:** Vẽ sơ đồ tuần tự trước khi viết code.
2. **Implement Backend:** Viết REST API sạch, đầy đủ DTO và Unit Test.
3. **UI Integration:** Tích hợp giao diện và xử lý lỗi tại Frontend.
4. **Automated Validation:** Chạy MockMvc test cho logic và Selenium cho UI flow.

---
*Cập nhật lần cuối: 2026-04-08 | Tác giả: StarCinema AI Engineering Team*