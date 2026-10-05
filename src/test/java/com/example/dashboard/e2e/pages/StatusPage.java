package com.example.dashboard.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class StatusPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    private final By taskTable = By.cssSelector("table");
    private final By taskRows = By.cssSelector("table tbody tr");
    private final By pageHeader = By.cssSelector("h1, h2");

    public StatusPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public StatusPage open(String statusPath) {
        String path = statusPath != null && !statusPath.isEmpty() ? "/status/" + statusPath : "/status";
        driver.get(baseUrl + path);
        wait.until(ExpectedConditions.presenceOfElementLocated(pageHeader));
        return this;
    }

    public int getTaskRowCount() {
        try {
            return driver.findElements(taskRows).size();
        } catch (Exception e) {
            return 0;
        }
    }

    public String getHeaderTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeader)).getText();
    }
}
