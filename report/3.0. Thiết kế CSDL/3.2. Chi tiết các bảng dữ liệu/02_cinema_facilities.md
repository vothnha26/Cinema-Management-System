# Thiết kế Cơ sở dữ liệu - Nhóm 2: Cơ sở vật chất & Rạp

Nhóm này quản lý hạ tầng vật lý của hệ thống rạp phim, bao gồm chi nhánh, phòng chiếu và chi tiết ghế ngồi.

---

## 8. Bảng: branches (Chi nhánh rạp)
Lưu trữ thông tin các cụm rạp trong hệ thống.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh duy nhất | PK, Auto Increment |
| 2 | name | VARCHAR(255) | - | Tên chi nhánh rạp | Not Null |
| 3 | address | VARCHAR(255) | - | Địa chỉ chi tiết | - |
| 4 | city | VARCHAR(100) | - | Tỉnh/Thành phố | - |
| 5 | phone | VARCHAR(20) | - | Số điện thoại chi nhánh | - |
| 6 | is_active | BOOLEAN | true, false | Trạng thái hoạt động | Mặc định: true |

---

## 9. Bảng: rooms (Phòng chiếu)
Quản lý các phòng chiếu tại mỗi chi nhánh.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh phòng chiếu | PK, Auto Increment |
| 2 | branch_id | BIGINT | - | Thuộc chi nhánh nào | FK (branches.id) |
| 3 | room_type_id | VARCHAR(50) | - | Loại phòng (Vd: GOLD) | FK (room_types.id) |
| 4 | name | VARCHAR(255) | - | Tên hoặc số phòng | Not Null |
| 5 | capacity | INT | > 0 | Sức chứa tối đa (số ghế) | Not Null |
| 6 | num_rows | INT | > 0 | Tổng số hàng ghế | - |
| 7 | num_cols | INT | > 0 | Tổng số cột ghế | - |
| 8 | status | VARCHAR(50) | ACTIVE, INACTIVE | Trạng thái phòng máy | - |

---

## 10. Bảng: room_types (Loại phòng chiếu)
Định nghĩa các tiêu chuẩn phòng chiếu (Standard, Gold Class, IMAX...).

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | VARCHAR(50) | - | Mã loại phòng (PK) | PK |
| 2 | name | VARCHAR(255) | - | Tên hiển thị của loại phòng | Not Null |

---

## 11. Bảng: seats (Ghế ngồi)
Chi tiết tọa độ và định dạng của từng ghế trong phòng.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh ghế | PK, Auto Increment |
| 2 | room_id | BIGINT | - | Thuộc phòng chiếu nào | FK (rooms.id) |
| 3 | row_char | VARCHAR(10) | A-Z | Ký hiệu hàng ghế | Not Null |
| 4 | col_num | INT | > 0 | Số thứ tự cột ghế | Not Null |
| 5 | seat_type_id | VARCHAR(50) | - | Loại ghế (VIP/Thường) | FK (seat_types.id) |
| 6 | status | BOOLEAN | true, false | Trạng thái ghế có hư hỏng không | Mặc định: true |

---

## 12. Bảng: seat_types (Loại ghế ngồi)
Phân loại chất lượng ghế (Standard, VIP, Sweetbox).

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | VARCHAR(50) | - | Mã loại ghế (PK) | PK |
| 2 | name | VARCHAR(255) | - | Tên hiển thị của loại ghế | Not Null |
| 3 | description | TEXT | - | Mô tả các tiện ích kèm theo | - |

---

## 13. Bảng: room_type_formats (Ràng buộc Phòng-Định dạng)
Bảng trung gian quy định một loại phòng hỗ trợ các định dạng chiếu nào.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | room_type_id | VARCHAR(50) | - | Mã loại phòng | PK, FK (room_types.id) |
| 2 | format_id | VARCHAR(50) | - | Mã định dạng phim hỗ trợ | PK, FK (formats.id) |
