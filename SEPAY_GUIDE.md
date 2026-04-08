1. Cấu hình (Configuration)
   Dự án sử dụng file application.properties để lưu thông tin kết nối và SePayProperties.java để map vào code.
   - API Key/Secret: Dùng để gọi API của SePay và xác thực chữ ký (Signature) của Webhook.
   - Webhook URL: SePay sẽ gửi thông báo về URL này khi có giao dịch chuyển khoản thành công.

2. Luồng thanh toán (Payment Flow)

Bước 1: Tạo link thanh toán (Checkout)
Khi người dùng nhấn thanh toán, hệ thống gọi SePayService.createPaymentUrl.

- Logic: Service này sẽ gọi API của SePay (thường là tạo một "Checkout" hoặc "Order") để lấy về một link thanh
  toán.
- Redirect: Controller sẽ chuyển hướng (redirect) trình duyệt của người dùng sang link này để họ quét mã QR.

Bước 2: Xử lý Webhook (Quan trọng nhất)
Khi người dùng chuyển khoản xong, SePay gửi một POST request chứa dữ liệu giao dịch về hệ thống của bạn.

- Endpoint: /api/payments/webhook (được cấu hình trong SePayWebhookController).
- Xác thực: Kiểm tra Header X-SEPAY-SIGN để đảm bảo request đúng là từ SePay (dựa trên API Secret).
- Xử lý dữ liệu:
  - Parse dữ liệu JSON (số tiền, nội dung chuyển khoản - thường chứa mã đơn hàng).
  - Cập nhật trạng thái đơn hàng trong database.
  - Ghi log giao dịch vào bảng ThanhToan.

3. Các File quan trọng bạn nên tham khảo:

Tôi sẽ đọc nội dung chi tiết của SePayServiceImpl.java để bạn thấy cách họ gọi API và xử lý Webhook:

╭───────────────────────────────────────────────────────────────────────────────────────────────────────────────╮
│ ✓ ReadFile src\...\impl\SePayServiceImpl.java │
│ │
╰───────────────────────────────────────────────────────────────────────────────────────────────────────────────╯
✦ Dựa trên mã nguồn thực tế, đây là hướng dẫn chi tiết để bạn áp dụng SePay vào dự án khác:

1. Cấu trúc dữ liệu yêu cầu
   SePay hoạt động tốt nhất khi bạn có một mã định danh (ID) cho giao dịch. Trong dự án này:

- order_id: Chính là ID của bản ghi thanh toán trong database (maThanhToan).
- Nội dung chuyển khoản (Description): Được định dạng là SEVQR {order_id} (ví dụ: SEVQR 123). Điều này giúp SePay
  tự động khớp giao dịch khi tiền về tài khoản ngân hàng.

2. Cách tạo Link thanh toán (Checkout)
   Bạn gửi một request POST đến API của SePay:

- Endpoint: https://api.sepay.vn/v1/transactions/create
- Headers: X-API-KEY: [Your_API_Key]
- Body (Form-URL-Encoded):
  - order_id: ID đơn hàng của bạn.
  - amount: Số tiền.
  - description: Nội dung chuyển khoản (nên để SEVQR [ID]).
  - callback_url: Link Webhook của bạn (SePay sẽ gọi vào đây khi thành công).
  - return_url: Link để khách hàng quay lại web sau khi thanh toán xong.

3. Cách xử lý Webhook (Quan trọng)
   Khi có tiền về, SePay gọi POST vào endpoint của bạn với JSON payload. Code của dự án xử lý như sau:

1. Lấy nội dung chuyển khoản: payload.get("content").
1. Tách ID đơn hàng: Sử dụng Regex để tìm mã đơn hàng trong nội dung.

1 Pattern p = Pattern.compile("SEVQR\\s\*(\\d+)", Pattern.CASE_INSENSITIVE);
2 Matcher m = p.matcher(content);
3 if (m.find()) {
4 Integer orderId = Integer.parseInt(m.group(1));
5 } 3. Xác thực và Cập nhật: - Kiểm tra orderId có tồn tại trong DB không. - Kiểm tra số tiền nhận được (transferAmount) có khớp không (tùy chọn). - Cập nhật trạng thái đơn hàng thành DA_THANH_TOAN.

4. Xử lý Real-time (Trải nghiệm người dùng)
   Dự án này sử dụng WebSocket (SimpMessagingTemplate) để thông báo cho trình duyệt của người dùng ngay lập tức khi
   Webhook xử lý xong.

- Khi Webhook nhận được tiền -> Gửi tin nhắn qua WebSocket tới topic /topic/payment/{orderId}.
- Ở phía Frontend (Javascript), bạn lắng nghe topic này để tự động chuyển trang hoặc hiện thông báo "Thành công"
  mà người dùng không cần F5.

5. Lưu ý bảo mật

- X-SEPAY-SIGN: SePay có gửi kèm chữ ký trong Header. Bạn nên kiểm tra chữ ký này bằng API Secret để tránh việc kẻ
  xấu giả mạo Webhook gửi request ảo đến hệ thống của bạn.
- CSRF: Trong Spring Security, bạn cần cấu hình bỏ qua (ignore) CSRF cho endpoint Webhook vì SePay là bên thứ ba
  gọi vào.

Tóm lại: Bạn cần tạo một Service xử lý gọi API SePay và một API Endpoint (POST) để nhận dữ liệu từ SePay gửi về.
Dùng Regex để bắt mã đơn hàng là cách đơn giản và hiệu quả nhất mà dự án này đang dùng.
