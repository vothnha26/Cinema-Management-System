# Thiết kế Cơ sở dữ liệu - Nhóm 1: Người dùng & Phân quyền

Nhóm này quản lý danh tính, vai trò và nhật ký hoạt động của tất cả các đối tượng tham gia vào hệ thống FlashCinema.

---

## 1. Bảng: users (Tài khoản hệ thống)
Lưu trữ thông tin định danh dùng để đăng nhập và phân quyền.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | username | VARCHAR(255) | Duy nhất | Tên đăng nhập | Not Null, Unique |
| 3 | password | VARCHAR(255) | - | Mật khẩu đã mã hóa (BCrypt) | Not Null |
| 4 | email | VARCHAR(255) | Duy nhất | Địa chỉ thư điện tử | Not Null, Unique |
| 5 | role | VARCHAR(50) | ADMIN, MANAGER, STAFF, CUSTOMER | Vai trò trong hệ thống | Not Null |
| 6 | status | BOOLEAN | true, false | Trạng thái hoạt động | Mặc định: true |
| 7 | created_at | DATETIME | - | Thời điểm tạo tài khoản | - |

---

## 2. Bảng: customers (Thông tin Khách hàng)
Chứa hồ sơ cá nhân và thông tin tích lũy của khách hàng thành viên.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh khách hàng | PK, Auto Increment |
| 2 | user_id | BIGINT | - | Liên kết với tài khoản | FK (users.id) |
| 3 | full_name | VARCHAR(255) | - | Họ và tên khách hàng | Not Null |
| 4 | phone | VARCHAR(20) | - | Số điện thoại liên lạc | - |
| 5 | membership_level_id | BIGINT | - | Liên kết hạng thành viên | FK (membership_levels.id) |
| 6 | points | INT | >= 0 | Điểm tích lũy StarPoint | - |
| 7 | total_spending | DECIMAL(15,2) | >= 0 | Tổng chi tiêu tích lũy | - |

---

## 3. Bảng: membership_levels (Phân hạng thành viên)
Cấu trúc Master lưu trữ các cấp bậc thành viên.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | name | VARCHAR(255) | Duy nhất | Tên hạng (Vd: GOLD) | Not Null, Unique |
| 3 | min_spending | DECIMAL(15,2) | >= 0 | Ngưỡng chi tiêu tối thiểu | - |
| 4 | priority | INT | > 0 | Thứ tự ưu tiên hạng | - |

---

## 4. Bảng: membership_benefits (Quyền lợi thành viên)
Bảng Detail lưu trữ các ưu đãi cụ thể cho từng hạng.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | membership_level_id | BIGINT | - | Thuộc hạng thành viên nào | FK (membership_levels.id) |
| 3 | benefit_type | VARCHAR(50) | DISCOUNT, POINT_MULTIPLIER | Loại quyền lợi | - |
| 4 | benefit_value | VARCHAR(255) | - | Giá trị cụ thể (Vd: 10, 1.2) | - |

---

## 5. Bảng: staffs (Thông tin Nhân viên)
Lưu trữ thông tin nhân sự và nơi công tác.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh nhân viên | PK, Auto Increment |
| 2 | user_id | BIGINT | - | Liên kết tài khoản nhân viên | FK (users.id) |
| 3 | branch_id | BIGINT | - | Chi nhánh đang làm việc | FK (branches.id) |
| 4 | staff_code | VARCHAR(50) | Duy nhất | Mã số nhân viên nội bộ | Unique |
| 5 | full_name | VARCHAR(255) | - | Họ và tên nhân viên | - |
| 6 | position | VARCHAR(100) | - | Chức vụ | - |

---

## 6. Bảng: audit_logs (Nhật ký hành động)
Ghi chú lại các thao tác thay đổi dữ liệu của Admin và Manager.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh log | PK, Auto Increment |
| 2 | username | VARCHAR(255) | - | Người thực hiện hành động | Not Null |
| 3 | action | VARCHAR(100) | CREATE, UPDATE, DELETE... | Loại hành động | - |
| 4 | target | VARCHAR(100) | MOVIE, SHOWTIME, ROOM... | Đối tượng bị tác động | - |
| 5 | target_id | BIGINT | - | ID của đối tượng bị tác động | - |
| 6 | details | TEXT | - | Nội dung chi tiết các thay đổi | - |
| 7 | timestamp | DATETIME | - | Thời gian ghi nhận | Mặc định: NOW |

---

## 7. Bảng: notifications (Thông báo)
Hệ thống thông báo đẩy cho người dùng.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh thông báo | PK |
| 2 | user_id | BIGINT | - | Người nhận thông báo | FK (users.id) |
| 3 | title | VARCHAR(255) | - | Tiêu đề thông báo | - |
| 4 | message | TEXT | - | Nội dung chi tiết | - |
| 5 | type | VARCHAR(50) | SYSTEM, BOOKING, PROMOTION | Loại thông báo | - |
| 6 | is_read | BOOLEAN | true, false | Trạng thái đã xem | - |
| 7 | created_at | DATETIME | - | Thời điểm gửi | - |
