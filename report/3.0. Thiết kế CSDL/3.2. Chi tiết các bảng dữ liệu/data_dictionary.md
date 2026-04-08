# Chương: THIẾT KẾ CƠ SỞ DỮ LIỆU

Trong chương này, chúng ta sẽ đi sâu vào cấu trúc dữ liệu chi tiết của hệ thống StarCinema. Cơ sở dữ liệu được thiết kế để đảm bảo tính toàn vẹn dữ liệu, khả năng mở rộng và hiệu suất truy vấn cao.

## 1. Sơ đồ Thực thể liên kết (ERD)
Sơ đồ ERD mô tả mối quan hệ giữa các thực thể cốt lõi trong hệ thống.
![Sơ đồ ERD](./erd_diagram.puml)

---

## 2. Đặc tả các bảng dữ liệu

### 2.1 Nhóm Người dùng & Phân quyền
![Sơ đồ ERD Cụm Người dùng](./erd_cluster_users.puml)

#### Bảng: users (Tài khoản hệ thống)
Lưu trữ thông tin định danh và đăng nhập của tất cả người dùng trong hệ thống.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | username | VARCHAR(255) | Duy nhất | Tên đăng nhập | Not Null, Unique |
| 3 | password | VARCHAR(255) | - | Mật khẩu đã mã hóa (BCrypt) | Not Null |
| 4 | email | VARCHAR(255) | Duy nhất | Địa chỉ thư điện tử | Not Null, Unique |
| 5 | role | VARCHAR(50) | ADMIN, MANAGER, STAFF, CUSTOMER | Vai trò trong hệ thống | Not Null |
| 6 | status | BOOLEAN | true, false | Trạng thái hoạt động | Mặc định: true |
| 7 | created_at | DATETIME | - | Thời điểm tạo tài khoản | - |

#### Bảng: customers (Thông tin Khách hàng)
Chứa thông tin chi tiết về khách hàng và mảng thành viên.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | user_id | BIGINT | - | Liên kết với tài khoản | FK (users.id) |
| 3 | full_name | VARCHAR(255) | - | Họ và tên khách hàng | Not Null |
| 4 | phone | VARCHAR(20) | - | Số điện thoại liên lạc | - |
| 5 | membership_level_id | BIGINT | - | Liên kết hạng thành viên | FK (membership_levels.id) |
| 6 | points | INT | >= 0 | Điểm tích lũy | - |
| 7 | total_spending | DECIMAL(15,2) | >= 0 | Tổng chi tiêu của khách | - |

#### Bảng: membership_levels (Phân hạng thành viên)
Định nghĩa các cấp bậc thành viên trong hệ thống.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | name | VARCHAR(255) | Duy nhất | Tên hạng (Vd: GOLD) | Not Null, Unique |
| 3 | min_spending | DECIMAL(15,2) | >= 0 | Ngưỡng chi tiêu tối thiểu | - |
| 4 | priority | INT | > 0 | Thứ tự ưu tiên hạng | - |

#### Bảng: membership_benefits (Quyền lợi thành viên)
Chi tiết các ưu đãi cho từng hạng thành viên.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | membership_level_id | BIGINT | - | Thuộc hạng nào | FK (membership_levels.id) |
| 3 | benefit_type | VARCHAR(50) | DISCOUNT, POINT_MULTIPLIER...| Loại quyền lợi | - |
| 4 | benefit_value | VARCHAR(255) | - | Giá trị quyền lợi | - |

#### Bảng: staffs (Thông tin Nhân viên)
Lưu trữ thông tin nhân sự và chi nhánh làm việc.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | user_id | BIGINT | - | Liên kết tài khoản nhân viên | FK (users.id) |
| 3 | branch_id | BIGINT | - | Chi nhánh nhân viên làm việc | FK (branches.id) |
| 4 | staff_code | VARCHAR(50) | Duy nhất | Mã số nhân viên | Unique |
| 5 | full_name | VARCHAR(255) | - | Họ và tên nhân viên | - |
| 6 | position | VARCHAR(100) | - | Chức vụ | - |

---

### 2.2 Nhóm Rạp & Cơ sở vật chất
![Sơ đồ ERD Cụm Cơ sở vật chất](./erd_cluster_facilities.puml)

#### Bảng: branches (Chi nhánh rạp)
Quản lý thông tin các cụm rạp trên hệ thống.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | name | VARCHAR(255) | - | Tên chi nhánh | Not Null |
| 3 | address | VARCHAR(255) | - | Địa chỉ chi tiết | - |
| 4 | city | VARCHAR(100) | - | Thành phố/Tỉnh | - |
| 5 | phone | VARCHAR(20) | - | Số điện thoại liên hệ | - |
| 6 | is_active | BOOLEAN | true/false | Trạng thái hoạt động | Mặc định: true |

