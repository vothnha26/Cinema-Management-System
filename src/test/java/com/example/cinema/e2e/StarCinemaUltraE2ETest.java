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
        driver.findElement(By.id("description")).sendKeys("Mô tả phim được tạo tự động bởi Ultra Test");
        Thread.sleep(1000);

        // 3. Upload Poster (ID chuẩn: posterInput)
        File sampleImage = new File("src/test/resources/sample-combo.png");
        driver.findElement(By.id("posterInput")).sendKeys(sampleImage.getAbsolutePath());
        Thread.sleep(2000);

        // 4. Lưu (Dùng JS click cho nút Lưu)
        WebElement saveBtn = driver.findElement(By.id("btnSaveMovie"));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(3000);

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
        WebElement saveBtn = driver.findElement(By.id("btnSubmitRoom"));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(3000);

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
        Thread.sleep(2000);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h5[contains(text(), '" + comboName + "')]")));
        System.out.println("PASSED: Ultra Add Combo");
    }
}
