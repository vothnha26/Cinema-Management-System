# Chương: CÁC RÀNG BUỘC TOÀN VẸN

Tài liệu này đặc tả các quy tắc đảm bảo tính nhất quán và toàn vẹn của dữ liệu trong hệ thống FlashCinema thông qua các ràng buộc Khóa chính (PK) và Khóa ngoại (FK).

---

## 1. Ràng buộc Khóa chính (Primary Key - PK)
Mục tiêu: Đảm bảo mỗi dòng trong bảng là duy nhất và không bị trùng lặp.

| Mã số | Mô tả miền giá trị | Thành phần liên quan | Ghi chú |
|:---:|:---|:---|:---|
| PK_01 | Số nguyên lớn (BIGINT), tự tăng | users (id) | Định danh tài khoản |
| PK_02 | Số nguyên lớn (BIGINT), tự tăng | customers (id) | Định danh khách hàng |
| PK_03 | Số nguyên lớn (BIGINT), tự tăng | staffs (id) | Định danh nhân viên |
| PK_04 | Số nguyên lớn (BIGINT), tự tăng | branches (id) | Định danh chi nhánh |
| PK_05 | Số nguyên lớn (BIGINT), tự tăng | rooms (id) | Định danh phòng chiếu |
| PK_06 | Chuỗi (VARCHAR), tối đa 50 ký tự | room_types (id) | Mã loại phòng (Vd: GOLD) |
| PK_07 | Số nguyên lớn (BIGINT), tự tăng | seats (id) | Định danh ghế ngồi |
| PK_08 | Chuỗi (VARCHAR), tối đa 50 ký tự | seat_types (id) | Mã loại ghế (Vd: VIP) |
| PK_09 | Số nguyên lớn (BIGINT), tự tăng | movies (id) | Định danh bộ phim |
| PK_10 | Chuỗi (VARCHAR), tối đa 50 ký tự | genres (id) | Mã thể loại phim |
| PK_11 | Chuỗi (VARCHAR), tối đa 50 ký tự | formats (id) | Mã định dạng (2D, 3D...) |
| PK_12 | Số nguyên lớn (BIGINT) | actors (id) | Định danh diễn viên |
| PK_13 | Số nguyên lớn (BIGINT) | directors (id) | Định danh đạo diễn |
| PK_14 | Số nguyên lớn (BIGINT), tự tăng | showtimes (id) | Định danh suất chiếu |
| PK_15 | Số nguyên lớn (BIGINT), tự tăng | bookings (id) | Định danh đơn đặt vé |
| PK_16 | Số nguyên lớn (BIGINT), tự tăng | booking_details (id) | Mã chi tiết ghế đặt |
| PK_17 | Số nguyên lớn (BIGINT), tự tăng | payments (id) | Mã giao dịch thanh toán |
| PK_18 | Số nguyên lớn (BIGINT), tự tăng | combos (id) | Mã sản phẩm combo |
| PK_19 | Số nguyên lớn (BIGINT), tự tăng | booking_combos (id) | Mã chi tiết bắp nước |
| PK_20 | Số nguyên lớn (BIGINT), tự tăng | pricing_rules (id) | Mã quy tắc tính giá |
| PK_21 | Số nguyên lớn (BIGINT), tự tăng | pricing_conditions (id) | Mã điều kiện giá |
| PK_22 | Số nguyên lớn (BIGINT), tự tăng | promotions (id) | Mã chương trình KM |
| PK_23 | Số nguyên lớn (BIGINT), tự tăng | audit_logs (id) | Mã nhật ký hệ thống |
| PK_24 | Khóa phức hợp (movie_id, actor_id) | movie_actors | Liên kết diễn viên |
| PK_25 | Khóa phức hợp (movie_id, director_id) | movie_directors | Liên kết đạo diễn |
| PK_26 | Khóa phức hợp (branch_id, combo_id) | branch_combos | Phân phối combo |
| PK_27 | Số nguyên lớn (BIGINT), tự tăng | branch_pricing_rules (id) | Định danh gán quy tắc |
| PK_28 | Số nguyên lớn (BIGINT), tự tăng | membership_levels (id) | Định danh hạng thành viên |
| PK_29 | Số nguyên lớn (BIGINT), tự tăng | membership_benefits (id) | Định danh quyền lợi |
| PK_30 | Số nguyên lớn (BIGINT), tự tăng | notifications (id) | Định danh thông báo |