#### Bảng: rooms (Phòng chiếu)
Thông tin các phòng chiếu tại các chi nhánh.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | branch_id | BIGINT | - | Liên kết với chi nhánh | FK (branches.id) |
| 3 | room_type_id | VARCHAR(50) | - | Loại phòng (2D, 3D...) | FK (room_types.id) |
| 4 | name | VARCHAR(255) | - | Tên/Số phòng | Not Null |
| 5 | capacity | INT | > 0 | Sức chứa tối đa | Not Null |
| 6 | num_rows | INT | > 0 | Số lượng hàng ghế | - |
| 7 | num_cols | INT | > 0 | Số lượng cột ghế | - |
| 8 | status | VARCHAR(50) | ACTIVE, INACTIVE | Trạng thái phòng | - |

#### Bảng: seats (Ghế ngồi)
Chi tiết từng vị trí ghế trong phòng chiếu.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | room_id | BIGINT | - | Liên kết với phòng | FK (rooms.id) |
| 3 | row_char | VARCHAR(10) | A-Z | Ký hiệu hàng ghế | Not Null |
| 4 | col_num | INT | > 0 | Số thứ tự ghế | Not Null |
| 5 | seat_type_id | VARCHAR(50) | - | Loại ghế (VIP/Thường) | FK (seat_types.id) |
| 6 | status | BOOLEAN | true/false | Ghế có khả dụng không | - |

#### Bảng: room_types / seat_types (Danh mục Master Data)
Lưu trữ các định nghĩa phân loại cho phòng và ghế.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | VARCHAR(50) | - | Mã phân loại (Vd: VIP) | PK |
| 2 | name | VARCHAR(255) | - | Tên hiển thị | Not Null |
| 3 | description | TEXT | - | Mô tả chi tiết | - |

---

### 2.3 Nhóm Phim & Suất chiếu
![Sơ đồ ERD Cụm Phim](./erd_cluster_content.puml)

#### Bảng: movies (Thông tin Phim)
Lưu trữ metadata của các bộ phim trong hệ thống.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | title | VARCHAR(255) | - | Tên phim | Not Null |
| 3 | description | TEXT | - | Nội dung tóm tắt | - |
| 4 | duration | INT | > 0 | Thời lượng (phút) | Not Null |
| 5 | release_date | DATE | - | Ngày khởi chiếu | - |
| 6 | age_rating | VARCHAR(50) | P, K, T13, T16, T18 | Phân loại độ tuổi | - |
| 7 | status | VARCHAR(50) | COMING_SOON, SHOWING... | Trạng thái phát hành | - |
| 8 | tmdb_id | BIGINT | Duy nhất | ID từ hệ thống TMDB | Unique |
| 9 | origin_country | VARCHAR(100) | - | Quốc gia sản xuất | - |
| 10 | priority_level | INT | 1-10 | Độ ưu tiên (dành cho AI) | Mặc định: 1 |

#### Bảng: showtimes (Suất chiếu)
Lịch chiếu cụ thể của phim tại các phòng.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | movie_id | BIGINT | - | Phim được chiếu | FK (movies.id) |
| 3 | room_id | BIGINT | - | Phòng chiếu | FK (rooms.id) |
| 4 | format_id | VARCHAR(50) | - | Định dạng (2D/3D...) | FK (formats.id) |
| 5 | start_time | DATETIME | - | Giờ bắt đầu | Not Null |
| 6 | end_time | DATETIME | - | Giờ kết thúc | Not Null |
| 7 | status | VARCHAR(50) | ACTIVE, CANCELLED | Trạng thái suất chiếu | - |

---

### 2.4 Nhóm Giao dịch & Đặt vé
![Sơ đồ ERD Cụm Đặt vé](./erd_cluster_booking.puml)

#### Bảng: bookings (Hồ sơ đặt vé)
Lưu trữ các giao dịch đặt vé của khách hàng.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | customer_id | BIGINT | - | Khách hàng thực hiện | FK (customers.id) |
| 3 | showtime_id | BIGINT | - | Suất chiếu đã chọn | FK (showtimes.id) |
| 4 | booking_code | VARCHAR(50) | Duy nhất | Mã đặt vé (6 ký tự) | Unique, Not Null |
| 5 | total_price | DECIMAL(15,2) | >= 0 | Tổng tiền thanh toán | Not Null |
| 6 | status | VARCHAR(50) | PENDING, CONFIRMED... | Trạng thái đặt vé | - |
| 7 | created_at | DATETIME | - | Thời điểm đặt vé | - |

