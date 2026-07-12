# 🎖️ StarCinema Elite Engineering Mandates

## 1. Kiến trúc & Nguyên tắc cốt lõi (Bắt buộc)
- **SOLID Absolute Compliance:** Mọi dòng code viết ra phải được đối soát với 5 nguyên tắc SOLID.
    - *Single Responsibility (SRP):* Tách biệt logic nghiệp vụ, mapping, và validation.
    - *Open/Closed (OCP):* **Bắt buộc** nghiên cứu và áp dụng các mẫu thiết kế (Design Patterns) phù hợp để thay thế cho logic rẽ nhánh `if-else` hoặc `switch-case` khi xử lý các loại (Types/Enums) hoặc các quy tắc nghiệp vụ dễ thay đổi. Mục tiêu là để hệ thống có thể mở rộng tính năng mới mà không cần sửa đổi mã nguồn hiện có.
    - *Liskov Substitution (LSP) & Interface Segregation (ISP):* Thiết kế Interface tinh gọn, đảm bảo tính kế thừa đúng đắn.
    - *Dependency Inversion (DIP):* Chỉ phụ thuộc vào Abstraction, luôn inject qua Constructor.

- **Design Pattern First Mentality:** Trước khi triển khai một logic phức tạp (như Tính giá, Khuyến mãi, Thống kê), phải xem xét mẫu thiết kế nào (Facade, Observer, Decorator, Command...) là tối ưu nhất để đảm bảo tính mở rộng.

## 2. Quy trình phát triển (Standard Flow)
1. **Vẽ Sequence Diagram:** Thiết kế logic và API contract trước khi code.
2. **Triển khai Code:** Backend sạch + UI tích hợp.
3. **Automated Validation:** MockTest (Logic) + E2E Selenium (Giao diện).
4. **Clean Push:** Đăng mã nguồn nghiệp vụ lên GitHub (loại bỏ test code).
5. **Checklist Update:** Cập nhật trạng thái trong `CONDUCTOR.md`.

## 3. Insight kiến trúc hiện tại
- **Seat Layout:** Strategy Pattern (`SeatLayoutStrategy`).
- **Showtime:** Refactored sang SRP (Mapping qua ModelMapper config).
- **Media:** Facade Pattern cho Cloudinary integration.