---

## 2. Ràng buộc Khóa ngoại (Foreign Key - FK)
Mục tiêu: Đảm bảo mối liên kết tham chiếu và tính toàn vẹn giữa các bảng liên quan.

| Mã số | Mô tả miền giá trị | Thành phần liên quan | Ghi chú |
|:---:|:---|:---|:---|
| FK_01 | bigint -> users.id | customers (user_id) | Một khách hàng phải có một tài khoản |
| FK_02 | bigint -> users.id | staffs (user_id) | Một nhân viên phải có một tài khoản |
| FK_03 | bigint -> branches.id | staffs (branch_id) | Nhân viên thuộc về một chi nhánh |
| FK_04 | bigint -> branches.id | rooms (branch_id) | Phòng chiếu thuộc một chi nhánh |
| FK_05 | varchar -> room_types.id | rooms (room_type_id) | Xác định loại của phòng chiếu |
| FK_06 | bigint -> rooms.id | seats (room_id) | Ghế thuộc về một phòng nhất định |
| FK_07 | varchar -> seat_types.id | seats (seat_type_id) | Xác định loại ghế (Standard/VIP) |
| FK_08 | bigint -> movies.id | showtimes (movie_id) | Suất chiếu phải chiếu một phim |
| FK_09 | bigint -> rooms.id | showtimes (room_id) | Suất chiếu diễn ra tại một phòng |
| FK_10 | varchar -> formats.id | showtimes (format_id) | Xác định định dạng của suất chiếu |
| FK_11 | bigint -> customers.id | bookings (customer_id) | Tùy chọn (Cho phép NULL với khách vãng lai) |
| FK_12 | bigint -> showtimes.id | bookings (showtime_id) | Đơn hàng dành cho suất chiếu nào |
| FK_13 | bigint -> bookings.id | booking_details (booking_id) | Chi tiết thuộc về một đơn hàng |
| FK_14 | bigint -> seats.id | booking_details (seat_id) | Suất chiếu đã đặt vị trí ghế nào |
| FK_15 | bigint -> bookings.id | payments (booking_id) | Thanh toán cho một đơn hàng cụ thể |
| FK_16 | bigint -> bookings.id | booking_combos (booking_id) | Combo mua kèm theo đơn hàng |
| FK_17 | bigint -> combos.id | booking_combos (combo_id) | Xác định loại combo đã mua |
| FK_18 | bigint -> branches.id | branch_combos (branch_id) | Chi nhánh có bán combo này |
| FK_19 | bigint -> pricing_rules.id | pricing_conditions (rule_id) | Điều kiện cho một quy tắc giá |
| FK_20 | bigint -> movies.id | movie_actors (movie_id) | Phim có diễn viên tham gia |
| FK_21 | bigint -> actors.id | movie_actors (actor_id) | Diễn viên đóng trong phim |
| FK_22 | bigint -> movies.id | movie_genres (movie_id) | Phim thuộc thể loại |
| FK_23 | varchar -> genres.id | movie_genres (genre_id) | Thể loại được gán cho phim |
| FK_24 | varchar -> room_types.id | room_type_formats (room_type_id) | Loại phòng hỗ trợ định dạng |
| FK_25 | varchar -> formats.id | room_type_formats (format_id) | Định dạng được phòng hỗ trợ |
| FK_26 | bigint -> users.id | notifications (user_id) | Thông báo gửi tới một người dùng |
| FK_27 | bigint -> branches.id | branch_pricing_rules (branch_id) | Áp dụng tại chi nhánh nào |
| FK_28 | bigint -> pricing_rules.id | branch_pricing_rules (rule_id) | Áp dụng quy tắc nào |
| FK_29 | bigint -> membership_levels.id | customers (membership_level_id) | Hạng của khách hàng |
| FK_30 | bigint -> membership_levels.id | membership_benefits (membership_level_id) | Quyền lợi của một cấp độ |

---

Kết thúc tài liệu Đặc tả Ràng buộc Toàn vẹn hệ thống FlashCinema.
