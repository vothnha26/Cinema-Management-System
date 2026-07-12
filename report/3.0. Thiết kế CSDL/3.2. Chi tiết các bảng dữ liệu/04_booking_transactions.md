# Thiết kế Cơ sở dữ liệu - Nhóm 4: Giao dịch & Bắp nước

Nhóm này quản lý các hoạt động kinh doanh cốt lõi bao gồm đặt vé, thanh toán và bán lẻ combo thực phẩm (FnB).

---

## 24. Bảng: bookings (Hồ sơ đặt vé)
Thông tin tổng quát về một lượt đặt vé của khách hàng.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh đặt vé | PK, Auto Increment |
| 2 | customer_id | BIGINT | - | Khách hàng thành viên | FK_11 (customers.id), Cho phép NULL (Khách vãng lai) |
| 3 | showtime_id | BIGINT | - | Suất chiếu đã chọn | FK (showtimes.id) |
| 4 | booking_code | VARCHAR(50) | Duy nhất | Mã tra cứu vé (6 ký tự) | Unique, Not Null |
| 5 | total_price | DECIMAL(15,2) | >= 0 | Tổng giá tiền sau giảm giá | Not Null |
| 6 | status | VARCHAR(50) | PENDING, CONFIRMED... | Trạng thái thanh toán | - |
| 7 | created_at | DATETIME | - | Thời điểm lập hồ sơ | Mặc định: NOW |

---

## 25. Bảng: booking_details (Chi tiết ghế đặt)
Danh sách các vị trí ghế được chọn trong một đơn hàng.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã chi tiết | PK |
| 2 | booking_id | BIGINT | - | Thuộc đơn hàng nào | FK (bookings.id) |
| 3 | seat_id | BIGINT | - | Mã ghế đã chọn | FK (seats.id) |
| 4 | price | DECIMAL(15,2) | >= 0 | Giá vé của ghế này | - |

---

## 26. Bảng: payments (Thanh toán)
Thông tin giao dịch tài chính liên kết với đơn hàng.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh thanh toán | PK |
| 2 | booking_id | BIGINT | Duy nhất | Thanh toán cho đơn nào | FK (bookings.id), Unique |
| 3 | amount | DECIMAL(15,2) | > 0 | Số tiền đã thực đóng | Not Null |
| 4 | method | VARCHAR(50) | MOMO, VNPAY, CASH | Hình thức thanh toán | - |
| 5 | transaction_id | VARCHAR(255) | - | Mã bút toán từ cổng đối tác | - |
| 6 | status | VARCHAR(50) | COMPLETED, FAILED | Trạng thái giao dịch | - |

---

## 27. Bảng: combos (Danh mục Combo)
Định nghĩa các sản phẩm bắp nước (FnB) được bán tại rạp.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã sản phẩm combo | PK, Auto Increment |
| 2 | name | VARCHAR(255) | - | Tên sản phẩm | Not Null |
| 3 | price | DECIMAL(10,2) | > 0 | Giá bán niêm yết | Not Null |
| 4 | stock_quantity | INT | >= 0 | Số lượng tồn kho hệ thống | - |
| 5 | is_active | BOOLEAN | true, false | Trạng thái kinh doanh | Mặc định: true |

---

## 28. Bảng: booking_combos (Chi tiết Combo đã mua)
Lưu trữ danh sách bắp nước đi kèm trong đơn hàng đặt vé.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã chi tiết combo | PK |
| 2 | booking_id | BIGINT | - | Thuộc đơn đặt vé nào | FK (bookings.id) |
| 3 | combo_id | BIGINT | - | Loại combo đã mua | FK (combos.id) |
| 4 | quantity | INT | > 0 | Số lượng combo đã chọn | Not Null |

---

## 29. Bảng: branch_combos (Phân phối Combo theo Chi nhánh)
Quản lý sự khả dụng và tồn kho của Combo tại từng chi nhánh cụ thể.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | branch_id | BIGINT | - | Mã chi nhánh | PK, FK (branches.id) |
| 2 | combo_id | BIGINT | - | Mã sản phẩm combo | PK, FK (combos.id) |
| 3 | stock_quantity | INT | >= 0 | Số lượng hiện có tại chi nhánh | - |