#### Bảng: booking_details (Chi tiết ghế đặt)
Chi tiết các ghế trong một hồ sơ đặt vé.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | booking_id | BIGINT | - | Thuộc hồ sơ đặt vé nào | FK (bookings.id) |
| 3 | seat_id | BIGINT | - | Ghế đã chọn | FK (seats.id) |
| 4 | price | DECIMAL(15,2) | >= 0 | Giá vé tại thời điểm đặt | - |

#### Bảng: payments (Thông tin Thanh toán)
Thông tin giao dịch tài chính cho các đặt vé.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | booking_id | BIGINT | Duy nhất | Liên kết với đặt vé | FK (bookings.id), Unique |
| 3 | amount | DECIMAL(15,2) | > 0 | Số tiền đã thanh toán | Not Null |
| 4 | method | VARCHAR(50) | MOMO, VNPAY, CASH | Phương thức thanh toán | - |
| 5 | transaction_id | VARCHAR(255) | - | Mã giao dịch từ cổng thanh toán | - |
| 6 | status | VARCHAR(50) | COMPLETED, FAILED | Trạng thái thanh toán | - |

---

### 2.5 Nhóm Ưu đãi & Chính sách giá
![Sơ đồ ERD Cụm Chính sách & Khuyến mãi](./erd_cluster_pricing.puml)

#### Bảng: promotions (Chương trình Khuyến mãi)
Lưu trữ các mã giảm giá và chiến dịch ưu đãi.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | code | VARCHAR(50) | Duy nhất | Mã khuyến mãi (Vd: KM50) | Unique, Not Null |
| 3 | name | VARCHAR(255) | - | Tên chương trình | Not Null |
| 4 | discount_type | VARCHAR(20) | PERCENTAGE, FIXED_AMOUNT | Loại giảm giá | - |
| 5 | discount_value | DECIMAL(10,2) | > 0 | Giá trị giảm | - |
| 6 | start_date | DATE | - | Ngày bắt đầu | - |
| 7 | end_date | DATE | - | Ngày kết thúc | - |
| 8 | usage_limit | INT | > 0 | Số lượt dùng tối đa | - |

#### Bảng: pricing_rules (Quy tắc tính giá)
Định nghĩa các quy tắc cộng thêm hoặc giảm giá vé tự động (Decorator Pattern).

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | name | VARCHAR(255) | - | Tên quy tắc (Vd: Cuối tuần) | Not Null |
| 3 | category | VARCHAR(50) | BASE, SURCHARGE, DISCOUNT | Nhóm quy tắc | - |
| 4 | impact_type | VARCHAR(20) | ADDITIVE, PERCENTAGE, FIXED | Cách thức tác động giá | - |
| 5 | impact_value | DECIMAL(10,2) | - | Giá trị tác động | - |
| 6 | is_stackable | BOOLEAN | - | Có được cộng dồn không | Mặc định: true |
| 7 | is_active | BOOLEAN | - | Trạng thái hoạt động | Mặc định: true |

#### Bảng: pricing_conditions (Chi tiết điều kiện giá)
Các điều kiện logic để kích hoạt quy tắc giá.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK |
| 2 | rule_id | BIGINT | - | Thuộc quy tắc giá nào | FK (pricing_rules.id) |
| 3 | type | VARCHAR(50) | TIME, AGE, DAY_OF_WEEK... | Loại điều kiện | - |
| 4 | value | VARCHAR(255) | - | Giá trị điều kiện (String/JSON) | - |
| 5 | description | VARCHAR(255) | - | Mô tả điều kiện | - |

---

### 2.6 Nhóm Bắp nước & Combo

#### Bảng: combos (Danh mục Combo)
Thông tin các gói bắp nước bán kèm.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | name | VARCHAR(255) | - | Tên Combo | Not Null |
| 3 | price | DECIMAL(10,2) | > 0 | Giá bán | Not Null |
| 4 | stock_quantity | INT | >= 0 | Số lượng tồn kho | Mặc định: 0 |
| 5 | is_active | BOOLEAN | true/false | Trạng thái kinh doanh | - |

