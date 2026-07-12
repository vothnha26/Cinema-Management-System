CHƯƠNG 5 : KẾT LUẬN

5.1. Những kết quả đã đạt được trong đề tài
5.1.1. Về kiến thức và kỹ năng
Quá trình thực hiện đồ án đã mang lại cho các thành viên trong nhóm nhiều kiến thức và kỹ năng quý báu, không chỉ củng cố nền tảng lý thuyết mà còn nâng cao năng lực thực hành:
Về quy trình phát triển phần mềm: Nhóm đã áp dụng thành công một quy trình phát triển phần mềm bài bản, bao gồm các giai đoạn:
-Khảo sát và phân tích: Phân tích chi tiết hiện trạng sử dụng các hệ thống đặt mua vé xem phim trực tuyến để xác định yêu cầu nghiệp vụ và yêu cầu hệ thống .
-Thiết kế hệ thống: Sử dụng các công cụ và phương pháp luận chuyên nghiệp như UML (sơ đồ Use Case, sơ đồ tuần tự) để mô hình hóa yêu cầu , và thiết kế cơ sở dữ liệu chi tiết bằng sơ đồ ERD.
Về kỹ năng công nghệ:
-Backend: Nắm vững và ứng dụng thành thạo Spring Boot (Java 17) để xây dựng một hệ thống backend mạnh mẽ và có cấu trúc rõ ràng. Triển khai thành công Spring Security để xử lý quá trình xác thực, phân quyền phức tạp cho nhiều vai trò người dùng (RBAC). Sử dụng Spring WebSocket để đồng bộ trạng thái ghế và trạng thái thanh toán thời gian thực.
-Frontend: Kết hợp hiệu quả HTML5, CSS3 và Vanilla JavaScript thuần túy để tạo ra giao diện người dùng trực quan, tối ưu tốc độ tải trang và đảm bảo tính tương thích trên nhiều thiết bị (responsive) mà không cần phụ thuộc vào các framework nặng nề.
-Quản lý dự án: Phối hợp làm việc nhóm một cách hiệu quả thông qua việc sử dụng Git/GitHub để quản lý mã nguồn, theo dõi phiên bản và bảo vệ bí mật hệ thống qua cấu hình biến môi trường (.env).

5.1.2. Về đề tài
Nhóm đã hoàn thành các mục tiêu và chức năng đã đề ra trong phạm vi của đề tài. Sản phẩm cuối cùng là một hệ thống đặt vé xem phim trực tuyến FlashCinema hoạt động ổn định, quy trình nghiệp vụ khép kín từ khâu chọn phim đến thanh toán, đáp ứng đầy đủ yêu cầu cho từng vai trò người dùng:
Đối với Khách hàng (User - Người xem phim):
Quản lý tài khoản: Triển khai hoàn chỉnh chức năng đăng ký, đăng nhập, xác thực OTP qua Email, quản lý hồ sơ cá nhân và theo dõi lịch sử giao dịch.
Trải nghiệm người dùng: Hệ thống cho phép tìm kiếm phim, xem chi tiết thông tin phim từ TMDB, trailer và chọn suất chiếu linh hoạt.
Quy trình đặt vé: Xây dựng thành công tính năng chọn suất chiếu, chọn ghế ngồi theo thời gian thực (Real-time Seat Locking qua Redis) và tự động tính giá vé động.
Thanh toán trực tuyến: Tích hợp thành công giải pháp SePay (VietQR), hỗ trợ xác nhận thanh toán tự động qua Webhook và chuyển trang tức thì qua WebSocket ngay khi tiền về tài khoản.
Đối với Nhân viên (Staff - Tại quầy/Rạp):
Giao diện POS: Hệ thống cung cấp công cụ bán vé và combo bắp nước tại quầy với khả năng chọn phương thức thanh toán (Tiền mặt/Thẻ) linh hoạt.
Quản lý check-in: Công cụ tra cứu và xác nhận vé vào phòng chiếu nhanh chóng và chính xác.
Đối với Quản trị viên và Quản lý (Admin/Manager):
Quản lý nội dung: Dashboard chuyên sâu để quản lý danh mục phim, đồng bộ TMDB, quản lý rạp và phòng chiếu.
Lập lịch AI: Ứng dụng Generative AI (Google Gemini) để tự động hóa việc xếp lịch chiếu phim tối ưu dựa trên phân tích prompt ngữ cảnh.
Kiểm soát và báo cáo: Chức năng thống kê doanh thu đa chiều với bộ lọc Ngày và Giờ chi tiết, giúp hỗ trợ ra quyết định kinh doanh chính xác.

5.2. Ưu điểm
Hệ thống FlashCinema đã đạt được những kết quả tích cực, mang lại giá trị thiết thực cho cả người quản lý rạp và khách hàng:
Số hóa quy trình vận hành toàn diện: Giải quyết triệt để vấn đề đặt trùng ghế qua Redis Lock. Khách hàng có thể chủ động thanh toán và nhận vé điện tử tức thì.
Công nghệ tiên phong: Việc ứng dụng AI (Gemini) và giải pháp thanh toán tự động (SePay) giúp hệ thống mang tính hiện đại và tiệm cận tiêu chuẩn doanh nghiệp.
Kiến trúc bền vững: Tuân thủ tuyệt đối nguyên tắc SOLID và áp dụng các Design Patterns (Decorator, Strategy, Facade) giúp hệ thống cực kỳ dễ mở rộng và bảo trì.
Hiệu năng tối ưu: Frontend tối giản bằng Vanilla JS giúp tốc độ phản hồi nhanh, giảm tải cho trình duyệt khách hàng.

5.3. Nhược điểm
Mặc dù đã hoàn thành các mục tiêu cốt lõi, hệ thống vẫn tồn tại một số hạn chế nhất định:
Chưa có ứng dụng di động chính thức (Native App).
Hệ thống báo cáo nhân sự chuyên sâu còn ở mức cơ bản (đã lược bỏ bớt các chức năng thừa để tập trung vào doanh thu).

5.4. Hướng phát triển
Phát triển ứng dụng Mobile (App FlashCinema).
Nâng cấp AI để phân tích sâu hơn hành vi khách hàng và cá nhân hóa gợi ý phim.
Mở rộng kết nối với các đối tác FnB bên ngoài.
