package com.example.dashboard.e2e.extension;

import com.example.dashboard.e2e.BaseE2ETest;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ScreenshotOnFailureExtension implements TestWatcher {

    private static final String SCREENSHOTS_DIR = "target/screenshots";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        Object testInstance = context.getRequiredTestInstance();
        if (testInstance instanceof BaseE2ETest) {
            WebDriver driver = ((BaseE2ETest) testInstance).getDriver();
            if (driver != null && driver instanceof TakesScreenshot) {
                try {
                    Path dir = Paths.get(SCREENSHOTS_DIR);
                    if (!Files.exists(dir)) {
                        Files.createDirectories(dir);
                    }

                    String className = context.getRequiredTestClass().getSimpleName();
                    String methodName = context.getRequiredTestMethod().getName();
                    String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                    String fileName = String.format("%s_%s_%s.png", className, methodName, timestamp);
                    Path destination = dir.resolve(fileName);

                    File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                    Files.copy(screenshotFile.toPath(), destination);

                    String currentUrl = "";
                    String pageTitle = "";
                    try {
                        currentUrl = driver.getCurrentUrl();
                        pageTitle = driver.getTitle();
                    } catch (Exception ignored) {}

                    System.err.println("=================================================================");
                    System.err.println("❌ TEST FAILED: " + className + "." + methodName);
                    System.err.println("📸 Failure screenshot captured at: " + destination.toAbsolutePath());
                    System.err.println("🌐 Failed Page URL: " + currentUrl);
                    System.err.println("📄 Failed Page Title: " + pageTitle);
                    System.err.println("💥 Failure Cause: " + (cause != null ? cause.getMessage() : "Unknown"));
                    System.err.println("=================================================================");
                } catch (IOException e) {
                    System.err.println("Failed to capture screenshot on test failure: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {}

    @Override
    public void testSuccessful(ExtensionContext context) {}

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {}
}
