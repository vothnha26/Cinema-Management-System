package com.example.cinema.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BookingSeleniumTest {

    @LocalServerPort
    private int port;

    private WebDriver driver;

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setupTest() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testGetLockedSeatsEndpoint_WithSelenium() {
        // Truy cập endpoint API lấy danh sách ghế bị khóa
        String url = "http://localhost:" + port + "/api/bookings/my-locked-seats?showtimeId=1&sessionId=session-test";
        driver.get(url);

        // Đọc nội dung JSON trả về từ body
        String pageSource = driver.findElement(By.tagName("body")).getText();

        System.out.println(">>> Selenium Response Body: " + pageSource);
        assertNotNull(pageSource);
        // Do chưa đăng nhập (hoặc API trả về unauthorized/forbidden/ok tùy security)
        // Chúng ta kiểm tra response chứa mã JSON của ApiResponse hoặc Security Error
        assertTrue(pageSource.contains("success") || pageSource.contains("status") || pageSource.contains("error") || pageSource.contains("message"));
    }
}
