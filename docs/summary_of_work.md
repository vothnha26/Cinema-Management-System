# Tổng kết Nhật ký Công việc - Dự án StarCinema
**Ngày thực hiện:** 07/04/2026

## 1. Cập nhật Tài liệu Nghiệp vụ (Chương 1 & 2)
Đã tiến hành viết lại toàn bộ các mục trong chương "Giới thiệu chung" và "Yêu cầu nghiệp vụ" để phản ánh đúng thực tế mã nguồn:
- **1.1. Đặt vấn đề**: Nhấn mạnh các bài toán thực tế như Dynamic Pricing, AI Scheduling và Real-time Payment.
- **1.2. Tổng quan & Lý do đề xuất**: Đề xuất các giải pháp kỹ thuật cụ thể (Design Patterns, Gemini AI, Redis Caching).
- **1.3. Mục tiêu đề tài**: Chuyển đổi thành các mục tiêu công nghệ chuyên sâu (Stateless, Clean Architecture).
- **2.1. Yêu cầu nghiệp vụ**: Viết lại theo phong cách văn tiểu luận mạch lạc, phân tích sâu về các Actor và luồng vận hành thực tế.

## 2. Phân tích Hệ thống qua sơ đồ UML (Use Case)
Đã hoàn thiện bộ 3 sơ đồ Use Case định dạng PlantUML (.puml) phản ánh 4 vai trò của hệ thống:
- **2.3.1. Sơ đồ Use Case Quản trị (Admin & Manager)**: Làm rõ quyền hạn của Admin (Hệ thống) và Manager (Chi nhánh) kèm theo tính năng AI Scheduling.
- **2.3.2. Sơ đồ Use Case Khách hàng (Customer)**: Mô tả chi tiết luồng đặt vé, tích điểm và thanh toán tự động với Redis locking.
- **2.3.3. Sơ đồ Use Case Nhân viên (Staff)**: Tập trung vào giao diện POS bán vé tại quầy và nghiệp vụ Check-in vé điện tử.

## 3. Phân tích Kỹ thuật chuyên sâu
- Xác định và đưa vào tài liệu các Design Pattern đang áp dụng: **Strategy**, **Decorator**, **Facade**, **Factory**, **AOP**.
- Mô tả cơ chế phối hợp giữa **Gemini API** và **SchedulingService**.
- Mô tả cơ chế đồng bộ thanh toán qua **SePay/VietQR Webhook**.

## 4. Trạng thái hiện tại
Toàn bộ tài liệu Chương 1 & 2 đã được đồng bộ hóa với Codebase. Hệ thống sơ đồ Use Case đã sẵn sàng để chuyển sang bước vẽ sơ đồ Sequence cho các luồng nghiệp vụ phức tạp.
