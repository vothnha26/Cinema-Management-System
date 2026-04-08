CHƯƠNG 5 : KẾT LUẬN

5.1. Những kết quả đã đạt được trong đề tài
5.1.1. Về kiến thức và kỹ năng
Quá trình thực hiện đồ án đã mang lại cho các thành viên trong nhóm nhiều kiến thức và kỹ năng quý báu, không chỉ củng cố nền tảng lý thuyết mà còn nâng cao năng lực thực hành:
Về quy trình phát triển phần mềm: Nhóm đã áp dụng thành công một quy trình phát triển phần mềm bài bản, bao gồm các giai đoạn:
-Khảo sát và phân tích: Phân tích chi tiết hiện trạng sử dụng các hệ thống đặt mua vé xem phim trực tuyến để xác định yêu cầu nghiệp vụ và yêu cầu hệ thống .
-Thiết kế hệ thống: Sử dụng các công cụ và phương pháp luận chuyên nghiệp như UML (sơ đồ Use Case, sơ đồ tuần tự) để mô hình hóa yêu cầu , và thiết kế cơ sở dữ liệu chi tiết bằng sơ đồ ERD.
Về kỹ năng công nghệ:
-Backend: Nắm vững và ứng dụng thành thạo Spring Boot (Java) để xây dựng một hệ thống backend mạnh mẽ và có cấu trúc rõ ràng. Triển khai thành công Spring Security để xử lý quá trình xác thực, phân quyền phức tạp cho nhiều vai trò người dùng. Sử dụng Spring WebSocket để xây dựng tính năng nhắn tin và trò chuyện thời gian thực, một trong những chức năng cốt lõi của hệ thống.
-Frontend: Kết hợp hiệu quả HTML, CSS, JavaScript với framework ReactJS và các công nghệ bổ trợ như Redux để tạo ra giao diện người dùng trực quan, tương thích trên nhiều thiết bị (responsive) và có tính tương tác cao.
-Quản lý dự án: Phối hợp làm việc nhóm một cách hiệu quả thông qua việc sử dụng Git/GitHub để quản lý mã nguồn, theo dõi phiên bản và giải quyết xung đột.

