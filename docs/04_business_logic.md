# 🧠 Logic Nghiệp vụ Cốt lõi – Cinema Management System

Tài liệu này giải thích cách hệ thống xử lý các quy tắc nghiệp vụ phức tạp thông qua các thành phần logic chuyên biệt.

## 1. Logic Tính giá vé động (Dynamic Pricing Logic)

Hệ thống không sử dụng giá vé cứng. Giá được tính toán động dựa trên `PricingRuleMatcher` và chuỗi `PriceDecorator`.

### 1.1. Matching Quy tắc (Matching Rules)
Class `PricingRuleMatcher` chịu trách nhiệm tìm kiếm các quy tắc giá phù hợp nhất:
- **Ưu tiên**: Quy tắc khớp nhiều tiêu chí nhất sẽ được chọn.
- **Tiêu chí**:
    - `DayOfWeek`: Giá cuối tuần khác ngày thường.
    - `IsHoliday`: Ưu tiên giá ngày Lễ nếu có cấu hình.
    - `StartTime`: Khung giờ (Sáng/Trưa/Tối).
    - `MovieFormat`: 2D / 3D / IMAX.

### 1.2. Chuỗi Decorator (Pricing Pipeline)
Giá vé cuối cùng được tính qua một **Pipeline Decorator**:

1.  **Base Price**: Lấy từ `seat_prices` (Hạng ghế + Định dạng).
2.  **Pricing Rules**: Duyệt danh sách Rules kích hoạt tại chi nhánh. Nếu thỏa mãn `PricingCondition` (Vd: Ngày lễ, khung giờ), bọc (wrap) mức giá hiện tại vào một Decorator tương ứng.
3.  **Membership Benefit**: Kiểm tra hạng thành viên của khách hàng, áp dụng giảm giá trực tiếp lên tổng tiền.
4.  **Promotion**: Giảm giá bằng Coupon Code (nếu có).

---

## 2. Logic Khuyến mãi (Promotion Logic)

Mọi mã khuyến mãi (Promo Code) đều được xử lý qua `PromotionService` với các bước kiểm tra nghiêm ngặt:
- **Xác thực thời gian**: Kiểm tra `startDate` và `endDate`.
- **Hạn mức sử dụng**: Kiểm tra `usageLimit` (Tổng số lượt sử dụng tối đa) và `perCustomerLimit` (Số lần 1 người dùng được sử dụng).
- **Giá trị đơn hàng tối thiểu**: Kiểm tra tổng tiền booking có đạt `minOrderValue` không.
- **Áp dụng Strategy**: Tùy theo loại hình khuyến mãi (`FIX_AMOUNT` hoặc `PERCENTAGE`), hệ thống sẽ gọi Strategy tương ứng qua `DiscountStrategyFactory`.

---

## 3. Logic Lập lịch chiếu AI (AI Scheduling Logic)

Hệ thống sử dụng thuật toán **Advanced Weighting** để xếp lịch tự động.

### 3.1 Công thức tính trọng số (Score)
$$Score = (Buzz \times W_b) + (Region \times W_r) + (Priority \times W_p) - (Penalty \times W_{pen})$$

*   **Buzz Score ($W_b$):** Lấy từ TMDB Popularity, phản ánh mức độ quan tâm quốc tế.
*   **Region Score ($W_r$):** Phim Việt Nam (Origin Country = 'VN') được cộng thêm 30% trọng số để ưu tiên điện ảnh nước nhà.
*   **Priority ($W_p$):** Cấp độ ưu tiên (1-10) do Quản lý rạp thiết lập thủ công.
*   **Penalty ($W_{pen}$):** Điểm phạt dựa trên số lần phim đã được chiếu trong ngày để tránh nhàm chán.

- **Diversity Penalty**: Cơ chế phạt giảm điểm nếu phim đã được xếp quá nhiều suất gần nhau để đảm bảo sự đa dạng.
- **Gap Time**: Đảm bảo khoảng nghỉ 15-20 phút giữa các suất chiếu để vận hành.

---

## 4. Quản lý Ghế và Booking (Seat & Inventory Management)

Đây là phần nhạy cảm nhất của hệ thống, đòi hỏi tính nhất quán cao:
- **Pessimistic Locking**: Hệ thống sử dụng khóa mức database khi Khách hàng bắt đầu chọn ghế (Trạng thái: `SELECTED` tạm thời trong Redis trong 5-10 phút).
- **Seat States**: `AVAILABLE` -> `BOOKING` (Tạm khóa) -> `SOLD` / `FAILED` -> `AVAILABLE`.
- **Booking Detail**: Mỗi vé (`Ticket`) được liên kết chặt chẽ với một `Seat` trong một `Showtime` duy nhất.

---

## 5. Logic Tích điểm & Nâng hạng (Loyalty Logic)

- **Cơ chế Membership**: Hệ thống sử dụng mô hình **Master-Detail** linh hoạt:
    - `MembershipLevel`: Quản lý ngưỡng điểm (`minPoints`).
    - `MembershipBenefit`: Quản lý các quyền lợi động dưới dạng Key-Value (Vd: `DISCOUNT_RATE: 10%`).
- **Nâng hạng tự động**: Sau mỗi giao dịch thành công, hệ thống kiểm tra tổng điểm tích lũy với `minPoints` của Level tiếp theo để tự động thăng cấp và cập nhật quyền lợi mới cho khách hàng.
