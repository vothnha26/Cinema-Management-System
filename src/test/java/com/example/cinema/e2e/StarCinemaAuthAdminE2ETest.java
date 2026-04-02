package com.example.cinema.e2e;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StarCinemaAuthAdminE2ETest {

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
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        
        driver = new ChromeDriver(options);
        driver.manage().window().setSize(new Dimension(1920, 1080));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    @DisplayName("E2E Auth: Kiểm tra đăng nhập sai thông tin")
    void testLogin_Failure() {
        driver.get(baseUrl + "/auth.html");
        
        // Điền sai thông tin
        WebElement loginUser = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUser")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = 'wrong_user_e2e';", loginUser);
        
        WebElement loginPass = driver.findElement(By.id("loginPass"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = 'wrong_pass_123';", loginPass);
        
        WebElement loginBtn = driver.findElement(By.cssSelector("button[onclick='handleLogin()']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtn);

        // Chờ thông báo lỗi hiện lên (thẻ alert-auth)
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("alertMsg")));
        assertTrue(alert.getText().length() > 0);
        System.out.println("PASSED: Login failure shows alert: " + alert.getText());
    }

    @Test
    @Order(2)
    @DisplayName("E2E Flow: Admin tạo Staff -> Staff đăng nhập")
    void testAdminCreateStaffAndStaffLogin() throws InterruptedException {
        // 1. Admin Đăng nhập (DataSeeder tài khoản: admin / admin123)
        driver.get(baseUrl + "/auth.html");
        WebElement loginUser = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUser")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = 'admin';", loginUser);
        
        WebElement loginPass = driver.findElement(By.id("loginPass"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = 'admin123';", loginPass);
        
        WebElement loginBtn = driver.findElement(By.cssSelector("button[onclick='handleLogin()']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtn);

        // Chờ chuyển hướng dựa trên role ADMIN -> manage-users.html
        wait.until(ExpectedConditions.urlContains("manage-users.html"));
        System.out.println("PASSED: Admin Login success and redirected to manage-users.html");

        // 2. Tạo Staff mới
        WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[data-bs-target='#addUserModal']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);

        String staffUser = "staff_e2e_" + System.currentTimeMillis();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addUserModal")));
        
        WebElement usernameField = driver.findElement(By.id("staffUsername"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", usernameField, staffUser);
        
        WebElement emailField = driver.findElement(By.id("staffEmail"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", emailField, staffUser + "@starcinema.vn");
        
        WebElement passField = driver.findElement(By.id("staffPassword"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = 'password123';", passField);
        
        WebElement roleField = driver.findElement(By.id("staffRole"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = 'STAFF';", roleField);
        
        WebElement saveBtn = driver.findElement(By.cssSelector("button[onclick='createStaff()']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);
        
        // Chờ modal ẩn đi (thành công)
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("addUserModal")));
        System.out.println("PASSED: Admin created new Staff: " + staffUser);

        // 3. Đăng xuất và đăng nhập bằng Staff mới
        driver.get(baseUrl + "/auth.html");
        WebElement loginUserStaff = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginUser")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", loginUserStaff, staffUser);
        
        WebElement loginPassStaff = driver.findElement(By.id("loginPass"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = 'password123';", loginPassStaff);
        
        WebElement loginBtnStaff = driver.findElement(By.cssSelector("button[onclick='handleLogin()']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtnStaff);

        // Chờ chuyển hướng cho Staff -> pos.html
        wait.until(ExpectedConditions.urlContains("pos.html"));
        assertTrue(driver.getCurrentUrl().contains("pos.html"));
        System.out.println("PASSED: New Staff login success and redirected to POS");
    }

    @Test
    @Order(3)
    @DisplayName("E2E Auth: Kiểm tra đăng ký người dùng mới (Customer)")
    void testRegister_Success() throws InterruptedException {
        driver.get(baseUrl + "/auth.html");
        
        // Chuyển sang tab Đăng ký
        WebElement registerTab = driver.findElements(By.className("auth-tab")).get(1);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", registerTab);
        
        // Chờ hiệu ứng fade-in tab
        Thread.sleep(1000);
        
        String newUser = "cust_e2e_" + System.currentTimeMillis();
        WebElement regUsername = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("regUsername")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", regUsername, newUser);
        
        WebElement regFullName = driver.findElement(By.id("regFullName"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = 'E2E Customer Name';", regFullName);
        
        WebElement regEmail = driver.findElement(By.id("regEmail"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", regEmail, newUser + "@gmail.com");
        
        WebElement regPass = driver.findElement(By.id("regPass"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = 'password123';", regPass);
        
        WebElement regPass2 = driver.findElement(By.id("regPass2"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = 'password123';", regPass2);
        
        WebElement regBtn = driver.findElement(By.cssSelector("button[onclick='handleRegister()']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", regBtn);

        // Chờ chuyển hướng cho Customer -> index.html
        wait.until(ExpectedConditions.urlContains("index.html"));
        assertTrue(driver.getCurrentUrl().contains("index.html"));
        System.out.println("PASSED: New Customer registration success and redirected to Home");
    }
}