5.1.2. Về đề tài
Nhóm đã hoàn thành các mục tiêu và chức năng đã đề ra trong phạm vi của đề tài. Sản phẩm cuối cùng là một hệ thống đặt vé xem phim trực tuyến StarCine hoạt động ổn định, quy trình nghiệp vụ khép kín từ khâu chọn phim đến thanh toán, đáp ứng đầy đủ yêu cầu cho từng vai trò người dùng:
Đối với Khách hàng (User - Người xem phim):
Quản lý tài khoản: Triển khai hoàn chỉnh chức năng đăng ký, đăng nhập (hỗ trợ xác thực qua Google), quản lý hồ sơ cá nhân và theo dõi lịch sử giao dịch.
Trải nghiệm người dùng: Hệ thống cho phép tìm kiếm phim theo thể loại, trạng thái (đang chiếu/sắp chiếu), xem chi tiết thông tin phim, trailer và các đánh giá từ cộng đồng.
Quy trình đặt vé: Xây dựng thành công tính năng chọn suất chiếu, chọn ghế ngồi theo thời gian thực (Real-time) và áp dụng mã giảm giá (Voucher) vào đơn hàng.
Thanh toán trực tuyến: Tích hợp thành công cổng thanh toán điện tử (MoMo/VNPAY), đảm bảo quy trình thanh toán an toàn, bảo mật và cập nhật trạng thái vé ngay lập tức sau khi giao dịch thành công.
Đối với Nhân viên (Staff - Tại quầy/Rạp):
Quản lý check-in: Hệ thống cung cấp công cụ quét mã QR trên vé để xác nhận vào phòng chiếu nhanh chóng và chính xác.
Hỗ trợ khách hàng: Nhân viên có thể tra cứu thông tin vé, hỗ trợ khách hàng đổi trả hoặc xử lý các sự cố phát sinh trong quá trình đặt vé tại rạp.
Quản lý cơ sở vật chất: Theo dõi tình trạng ghế ngồi, trạng thái phòng chiếu và các thiết bị hỗ trợ tại rạp được phân công.
Đối với Quản trị viên hệ thống (Admin):
Quản lý nội dung: Xây dựng trang quản trị (Dashboard) chuyên sâu để quản lý danh mục phim, lịch chiếu (Showtimes), thông tin rạp và phòng chiếu.
Quản lý kinh doanh: Hệ thống cho phép thiết lập giá vé linh hoạt (theo khung giờ, ngày lễ), quản lý các chương trình khuyến mãi và danh sách thành viên.
Kiểm soát và báo cáo: Triển khai chức năng thống kê doanh thu theo ngày/tháng/phim, theo dõi số lượng vé bán ra và xuất báo cáo dữ liệu trực quan giúp hỗ trợ ra quyết định kinh doanh.
5.2. Ưu điểm
Hệ thống StarCine đã đạt được những kết quả tích cực, mang lại giá trị thiết thực cho cả người quản lý rạp và khách hàng:
Số hóa quy trình bán vé toàn diện: Giải quyết triệt để vấn đề xếp hàng và nghẽn mạng tại quầy vé truyền thống. Khách hàng có thể chủ động chọn phim, giữ ghế và thanh toán mọi lúc mọi nơi, giúp rạp phim tối ưu hóa vận hành và giảm chi phí nhân sự.
Trải nghiệm người dùng hiện đại và trực quan: Giao diện được thiết kế tập trung vào tính tiện dụng, giúp người dùng dễ dàng tiếp cận trailer, thông tin phim và sơ đồ ghế ngồi trực quan theo thời gian thực (Real-time).
Thanh toán an toàn và nhanh chóng: Việc tích hợp các cổng thanh toán điện tử phổ biến (MoMo/VNPAY) giúp quy trình giao dịch trở nên minh bạch, an toàn và chuyên nghiệp, tạo sự tin tưởng tuyệt đối cho khách hàng.
Kiến trúc hệ thống mạnh mẽ: Sử dụng công nghệ Spring Boot kết hợp với MySQL và JWT, hệ thống đảm bảo khả năng xử lý đồng thời nhiều giao dịch đặt vé cùng lúc, tính bảo mật cao và dễ dàng bảo trì hoặc nâng cấp thêm các cụm rạp mới.
5.3. Nhược điểm
Mặc dù đã hoàn thành các mục tiêu cốt lõi, hệ thống vẫn tồn tại một số hạn chế nhất định:
Chưa tối ưu cho thiết bị di động: Hiện tại sản phẩm mới chỉ dừng lại ở phiên bản Web Responsive. Việc thiếu một ứng dụng Native (iOS/Android) làm hạn chế khả năng thông báo nhắc lịch chiếu (Push Notifications) và trải nghiệm mượt mà khi quét mã QR vào cổng.
Cơ chế xử lý ghế trống chưa linh hoạt: Hệ thống chưa có tính năng tự động gợi ý chỗ ngồi tối ưu hoặc xử lý các tình huống người dùng giữ ghế quá lâu mà không thanh toán (Holding timeout) một cách triệt để nhất trong các đợt phim "bom tấn".
Hệ thống hoạt động độc lập: Sản phẩm chưa kết nối với các đối tác cung cấp dịch vụ ăn uống bên ngoài hoặc các chương trình khách hàng thân thiết liên kết, làm giảm tính đa dạng trong hệ sinh thái dịch vụ của rạp.
Thiếu tính năng phân tích hành vi: Hệ thống hiện tại chỉ ghi nhận giao dịch mà chưa có các thuật toán phân tích sở thích người dùng để đưa ra các gợi ý phim cá nhân hóa (Recommendation System).
5.4. Hướng phát triển
Để hoàn thiện và nâng cao năng lực cạnh tranh cho StarCine, nhóm đề xuất các hướng phát triển như sau:
Phát triển ứng dụng Mobile (App StarCine): Xây dựng ứng dụng di động để tận dụng các tính năng như định vị rạp gần nhất, lưu vé Offline vào ví điện tử và gửi thông báo khuyến mãi trực tiếp đến điện thoại người dùng.
Ứng dụng AI và Big Data: Tích hợp trí tuệ nhân tạo để phân tích lịch sử xem phim của khách hàng, từ đó tự động gợi ý các bộ phim phù hợp với sở thích cá nhân, đồng thời dự báo doanh thu để hỗ trợ Admin sắp xếp lịch chiếu tối ưu nhất.
Mở rộng hệ sinh thái dịch vụ: Tích hợp thêm các tính năng đặt trước combo bắp nước, quà tặng kèm theo phim và xây dựng hệ thống tích điểm thành viên (Loyalty Points) liên kết đa nền tảng.
Nâng cấp hạ tầng Real-time: Áp dụng các công nghệ như WebSockets để cập nhật trạng thái ghế ngồi tức thời hơn nữa, tránh tuyệt đối tình trạng hai người dùng chọn trùng một ghế trong cùng một thời điểm (Race condition).
