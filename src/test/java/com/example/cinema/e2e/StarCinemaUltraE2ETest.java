package com.example.cinema.e2e;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StarCinemaUltraE2ETest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setupTest() {
        baseUrl = "http://localhost:" + port;
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterEach
    void teardown() throws InterruptedException {
        if (driver != null) {
            Thread.sleep(3000); 
            driver.quit();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Ultra Test 1: Thêm Phim Mới (Movie)")
    void testAddMovie() throws InterruptedException {
        driver.get(baseUrl + "/manage-movies.html");
        Thread.sleep(2000);
        
        // 1. Nhấn nút Thêm Phim
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-add")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);
        
        // 2. Chờ Modal hiện và điền thông tin
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("movieModal")));
        Thread.sleep(1500);
        String movieName = "Ultra Movie " + System.currentTimeMillis();
        
        driver.findElement(By.id("title")).sendKeys(movieName);
        Thread.sleep(1000);
        driver.findElement(By.id("duration")).sendKeys("120"); // Bổ sung thời lượng
        Thread.sleep(500);
        driver.findElement(By.id("description")).sendKeys("Mô tả phim được tạo tự động bởi Ultra Test");
        Thread.sleep(1000);

        // 3. Upload Poster (ID chuẩn: posterInput)
        File sampleImage = new File("src/test/resources/sample-combo.png");
        driver.findElement(By.id("posterInput")).sendKeys(sampleImage.getAbsolutePath());
        Thread.sleep(2000);

        // 4. Lưu (Dùng JS click cho nút Lưu)
        WebElement saveBtn = driver.findElement(By.id("btnSaveMovie"));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);
        
        // Đợi và xử lý Alert
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        Thread.sleep(2000);

        // 5. Kiểm tra
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(text(), '" + movieName + "')]")));
        System.out.println("PASSED: Ultra Add Movie");
    }

    @Test
    @Order(2)
    @DisplayName("Ultra Test 2: Thêm Phòng Chiếu (Room)")
    void testAddRoom() throws InterruptedException {
        driver.get(baseUrl + "/manage-rooms.html");
        Thread.sleep(2000);

        // 1. Nhấn nút Thêm Phòng (ID của hàm: openAddModal)
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-add")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);
        
        // 2. Chờ Modal hiện và điền thông tin (ID chuẩn: roomName/roomType)
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("roomModal")));
        Thread.sleep(1500);
        String roomName = "Ultra Room " + System.currentTimeMillis();
        
        driver.findElement(By.id("roomName")).sendKeys(roomName);
        Thread.sleep(1000);
        
        driver.findElement(By.id("roomType")).sendKeys("IMAX");
        Thread.sleep(1000);

        // 3. Lưu (ID chuẩn: btnSubmitRoom)
        WebElement submitBtn = driver.findElement(By.id("btnSubmitRoom"));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);
        
        // Đợi và xử lý Alert
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        Thread.sleep(2000);

        // 4. Kiểm tra
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(text(), '" + roomName + "')]")));
        System.out.println("PASSED: Ultra Add Room");
    }

    @Test
    @Order(3)
    @DisplayName("Ultra Test 3: Thêm Combo Bắp Nước (F&B)")
    void testAddCombo() throws InterruptedException {
        driver.get(baseUrl + "/manage-combos.html");
        Thread.sleep(1000);

        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-add")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);
        Thread.sleep(1000);

        String comboName = "Ultra Combo " + System.currentTimeMillis();
        driver.findElement(By.id("name")).sendKeys(comboName);
        Thread.sleep(500);
        driver.findElement(By.id("price")).sendKeys("120000");
        Thread.sleep(500);
        driver.findElement(By.id("stockQuantity")).sendKeys("200");

        File sampleImage = new File("src/test/resources/sample-combo.png");
        driver.findElement(By.id("comboImage")).sendKeys(sampleImage.getAbsolutePath());
        Thread.sleep(1000);

        driver.findElement(By.xpath("//button[contains(text(), 'LƯU THÔNG TIN')]")).click();
        
        // Đợi và xử lý Alert (nếu có)
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception e) {}
        
        Thread.sleep(2000);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h5[contains(text(), '" + comboName + "')]")));
        System.out.println("PASSED: Ultra Add Combo");
    }

    @Test
    @Order(4)
    @DisplayName("Ultra Test 4: Quản lý Khuyến mãi (Đa kịch bản)")
    void testManagePromotionScenarios() throws InterruptedException {
        driver.get(baseUrl + "/manage-promotions.html");
        Thread.sleep(1500);

        // --- Scenario 1: Tạo Khuyến mãi % với Max Discount ---
        createPromotionUI("PERC" + (System.currentTimeMillis() % 1000), "Giảm 20% Ultra", "PERCENT", "20", "50000", "200000", "GOLD");
        verifyPromotionExists("Giảm 20% Ultra");
        System.out.println("PASSED: Scenario 1 - Percentage Discount with Max Cap");

        // --- Scenario 2: Tạo Khuyến mãi Tiền cố định với Min Order ---
        createPromotionUI("FIXED" + (System.currentTimeMillis() % 1000), "Giảm 100k Ultra", "FIXED", "100000", "0", "500000", "SILVER");
        verifyPromotionExists("Giảm 100k Ultra");
        System.out.println("PASSED: Scenario 2 - Fixed Discount with Min Order");

        // --- Scenario 3: Kiểm tra xóa khuyến mãi ---
        deleteFirstPromotion();
        System.out.println("PASSED: Scenario 3 - Delete Promotion");
        
        Thread.sleep(2000); // Giữ trình duyệt để quan sát
    }

    private void createPromotionUI(String code, String name, String type, String value, String max, String minOrder, String tier) throws InterruptedException {
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-add")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("promoModal")));
        Thread.sleep(800);
        
        driver.findElement(By.id("code")).clear();
        driver.findElement(By.id("code")).sendKeys(code);
        driver.findElement(By.id("name")).clear();
        driver.findElement(By.id("name")).sendKeys(name);
        
        driver.findElement(By.id("discountType")).sendKeys(type);
        driver.findElement(By.id("discountValue")).clear();
        driver.findElement(By.id("discountValue")).sendKeys(value);
        
        driver.findElement(By.id("maxDiscountAmount")).clear();
        driver.findElement(By.id("maxDiscountAmount")).sendKeys(max);
        driver.findElement(By.id("minOrderAmount")).clear();
        driver.findElement(By.id("minOrderAmount")).sendKeys(minOrder);
        
        driver.findElement(By.id("minTier")).sendKeys(tier);
        Thread.sleep(500);

        WebElement saveBtn = driver.findElement(By.xpath("//button[contains(text(), 'TẠO CHIẾN DỊCH')]"));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(2000);
    }

    private void verifyPromotionExists(String name) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h5[contains(text(), '" + name + "')]")));
    }

    private void deleteFirstPromotion() throws InterruptedException {
        WebElement deleteBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-link.text-danger")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);
        Thread.sleep(1000);
        driver.switchTo().alert().accept();
        Thread.sleep(1500);
    }
}
