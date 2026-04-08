# 🔐 Bảo mật & Xác thực – Cinema Management System

Hệ thống Cinema Management System sử dụng mô hình bảo mật mạnh mẽ dựa trên Spring Security và cơ chế Stateless Token (JWT) để bảo vệ tài nguyên và dữ liệu người dùng.

## 1. Cơ chế Xác thực (Authentication)

Dự án áp dụng **JSON Web Token (JWT)** cho mọi giao dịch yêu cầu định danh:

- **Luồng đăng nhập**: 
    1.  Khách hàng gửi `username` + `password`.
    2.  Hệ thống kiểm tra qua `UserDetailsService` và xác thực mật khẩu qua `BCryptPasswordEncoder`.
    3.  Nếu thành công, `TokenProvider` sinh chuỗi JWT chứa thông tin: `Username`, `Expiration`, `Roles`.
- **Luồng xác thực sau đó**:
    - Mọi Request từ Frontend phải đính kèm Header: `Authorization: Bearer <token>`.
    - `JwtAuthenticationFilter` chặn mọi request để giải mã Token, trích xuất quyền hạn (`GrantedAuthorities`) và lưu vào `SecurityContextHolder`.

---

## 2. Phân quyền (Role-based Access Control - RBAC)

Hệ thống phân chia quyền hạn nghiêm ngặt thông qua các Roles được cấu hình trong `SecurityConfig.java`:

| Role | Phạm vi truy cập (API Prefix) | Mô tả |
|:---|:---|:---|
| `PUBLIC` | `/api/public/**` | Chỉ xem: Lịch chiếu, Phim, Thông tin chi nhánh. |
| `CUSTOMER` | `/api/customer/**` | Được phép: Đặt vé, Thanh toán, Quản lý tài khoản cá nhân. |
| `STAFF` | `/api/staff/**` | Được phép: Thao tác POS, Check-in vé, Xem báo cáo bán vé ngày. |
| `MANAGER` | `/api/manager/**` | Được phép: Lập lịch chiếu, quản lý phim tại chi nhánh, xem báo cáo doanh thu. |
| `ADMIN` | `/api/admin/**` | Toàn quyền: Cấu hình hệ thống, quản lý tài khoản Staff, Chi nhánh, Quy tắc giá. |

---

## 3. Bảo vệ Dữ liệu & Integrity

- **Password Hashing**: Sử dụng **BCrypt** (hàm băm một chiều) để lưu trữ mật khẩu, đảm bảo ngay cả khi lộ database, mật khẩu gốc vẫn an toàn.
- **CORS Configuration**: Hệ thống chỉ cho phép các Domain cụ thể (được cấu hình trong `.env`) truy cập API để tránh tấn công CORS.
- **Stateless Session**: Không lưu Session trên Server, giúp hệ thống dễ dàng mở rộng theo chiều ngang (Scaling) và an toàn trước tấn công CSRF.

---

## 4. Audit Logging (Theo dõi hành động)

Đây là tầng bảo mật bổ sung để kiểm soát hành vi người dùng có đặc quyền (`Manager`, `Admin`):
- **Cơ chế**: Sử dụng Spring AOP (`@Aspect`).
- **Phạm vi**: Mọi hành động nhạy cảm (Sửa giá vé, Xóa phim, Thay đổi khuyến mãi) đều được tự động ghi lại vào bảng `audit_log`.
- **Dữ liệu lưu trữ**: ID User, Hành động, Thời gian, Dữ liệu cũ (Old Data), Dữ liệu mới (New Data).

---

## 5. Tích hợp thanh toán an toàn

- **Webhook Verification**: Khi nhận Callback từ SePay/VietQR, hệ thống luôn xác thực chữ ký (Signature) hoặc kiểm tra ID giao dịch duy nhất trong Database trước khi cập nhật trạng thái Booking, tránh tấn công "giả mạo thanh toán".
