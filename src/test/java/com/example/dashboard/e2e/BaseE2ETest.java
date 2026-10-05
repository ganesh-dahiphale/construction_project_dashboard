package com.example.dashboard.e2e;

import com.example.dashboard.e2e.extension.ScreenshotOnFailureExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

@Tag("e2e")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@ExtendWith(ScreenshotOnFailureExtension.class)
public abstract class BaseE2ETest {

    @LocalServerPort
    protected int localPort;

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected static Properties testProperties = new Properties();

    protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    protected static final Duration POLL_INTERVAL = Duration.ofMillis(250);

    @BeforeAll
    public static void loadTestData() {
        try (InputStream is = BaseE2ETest.class.getClassLoader().getResourceAsStream("e2e-testdata.properties")) {
            if (is != null) {
                testProperties.load(is);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load e2e-testdata.properties: " + e.getMessage());
        }
    }

    @BeforeEach
    public void setUpBrowser() {
        ChromeOptions options = new ChromeOptions();

        boolean isHeadless = !"false".equalsIgnoreCase(System.getProperty("headless", "true"));
        if (isHeadless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1366,900");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, DEFAULT_TIMEOUT, POLL_INTERVAL);
    }

    @AfterEach
    public void tearDownBrowser() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.err.println("Warning during driver quit: " + e.getMessage());
            } finally {
                driver = null;
            }
        }
    }

    public WebDriver getDriver() {
        return this.driver;
    }

    public WebDriverWait getWait() {
        return this.wait;
    }

    public String getBaseUrl() {
        String systemBaseUrl = System.getProperty("base.url");
        if (systemBaseUrl != null && !systemBaseUrl.trim().isEmpty()) {
            return systemBaseUrl.endsWith("/") ? systemBaseUrl.substring(0, systemBaseUrl.length() - 1) : systemBaseUrl;
        }
        return "http://localhost:" + localPort;
    }

    public void navigateTo(String path) {
        String url = getBaseUrl() + (path.startsWith("/") ? path : "/" + path);
        driver.get(url);
    }

    public String getProperty(String key, String defaultValue) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp;
        }
        return testProperties.getProperty(key, defaultValue);
    }

    public WebElement waitForElementVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForElementClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public boolean waitForTextPresent(By locator, String expectedText) {
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, expectedText));
    }

    public void clickElement(By locator) {
        WebElement element = waitForElementClickable(locator);
        try {
            element.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    public void typeText(By locator, String text) {
        WebElement element = waitForElementVisible(locator);
        element.clear();
        element.sendKeys(text);
    }
}
