# 🚀 Hướng dẫn Nâng cấp Kiến trúc Hạng thành viên (StarCinema)

## 1. Bối cảnh hiện tại
Hệ thống đang sử dụng liên kết đơn giản (Direct Link) thông qua Enum `MembershipTier`:
- **Entity `Customer`**: Chứa trường `membershipTier` (Enum STRING: STANDARD, SILVER, GOLD...).
- **Entity `MembershipBenefit`**: Chứa thông tin quyền lợi cứng (discount, point_multiplier) gắn trực tiếp với `tier`.
- **Hạn chế**: Khó thêm các loại quyền lợi mới (quà tặng, dịch vụ) mà không phải sửa cấu trúc bảng.

## 2. Mục tiêu cải tiến (Scalability)
Chuyển đổi sang kiến trúc **Master-Detail (Level - Rules)** để cho phép một hạng thành viên có thể tùy biến vô số quyền lợi khác nhau.

## 3. Danh sách công việc cần thực hiện (ToDo List)

### Bước 1: Tạo Entity `MembershipLevel` (Master)
Thay vì dùng Enum, chúng ta dùng bảng để quản lý hạng thẻ:
- Các trường: `id`, `name` (SILVER, GOLD...), `min_spending` (số tiền tối thiểu để đạt hạng), `priority`.
- Đảm bảo tuân thủ SOLID và quy tắc đặt tên của dự án.

### Bước 2: Cập nhật Entity `MembershipBenefit` (Detail)
Chuyển đổi thực thể này thành một "Rule" có thể mở rộng:
- Xóa trường `tier` cũ.
- Thêm trường `membership_level_id` (ManyToOne tới `MembershipLevel`).
- **Nâng cao (Optional)**: Chuyển đổi sang mô hình Key-Value hoặc Strategy để lưu các loại quyền lợi khác nhau (ví dụ: `benefit_type`: 'DISCOUNT', 'VALUE': '10%').

### Bước 3: Cập nhật Entity `Customer`
- Thay đổi trường `membershipTier` (Enum) thành `membershipLevel` (ManyToOne tới `MembershipLevel`).
- Cập nhật các phương thức Getter/Setter tương ứng.

### Bước 4: Cập nhật Cở sở dữ liệu & Seed Data
- Viết SQL Migration hoặc cập nhật Hibernate ddl-auto để tạo bảng mới.
- Cập nhật `PricingRuleSeeder` hoặc tạo mới `MembershipLevelSeeder` để nạp dữ liệu mẫu cho các hạng thẻ (Standard, Silver, Gold, Platinum).

### Bước 5: Cập nhật Logic nghiệp vụ (Pricing Service)
- Sửa đổi logic tính giá trong `PricingService`: Thay vì tìm theo Enum, hãy tìm `MembershipLevel` của khách hàng và lấy danh sách `MembershipBenefit` liên kết để áp dụng giảm giá.

### Bước 6: Cập nhật Tài liệu (Documentation)
- Cập nhật lại sơ đồ ERD (`erd_cluster_users.puml`) để phản ánh mối quan hệ mới.
- Cập nhật `data_dictionary.md`.

---
**Yêu cầu kỹ thuật:**
- Ngôn ngữ: Java 17, Spring Boot 3.x.
- Database: MySQL.
- Tuân thủ nguyên tắc SOLID và Design Patterns (Strategy đề xuất).
