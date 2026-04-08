# 🗄️ Thiết kế Cơ sở Dữ liệu – Cinema Management System

Dự án sử dụng MySQL làm công cụ quản trị cơ sở dữ liệu chính, với **34 thực thể** tương ứng với các bảng lưu trữ thông tin nghiệp vụ và cấu trúc linh hoạt.

## 1. Các Thực thể Chính (Core Entities)

Dưới đây là các bảng quan trọng nhất cấu thành nên hệ thống:

| Tên bảng | Thực thể | Mô tả |
|:---|:---|:---|
| `users` / `customers` | User / Customer | Lưu trữ thông tin người dùng, mật khẩu (BCrypt) và liên kết hạng thành viên (`MembershipLevel`). |
| `movies` | Movie | Lưu trữ nội dung phim (Title, Overview, Poster URL, Trailer, OriginCountry, PriorityLevel). |
| `branches` | Branch | Quản lý các rạp phim (chi nhánh) trong hệ thống. |
| `rooms` | Room | Quản lý phòng chiếu, loại phòng (IMAX, 2D) và chi nhánh trực thuộc. |
| `seats` | Seat | Quản lý sơ đồ ghế trong từng phòng, loại ghế (VIP, Sweetbox). |
| `showtimes` | Showtime | Liên kết Movie + Room + Thời gian. |
| `bookings` | Booking | Ghi lại các giao dịch đặt vé của khách hàng. |
| `pricing_rules` | PricingRule | Định nghĩa các quy tắc tính giá vé (BASE, SURCHARGE, DISCOUNT). |
| `pricing_conditions`| PricingCondition| Các điều kiện logic (Time, Day, Age) để kích hoạt quy tắc giá. |
| `promotions` | Promotion | Lưu trữ các chương trình khuyến mãi và mã giảm giá. |

---

## 2. Các Mối quan hệ Quan trọng (Relationships)

Hệ thống được thiết kế theo hướng Normalized để đảm bảo tính toàn vẹn:
- **Branch - Movie (N-N)**: Thông qua bảng trung gian `BranchMovie`, cho thấy phim nào có mặt ở chi nhánh nào.
- **Room - Seat (1-N)**: Mỗi phòng chiếu có một tập hợp các ghế cố định (`Seat`).
- **Showtime - Booking (1-N)**: Một suất chiếu có nhiều booking, mỗi booking đại diện cho một giao dịch.
- **Booking - BookingDetail (1-N)**: Một giao dịch có thể đặt nhiều ghế, mỗi ghế tương ứng với 1 `BookingDetail` (Ticket).
- **Booking - BookingCombo (1-N)**: Mỗi booking có thể đi kèm nhiều loại combo bắp nước khác nhau.
- **MembershipLevel - MembershipBenefit (1-N)**: Một hạng thành viên có nhiều quyền lợi cụ thể.

---

## 3. Quản lý Quy tắc & Cấu hình (Config Entities)

Hệ thống linh hoạt nhờ các bảng cấu hình:
- **`PricingRule` / `PricingCondition`**: Quy tắc giá động linh hoạt (Decorator Pattern).
- **`SeatPrice`**: Định nghĩa phụ phí cho các loại ghế đặc biệt.
- **`membership_levels`**: Định nghĩa các hạng thành viên (Standard, Silver, Gold, Platinum) và ngưỡng chi tiêu tích lũy.
- **`membership_benefits`**: Danh sách quyền lợi chi tiết (DISCOUNT, POINT_MULTIPLIER).

---

## 4. Ghi log & Giám sát (Audit Entities)

- **`AuditLog`**: Lưu trữ lịch sử hành động của Admin/Manager (Ai đã thay đổi giá vé? Ai đã xóa lịch chiếu?).
- **`Payment`**: Ghi lại lịch sử thanh toán chi tiết (Số tiền, Mã giao dịch Ngân hàng, Thời gian).

---

## 5. Chiến lược tối ưu (Optimization)

- **Indexing**: 
    - `showtimes(start_time, room_id)`: Tăng tốc check xung đột lịch và tìm kiếm lịch cho khách.
    - `movies(title)`: Tăng tốc tìm kiếm phim.
    - `bookings(booking_id)`: Khóa chính và phục vụ mapping Webhook.
- **Redis Cache**: Lưu trữ danh sách `Showtime` đang hoạt động và trạng thái ghế tạm thời (`Temporary Seat Lock`) để giảm tải cho MySQL khi lượng truy cập cao.