#### Bảng: booking_combos (Chi tiết Combo trong đơn hàng)
Lưu trữ các combo mà khách hàng đã mua trong một lần đặt vé.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | booking_id | BIGINT | - | Thuộc đơn hàng nào | FK (bookings.id) |
| 3 | combo_id | BIGINT | - | Loại combo đã mua | FK (combos.id) |
| 4 | quantity | INT | > 0 | Số lượng mua | Not Null |

---

---

### 2.7 Nhóm Danh mục & Thuộc tính mở rộng

#### Bảng: formats / genres / actors / directors (Danh mục Nội dung)
Lưu trữ các danh mục dùng chung cho toàn hệ thống.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT/VARCHAR | Duy nhất | Mã định danh | PK |
| 2 | name | VARCHAR(255) | - | Tên hiển thị | Not Null |
| 3 | description | TEXT | - | Mô tả chi tiết | - |
| 4 | avatar_url | VARCHAR(255) | - | Ảnh (dành cho Actor/Director) | - |

#### Bảng: movie_actors / movie_directors (Bảng trung gian)
Liên kết phim với đội ngũ sản xuất.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | movie_id | BIGINT | - | Liên kết phim | PK, FK (movies.id) |
| 2 | actor_id/director_id | BIGINT | - | Liên kết nhân sự | PK, FK |
| 3 | role_name | VARCHAR(100) | - | Vai trò (Vd: Diễn viên chính) | - |

#### Bảng: movie_formats / movie_genres / room_type_formats (Bảng JoinTable)
Các bảng liên kết được tạo tự động để quản lý quan hệ Many-to-Many.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | movie_id / room_type_id| BIGINT/VARCHAR | - | Khóa ngoại của bảng cha | PK, FK |
| 2 | format_id / genre_id | VARCHAR(50) | - | Khóa ngoại của bảng tham chiếu | PK, FK |

---

### 2.8 Nhóm Hệ thống & Nhật ký

#### Bảng: audit_logs (Nhật ký hoạt động)
Ghi lại mọi thay đổi quan trọng trên hệ thống của Manager/Admin.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | username | VARCHAR(255) | - | Người thực hiện | Not Null |
| 3 | action | VARCHAR(100) | CREATE, UPDATE, DELETE | Hành động thực thực hiện | - |
| 4 | target | VARCHAR(100) | MOVIE, ROOM... | Đối tượng bị tác động | - |
| 5 | target_id | BIGINT | - | ID của đối diện bị tác động | - |
| 6 | details | TEXT | - | Nội dung chi tiết thay đổi | - |
| 7 | timestamp | DATETIME | - | Thời điểm ghi log | Mặc định: NOW |

#### Bảng: notifications (Thông báo hệ thống)
Quản lý các thông báo gửi đến người dùng/nhân viên.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | user_id | BIGINT | - | Người nhận thông báo | FK (users.id) |
| 3 | title | VARCHAR(255) | - | Tiêu đề thông báo | - |
| 4 | message | TEXT | - | Nội dung thông báo | - |
| 5 | type | VARCHAR(50) | SYSTEM, BOOKING, PROMOTION | Loại thông báo | - |
| 6 | is_read | BOOLEAN | true, false | Trạng thái đã đọc | - |
| 7 | created_at | DATETIME | - | Thời điểm gửi | - |

---

### 2.9 Nhóm Chính sách & Phân phối nâng cao

#### Bảng: membership_benefits (Quyền lợi hội viên)
Định nghĩa quyền lợi và mức chiết khấu theo hạng thẻ.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK |
| 2 | tier | VARCHAR(50) | STANDARD, GOLD... | Tên hạng thẻ | Unique |
| 3 | points_threshold | INT | > 0 | Ngưỡng điểm để lên hạng | - |
| 4 | discount_rate | DECIMAL(5,2) | 0 - 100 | Tỷ lệ giảm giá (%) | - |

#### Bảng: seat_prices / pricing_conditions (Cấu hình giá chi tiết)
Chi tiết cấu hình giá dựa trên khung giờ và điều kiện Decorator.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | rule_id | BIGINT | - | Thuộc quy tắc giá nào | FK (pricing_rules.id) |
| 2 | condition_type | VARCHAR(50) | TIME, AGE, SEAT_TYPE | Loại điều kiện | - |
| 3 | operator | VARCHAR(10) | >, <, =, IN | Phép toán so sánh | - |
| 4 | value | VARCHAR(255) | - | Giá trị điều kiện | - |

---

Kết thúc tài liệu Thiết kế Cơ sở dữ liệu chi tiết hệ thống StarCinema (Phiên bản đầy đủ 34 thực thể/bảng dữ liệu).
