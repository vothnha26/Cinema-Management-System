# 🚀 Hướng dẫn Triển khai & Cấu hình – Cinema Management System

Tài liệu này hướng dẫn cách thiết lập môi trường và chạy ứng dụng Cinema Management System trong môi trường phát triển (Local Development Environment).

## 1. Yêu cầu Hệ thống (Prerequisites)

- **Java SDK**: Version 17 trở lên.
- **Build Tool**: Apache Maven 3.x.
- **Database**: MySQL 8.0+.
- **Cache**: Redis Server.

---

## 2. Cấu hình Biến môi trường (.env)

Hệ thống quản lý cấu hình thông qua file `.env`. Sao chép `.env.example` thành `.env` tại thư mục root và cập nhật thông tin:

| Key | Mô tả | Giá trị ví dụ |
|:---|:---|:---|
| `DB_URL` | URL kết nối MySQL | `jdbc:mysql://localhost:3306/cinema_db` |
| `DB_USERNAME` | Tài khoản MySQL | `root` |
| `DB_PASSWORD` | Mật khẩu MySQL | `admin123` |
| `REDIS_HOST` | Host Redis Server | `localhost` |
| `REDIS_PORT` | Port Redis Server | `6379` |
| `JWT_SECRET` | Khóa bảo mật sinh Token | `Chuỗi_ký_tự_bí_mật_tự_đặt` |
| `CLOUDINARY_URL` | API URL của Cloudinary | `cloudinary://key:secret@name` |
| `SEPAY_API_KEY` | Mã API kết nối SePay | (Lấy từ dashboard SePay) |
| `VIETQR_CLIENT_ID` | Client ID kết nối VietQR | (Lấy từ dashboard VietQR) |

---

## 3. Khởi chạy Ứng dụng (Run Locally)

1.  **Chuẩn bị Database**:
    - Tạo database có tên trùng với `DB_URL`.
    - Ứng dụng sẽ tự động sinh bảng thông qua Hibernate (mặc định: `update`).
2.  **Khởi động các dịch vụ phụ trợ**:
    - Chạy Redis Server (`redis-server`).
3.  **Build & Run**:
    ```bash
    mvn clean spring-boot:run
    ```
4.  **Truy cập**:
    - API Documentation (Swagger/OpenAPI): `http://localhost:8080/swagger-ui.html` (Nếu có).

---

## 4. Dữ liệu Ban đầu (Seeding Data)

Sau khi khởi chạy lần đầu, hãy kích hoạt Seeder để có dữ liệu thử nghiệm:
1.  Truy cập API Endpoint của **DataSeeder** (cần quyền Admin).
2.  Hệ thống sẽ tự động khởi tạo:
    - Tài khoản Admin/Manager mặc định.
    - Danh sách Chi nhánh, Phòng chiếu, Sơ đồ ghế.
    - Một số Phim và Suất chiếu mẫu.

---

## 5. Lưu ý Quan trọng

- **Môi trường**: Đảm bảo cổng `8080`, `3306` và `6379` trống trước khi khởi động.
- **Mail Server**: (Tùy chọn) Nếu muốn dùng tính năng gửi thông báo đặt vé qua Email, hãy cấu hình `spring.mail` trong file `application.properties`.
- **Thanh toán**: Môi trường Local có thể sử dụng SePay Sandbox để test luồng Webhook mà không cần chuyển khoản thật.
