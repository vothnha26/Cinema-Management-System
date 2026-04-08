# 🧪 Chiến lược Kiểm thử (Testing Strategy) – Cinema Management System

Để đảm bảo hệ thống vận hành ổn định với các logic nghiệp vụ phức tạp, dự án áp dụng chiến lược kiểm thử đa tầng (Multi-tier Testing Strategy).

## 1. Kiểm thử Đơn vị (Unit Testing)

Tập trung vào kiểm tra logic nghiệp vụ cô lập trong tầng **Service**.
- **Công cụ**: JUnit 5, Mockito.
- **Phạm vi**:
    - Kiểm tra các thuật toán tính giá trong `PricingService`.
    - Kiểm tra các điều kiện áp dụng khuyến mãi trong `PromotionService`.
    - Kiểm tra logic băm mật khẩu và giải mã JWT.
- **Mục tiêu**: Đảm bảo từng hàm xử lý đúng các trường hợp biên (Edge Cases) mà không cần phụ thuộc vào Database thật.

---

## 2. Kiểm thử Tích hợp (Integration Testing)

Kiểm tra sự tương tác giữa các tầng và tính chính xác của API.
- **Công cụ**: Spring Boot Test, **MockMvc**.
- **Phạm vi**:
    - Gửi request giả lập đến các Controller (Admin, Customer, Staff).
    - Kiểm tra tính đúng đắn của mã lỗi HTTP (200 OK, 400 Bad Request, 403 Forbidden).
    - Kiểm tra tính toàn vẹn của dữ liệu DTO trả về.
- **Mục tiêu**: Đảm bảo luồng đi từ API -> Service -> Repository hoạt động chính xác.

---

## 3. Kiểm thử Giao diện Tự động (End-to-End Testing)

Kiểm tra luồng nghiệp vụ hoàn chỉnh từ góc nhìn người dùng trên trình duyệt.
- **Công cụ**: **Selenium Java**, WebDriverManager.
- **Phạm vi**:
    - Luồng đặt vé: Chọn phim -> Chọn ghế -> Thanh toán -> Nhận QR.
    - Luồng quản trị: Admin đăng nhập -> Quản lý phim -> Thêm suất chiếu.
- **Mục tiêu**: Đảm bảo hệ thống hoạt động đồng bộ giữa Backend và UI, không có lỗi hiển thị hoặc lỗi luồng logic (Logic flows).

---

## 4. Kiểm thử Chịu tải & Dữ liệu lớn (Big Data Testing)

Đặc thù của rạp phim là lượng dữ liệu suất chiếu và booking (lịch sử) rất lớn.
- **Cơ chế**: Sử dụng `BigDataSeeder.java` để sinh hàng triệu bản ghi giả lập trong môi trường kiểm thử.
- **Phạm vi**:
    - Kiểm tra hiệu năng load sơ đồ ghế khi phòng chiếu đạt tối đa lượt đặt.
    - Kiểm tra tốc độ truy vấn báo cáo doanh thu khi database đạt ngưỡng triệu bản ghi.
- **Công cụ**: Spring Seeder + SQL Profiler.

---

## 5. Kiểm thử Bảo mật (Security Testing)

- **JWT Validation**: Kiểm tra khả năng từ chối Token hết hạn hoặc chữ ký bị sửa đổi.
- **Role Scoping**: Chắc chắn rằng tài khoản `Staff` không thể truy cập các API của `Admin` (với `@PreAuthorize` test).
- **Audit Trace**: Kiểm tra sau khi thực hiện hành động, log có được ghi chính xác vào bảng `audit_log` không.
