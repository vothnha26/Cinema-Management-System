# Thiết kế Cơ sở dữ liệu - Nhóm 5: Khuyến mãi & Chính sách giá

Nhóm này quản lý các quy tắc tính giá linh hoạt (Decorator Pattern) và các chương trình marketing khuyến mãi.

---

## 30. Bảng: pricing_rules (Quy tắc giá gốc)
Định nghĩa các kịch bản thay đổi giá vé (Vd: Phụ thu cuối tuần).

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã quy tắc giá | PK, Auto Increment |
| 2 | name | VARCHAR(255) | - | Tên kịch bản giá | Not Null |
| 3 | category | VARCHAR(50) | BASE, SURCHARGE, DISCOUNT | Nhóm quy tắc | - |
| 4 | impact_type | VARCHAR(20) | ADDITIVE, PERCENTAGE, FIXED | Cách thức tính | - |
| 5 | impact_value | DECIMAL(10,2) | - | Giá trị tác động cụ thể | - |
| 6 | is_stackable | BOOLEAN | - | Có được cộng dồn không | - |
| 7 | is_active | BOOLEAN | - | Trạng thái hoạt động | - |

---

## 31. Bảng: pricing_conditions (Điều kiện giá)
Các điều kiện logic để kích hoạt quy tắc giá (Vd: Thời gian sau 17h).

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã chi tiết điều kiện | PK |
| 2 | rule_id | BIGINT | - | Thuộc quy tắc giá nào | FK (pricing_rules.id) |
| 3 | type | VARCHAR(50) | TIME, AGE, SEAT_TYPE... | Loại tiêu chí kiểm tra | - |
| 4 | value | VARCHAR(255) | - | Giá trị ngưỡng so sánh (String/JSON) | - |
| 5 | description | VARCHAR(255) | - | Mô tả điều kiện | - |

---

## 32. Bảng: seat_prices (Bảng giá ghế theo Suất chiếu)
Cấu hình giá cụ thể cho từng hạng ghế trong một suất chiếu/định dạng.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh | PK |
| 2 | seat_type_id | VARCHAR(50) | - | Áp dụng cho loại ghế nào | FK (seat_types.id) |
| 3 | format_id | VARCHAR(50) | - | Áp dụng cho định dạng nào | FK (formats.id) |
| 4 | base_price | DECIMAL(10,2) | > 0 | Giá gốc cơ bản | Not Null |

---

## 33. Bảng: promotions (Chương trình Khuyến mãi)
Danh sách các mã giảm giá và chiến dịch marketing.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh khuyến mãi | PK, Auto Increment |
| 2 | code | VARCHAR(50) | Duy nhất | Mã code nhập vào (Vd: STARFREE) | Unique, Not Null |
| 3 | name | VARCHAR(255) | - | Tên chương trình ưu đãi | Not Null |
| 4 | discount_type | VARCHAR(20) | PERCENTAGE, FIXED | Loại hình giảm giá | - |
| 5 | discount_value | DECIMAL(10,2) | > 0 | Mức giảm cụ thể | - |
| 6 | start_date | DATE | - | Ngày bắt đầu áp dụng | - |
| 7 | end_date | DATE | - | Ngày hết hạn | - |
| 8 | usage_limit | INT | > 0 | Giới hạn số lượt sử dụng | - |

---

## 34. Bảng: branch_pricing_rules (Áp dụng quy tắc giá theo chi nhánh)
Bảng trung gian quản lý việc gán các quy tắc giá (Vd: Phụ thu) cho từng chi nhánh cụ thể.

| STT | Thuộc tính | Kiểu | Miền giá trị | Ý nghĩa | Ghi chú |
|:---:|:---|:---|:---|:---|:---|
| 1 | id | BIGINT | > 0 | Mã định danh | PK, Auto Increment |
| 2 | branch_id | BIGINT | - | Mã chi nhánh áp dụng | FK (branches.id) |
| 3 | rule_id | BIGINT | - | Mã quy tắc giá áp dụng | FK (pricing_rules.id) |
| 4 | priority | INT | > 0 | Thứ tự ưu tiên áp dụng tại chi nhánh | - |
