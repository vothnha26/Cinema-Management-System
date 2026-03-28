# 🧪 Testing – Cinema Management System (Elite Standard)

## 1. Chiến lược kiểm thử (Elite Pipeline)

Hệ thống áp dụng quy trình kiểm thử 3 lớp để đảm bảo tính đúng đắn của logic nghiệp vụ và giao diện:

```
Elite Testing Pyramid:
         / \
        /E2E\         ← Selenium: Kiểm tra giao diện & Luồng thực tế (Chrome)
       /─────\
      / Integ \       ← SpringBootTest + MockMvc: Kiểm tra API & DB Integration
     /─────────\
    / Unit Tests \    ← JUnit 5 + Mockito: Kiểm tra thuật toán (Pricing, AI)
   /───────────────\
```

---

## 2. Các loại hình kiểm thử

### 2.1. Unit & Integration Tests (MockTest)
**Mục tiêu:** Xác thực thuật toán tính toán và các ràng buộc dữ liệu.
- **Pricing Engine:** Kiểm tra Decorator Pattern có tính đúng giá cộng dồn không.
- **AI Scheduling:** Kiểm tra thuật toán phân bổ giờ vàng (70/30) và dãn cách thời gian.
- **Conflict Detection:** Xác thực việc chặn các suất chiếu trùng lịch.

```java
@SpringBootTest
@Transactional
class PricingIntegrationTest {
    @Test
    void testCalculate_withDecorators() {
        // Giá gốc + VIP + IMAX + Monday Discount...
        assertEquals(expected, pricingService.calculate(showtime, seat));
    }
}
```

### 2.2. End-to-End (E2E) UI Testing
**Công cụ:** Selenium WebDriver + WebDriverManager.
**Quy trình tự động:**
1. Khởi động server trên Port 8081.
2. Selenium mở trình duyệt Chrome thật.
3. Tự động nhập liệu, click nút, và kiểm tra thông báo (Alert/Toast).
4. Kiểm tra dữ liệu hiển thị thực tế (ví dụ: Biểu đồ Dashboard, Danh sách phim).

```java
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class MovieManagementE2ETest {
    @Test
    void testAddMovieFlow() {
        driver.get("http://localhost:8081/manage-movies.html");
        // ... automation steps ...
        assertTrue(driver.getPageSource().contains("Success"));
    }
}
```

---

## 3. Quy trình thực hiện (Workflow)

Mọi tính năng mới đều phải đi qua các bước Validate:
1. **MockMvc Test:** Chạy test API ngầm để xác nhận logic DB.
2. **Selenium E2E:** Chạy test giao diện để xác nhận trải nghiệm người dùng.
3. **Clean Up:** Sau khi test thành công, xóa bỏ code test và dữ liệu rác trước khi push lên GitHub.

---

## 4. Lệnh chạy kiểm thử

```bash
# Chạy toàn bộ test suite
mvn test

# Chạy riêng Integration Tests
mvn test -Dtest=*IntegrationTest

# Chạy riêng E2E UI Tests (Yêu cầu có trình duyệt Chrome)
mvn test -Dtest=*E2ETest
```

---

## 5. Danh sách các Test Case Elite đã thực hiện

| Module | Chức năng kiểm tra | Loại test |
|--------|---------------------|-----------|
| **Movie** | Upload Poster & Map Actor/Director | Integration |
| **Room** | Tự động sinh sơ đồ ghế theo Strategy | Integration |
| **Showtime** | Chặn suất chiếu trùng giờ dọn dẹp | Integration |
| **Pricing** | Tính giá vé qua chuỗi Decorators | Unit |
| **AI Scheduling** | Gợi ý lịch chiếu 70% giờ vàng | E2E |
| **Audit Log** | Tự động ghi nhật ký qua AOP | MockMvc + E2E |
