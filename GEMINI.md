# StarCinema Project Mandates

## Core Principles
- **SOLID Compliance:** Mọi logic nghiệp vụ và cấu trúc mã nguồn phải tuân thủ nghiêm ngặt 5 nguyên tắc SOLID.
    - *Single Responsibility:* Mỗi class/module chỉ đảm nhận một nhiệm vụ duy nhất.
    - *Open/Closed:* Ưu tiên sử dụng **Strategy Pattern**, **Template Method**, hoặc **Factory Pattern** thay vì lạm dụng `if-else` hoặc `switch-case` khi xử lý logic phân loại (Type/Enum).
- **Design Patterns Encouragement:** Luôn xem xét áp dụng các mẫu thiết kế phù hợp (Creational, Structural, Behavioral) để giải quyết các vấn đề phức tạp, giúp mã nguồn linh hoạt và dễ bảo trì hơn. 
    - *Ví dụ:* Sử dụng **Facade** để gom nhóm các service phức tạp, **Observer** cho hệ thống thông báo, hoặc **Builder** cho các Object có nhiều thuộc tính.
    - *Liskov Substitution:* Đảm bảo các lớp con có thể thay thế lớp cha mà không làm hỏng logic.
    - *Interface Segregation:* Chia nhỏ interface, không ép buộc implement các phương thức không cần thiết.
    - *Dependency Inversion:* Luôn inject dependency qua Constructor, ưu tiên phụ thuộc vào Abstraction thay vì Concretion.

- **Standard Development Workflow (Bắt buộc):**
    1. **Vẽ Sequence Diagram:** Thiết kế logic, xác định API Contract và các ràng buộc nghiệp vụ trước khi code.
    2. **Triển khai Code:** Viết mã nguồn Backend (Service, Controller, Repository) và tích hợp Giao diện (UI).
    3. **Test tự động:** 
        - *MockTest:* Viết Integration Test sử dụng MockMvc để kiểm tra logic API.
        - *E2E Selenium Test:* Kiểm tra trình duyệt thực tế (sau khi test xong phải dọn dẹp môi trường sạch sẽ).
    4. **Đăng lên GitHub:** Commit và Push mã nguồn nghiệp vụ sạch lên repository.
    5. **Cập nhật Checklist:** Đánh dấu hoàn tất task tương ứng trong file `CONDUCTOR.md`.
- **Testing:** Mỗi tính năng mới hoặc bản sửa lỗi đều phải có Unit Test hoặc Integration Test đi kèm để xác thực.
- **Documentation:** Cập nhật tài liệu kỹ thuật và sơ đồ UML tương ứng với mỗi thay đổi lớn.

## Current Architecture Insights
- **Seat Layout:** Sử dụng Strategy Pattern (`SeatLayoutStrategy`) để khởi tạo sơ đồ ghế theo từng loại phòng (`RoomType`).
