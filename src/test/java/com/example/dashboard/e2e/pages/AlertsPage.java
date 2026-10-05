package com.example.dashboard.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AlertsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    private final By pageHeader = By.xpath("//h1[contains(., 'Alerts') or contains(., 'Exception')]");
    private final By overdueBadges = By.xpath("//span[contains(@class, 'badge') and (contains(., 'Overdue') or contains(., 'OVERDUE') or contains(., 'Delayed'))]");
    private final By alertTables = By.cssSelector("table");

    public AlertsPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public AlertsPage open() {
        driver.get(baseUrl + "/alerts");
        wait.until(ExpectedConditions.presenceOfElementLocated(alertTables));
        return this;
    }

    public boolean hasOverdueAlerts() {
        try {
            return driver.findElements(overdueBadges).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public int getOverdueBadgeCount() {
        try {
            return driver.findElements(overdueBadges).size();
        } catch (Exception e) {
            return 0;
        }
    }
}
