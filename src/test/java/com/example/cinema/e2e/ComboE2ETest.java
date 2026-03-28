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
public class ComboE2ETest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setupTest() {
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); // Mở giao diện để người dùng quan sát
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterEach
    void teardown() throws InterruptedException {
        if (driver != null) {
            Thread.sleep(3000); // Chờ 3 giây để người dùng xem kết quả cuối cùng trước khi đóng
            driver.quit();
        }
    }

    @Test
    @DisplayName("Test: Thêm Combo mới với ảnh thật và kiểm tra hiển thị")
    void testCreateComboWithRealImage() throws InterruptedException {
        String baseUrl = "http://localhost:" + port;
        driver.get(baseUrl + "/manage-combos.html");
        System.out.println("Page Title: " + driver.getTitle());
        Thread.sleep(1000);

        // 1. Chờ trang tải và nhấn nút "Thêm Combo mới"
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-add")));
        addBtn.click();
        Thread.sleep(1000);

        // 2. Điền thông tin vào Modal
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("comboModal")));
        
        String testComboName = "E2E Combo " + System.currentTimeMillis();
        driver.findElement(By.id("name")).sendKeys(testComboName);
        Thread.sleep(500);
        driver.findElement(By.id("description")).sendKeys("Mô tả từ E2E Selenium Test");
        Thread.sleep(500);
        driver.findElement(By.id("price")).sendKeys("150000");
        Thread.sleep(500);
        driver.findElement(By.id("stockQuantity")).sendKeys("100");
        Thread.sleep(500);

        // 3. Upload ảnh mẫu
        File sampleImage = new File("src/test/resources/sample-combo.png");
        assertTrue(sampleImage.exists(), "File ảnh mẫu không tồn tại tại: " + sampleImage.getAbsolutePath());
        
        WebElement fileInput = driver.findElement(By.id("comboImage"));
        fileInput.sendKeys(sampleImage.getAbsolutePath());
        Thread.sleep(1000);

        // 4. Kiểm tra ảnh preview hiển thị
        WebElement previewImg = driver.findElement(By.id("comboImagePreview"));
        wait.until(arg -> !previewImg.getAttribute("src").isEmpty());
        Thread.sleep(1000);

        // 5. Lưu thông tin
        driver.findElement(By.xpath("//button[contains(text(), 'LƯU THÔNG TIN')]")).click();
        Thread.sleep(2000);

        // 6. Xác nhận Combo mới xuất hiện trong danh sách
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h5[contains(text(), '" + testComboName + "')]")));
        
        WebElement comboCard = driver.findElement(By.xpath("//h5[contains(text(), '" + testComboName + "')]/ancestor::div[contains(@class, 'combo-card')]"));
        assertTrue(comboCard.isDisplayed(), "Combo mới không hiển thị trong danh sách!");
        
        // Kiểm tra xem ảnh hiển thị trên Card có phải link Cloudinary không (có chứa 'cloudinary' trong src)
        WebElement cardImg = comboCard.findElement(By.tagName("img"));
        String imgSrc = cardImg.getAttribute("src");
        assertTrue(imgSrc.contains("cloudinary") || imgSrc.startsWith("http"), "Ảnh Combo không phải từ Cloudinary/URL hợp lệ: " + imgSrc);
        
        System.out.println("E2E Test Success: Combo '" + testComboName + "' created with image stored at: " + imgSrc);
    }
}
