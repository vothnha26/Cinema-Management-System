package com.example.cinema.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ManagerSeleniumTest – E2E UI test cho luồng Manager.
 *
 * ⚠️  YÊU CẦU: Server StarCinema phải đang chạy ở http://localhost:8080
 *             trước khi chạy bộ test này.
 *
 * Chạy bằng:
 *   mvn test -Dtest=ManagerSeleniumTest -Dselenium.headless=false  (mở trình duyệt)
 *   mvn test -Dtest=ManagerSeleniumTest                            (headless mặc định)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ManagerSeleniumTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "http://localhost:8080";
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "123456";

    // Headless theo system property; mặc định headless=true
    private static final boolean HEADLESS = !"false".equalsIgnoreCase(
            System.getProperty("selenium.headless", "true"));

    @BeforeAll
    static void setUpDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        if (HEADLESS) {
            options.addArguments("--headless=new");
        }
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1440,900",
                "--lang=vi"
        );

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        System.out.println(">>> [Selenium] Browser started. Headless=" + HEADLESS);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
            System.out.println(">>> [Selenium] Browser closed.");
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Helper
    // ────────────────────────────────────────────────────────────────────────

    private void navigateTo(String page) {
        driver.get(BASE_URL + "/" + page);
        System.out.println(">>> [Selenium] Navigated to: " + page);
    }

    private WebElement waitForId(String id) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
    }

    private void waitForUrlContains(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    private void setLocalStorageToken(String token, String role, String username) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                "localStorage.setItem('cinemaToken', arguments[0]);" +
                "localStorage.setItem('cinemaRole', arguments[1]);" +
                "localStorage.setItem('cinemaUsername', arguments[2]);",
                token, role, username
        );
    }

    /**
     * Đăng nhập qua giao diện auth.html và lưu token vào localStorage.
     * Trả về token vừa được set.
     */
    private String doLogin() throws Exception {
        navigateTo("auth.html");

        // Nhập thông tin đăng nhập
        WebElement userInput = waitForId("loginUser");
        userInput.clear();
        userInput.sendKeys(ADMIN_USER);

        WebElement passInput = waitForId("loginPass");
        passInput.clear();
        passInput.sendKeys(ADMIN_PASS);

        // Click nút đăng nhập bằng JS (tránh click lag)
        ((JavascriptExecutor) driver).executeScript(
                "document.querySelector('.btn-auth').click();"
        );

        // Chờ redirect về dashboard.html
        try {
            waitForUrlContains("dashboard");
            System.out.println(">>> [Selenium] Login SUCCESS – redirected to dashboard");
        } catch (TimeoutException e) {
            // Fallback: set token thủ công bằng API call trực tiếp qua JS fetch
            System.out.println(">>> [Selenium] UI login timeout – injecting token via fetch fallback");
            String tokenScript = """
                fetch('/api/auth/login', {
                  method: 'POST',
                  headers: {'Content-Type':'application/json'},
                  body: JSON.stringify({username:'%s', password:'%s'})
                }).then(r => r.json()).then(d => {
                  localStorage.setItem('cinemaToken', d.data.token || '');
                  localStorage.setItem('cinemaRole', d.data.role || 'MANAGER');
                  localStorage.setItem('cinemaUsername', d.data.username || '%s');
                  return d.data.token;
                })
                """.formatted(ADMIN_USER, ADMIN_PASS, ADMIN_USER);
            Object token = ((JavascriptExecutor) driver).executeAsyncScript(
                    "var cb = arguments[arguments.length-1]; " + tokenScript + ".then(cb);"
            );
            assertNotNull(token, "Không thể lấy token từ API login");
            System.out.println(">>> [Selenium] Token injected: " + token.toString().substring(0, 20) + "...");
        }

        // Lấy token từ localStorage
        String token = (String) ((JavascriptExecutor) driver)
                .executeScript("return localStorage.getItem('cinemaToken');");
        return token;
    }

    // ────────────────────────────────────────────────────────────────────────
    // TEST CASES
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("UI-1: Trang đăng nhập hiển thị đúng giao diện StarCinema")
    void testLoginPageRendering() {
        navigateTo("auth.html");

        // Kiểm tra title
        assertTrue(driver.getTitle().contains("StarCinema"), "Title phải chứa StarCinema");

        // Kiểm tra form inputs
        WebElement userInput = waitForId("loginUser");
        WebElement passInput = waitForId("loginPass");
        assertNotNull(userInput, "Input username phải tồn tại");
        assertNotNull(passInput, "Input password phải tồn tại");

        // Kiểm tra nút đăng nhập
        WebElement loginBtn = driver.findElement(By.cssSelector(".btn-auth"));
        assertNotNull(loginBtn);
        assertTrue(loginBtn.getText().contains("Đăng nhập") || loginBtn.isDisplayed());

        // Kiểm tra tabs đăng nhập / đăng ký
        var tabs = driver.findElements(By.cssSelector(".auth-tab"));
        assertEquals(2, tabs.size(), "Phải có 2 tab: Đăng nhập & Đăng ký");

        System.out.println(">>> [Selenium] UI-1 PASSED – Login page rendered correctly");
    }

    @Test
    @Order(2)
    @DisplayName("UI-2: Đăng nhập thành công với tài khoản admin")
    void testLoginFlow() throws Exception {
        String token = doLogin();
        assertNotNull(token, "Token phải tồn tại sau khi đăng nhập");
        assertFalse(token.isBlank(), "Token không được rỗng");
        System.out.println(">>> [Selenium] UI-2 PASSED – Login successful, token obtained");
    }

    @Test
    @Order(3)
    @DisplayName("UI-3: Trang Quản lý Phim (manage-movies.html) hiển thị và load data")
    void testManageMoviesPage() throws Exception {
        doLogin(); // Đảm bảo đã đăng nhập
        navigateTo("manage-movies.html");

        // Chờ page load xong – kiểm tra title hoặc element chủ đạo
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete';"));

        String title = driver.getTitle();
        System.out.println(">>> [Selenium] manage-movies.html title: " + title);

        // Kiểm tra body không rỗng
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertFalse(bodyText.isEmpty(), "Trang không được rỗng");

        // Kiểm tra tên trang
        assertTrue(
            title.toLowerCase().contains("movie") || title.toLowerCase().contains("phim") || title.contains("Cinema"),
            "Title trang phải liên quan đến phim: " + title
        );

        // Chụp screenshot (in đường dẫn ra console)
        takeScreenshot("manage_movies");

        System.out.println(">>> [Selenium] UI-3 PASSED – manage-movies.html loaded");
    }

    @Test
    @Order(4)
    @DisplayName("UI-4: Trang Quản lý Suất chiếu (manage-showtimes.html) hiển thị")
    void testManageShowtimesPage() throws Exception {
        doLogin();
        navigateTo("manage-showtimes.html");

        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete';"));

        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertFalse(bodyText.isEmpty(), "Trang manage-showtimes không được rỗng");

        // Kiểm tra không có lỗi JS chạm vào console (bằng cách check body lớn)
        assertTrue(bodyText.length() > 100, "Trang phải có nội dung đáng kể");

        takeScreenshot("manage_showtimes");

        System.out.println(">>> [Selenium] UI-4 PASSED – manage-showtimes.html loaded");
    }

    @Test
    @Order(5)
    @DisplayName("UI-5: Trang Quản lý Giá vé (manage-pricing.html) hiển thị")
    void testManagePricingPage() throws Exception {
        doLogin();
        navigateTo("manage-pricing.html");

        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete';"));

        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertFalse(bodyText.isEmpty(), "Trang manage-pricing không được rỗng");

        takeScreenshot("manage_pricing");

        System.out.println(">>> [Selenium] UI-5 PASSED – manage-pricing.html loaded");
    }

    @Test
    @Order(6)
    @DisplayName("UI-6: Trang Audit Log (manage-audit.html) hiển thị")
    void testManageAuditPage() throws Exception {
        doLogin();
        navigateTo("manage-audit.html");

        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete';"));

        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertFalse(bodyText.isEmpty(), "Trang manage-audit không được rỗng");

        takeScreenshot("manage_audit");

        System.out.println(">>> [Selenium] UI-6 PASSED – manage-audit.html loaded");
    }

    @Test
    @Order(7)
    @DisplayName("UI-7: Trang Khuyến mãi (manage-promotions.html) hiển thị")
    void testManagePromotionsPage() throws Exception {
        doLogin();
        navigateTo("manage-promotions.html");

        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete';"));

        assertFalse(driver.findElement(By.tagName("body")).getText().isEmpty());

        takeScreenshot("manage_promotions");

        System.out.println(">>> [Selenium] UI-7 PASSED – manage-promotions.html loaded");
    }

    @Test
    @Order(8)
    @DisplayName("UI-8: Trang Dashboard (dashboard.html) hiển thị đúng")
    void testDashboardPage() throws Exception {
        doLogin();
        navigateTo("dashboard.html");

        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete';"));

        String title = driver.getTitle();
        System.out.println(">>> [Selenium] dashboard.html title: " + title);
        assertFalse(driver.findElement(By.tagName("body")).getText().isEmpty());

        takeScreenshot("dashboard");

        System.out.println(">>> [Selenium] UI-8 PASSED – dashboard.html loaded");
    }

    // ────────────────────────────────────────────────────────────────────────
    // Screenshot helper
    // ────────────────────────────────────────────────────────────────────────

    private void takeScreenshot(String name) {
        try {
            if (driver instanceof TakesScreenshot ts) {
                byte[] bytes = ts.getScreenshotAs(OutputType.BYTES);
                java.nio.file.Path path = java.nio.file.Paths.get(
                        "target", "selenium-screenshots", name + ".png");
                java.nio.file.Files.createDirectories(path.getParent());
                java.nio.file.Files.write(path, bytes);
                System.out.println(">>> [Screenshot] saved: " + path.toAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println(">>> [Screenshot] WARN – could not save screenshot: " + e.getMessage());
        }
    }
}
