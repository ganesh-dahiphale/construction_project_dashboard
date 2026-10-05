package com.example.dashboard.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class NavBar {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By homeLink = By.xpath("//a[contains(@class, 'nav-link') and contains(., 'Home')]");
    private final By dashboardLink = By.xpath("//a[contains(@class, 'nav-link') and contains(., 'Dashboard')]");
    private final By tasksLink = By.xpath("//a[contains(@class, 'nav-link') and contains(., 'Tasks')]");
    private final By statusLink = By.xpath("//a[contains(@class, 'nav-link') and contains(., 'Status')]");
    private final By alertsLink = By.xpath("//a[contains(@class, 'nav-link') and contains(., 'Alerts')]");
    private final By addTaskBtn = By.xpath("//a[contains(., 'Add Task')]");
    private final By adminBtn = By.xpath("//a[contains(., 'Admin')]");
    private final By logoutBtn = By.cssSelector("form[action*='/logout'] button");
    private final By userBadge = By.cssSelector(".navbar span.badge");

    public NavBar(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void clickHome() {
        click(homeLink);
    }

    public void clickDashboard() {
        click(dashboardLink);
    }

    public void clickTasks() {
        click(tasksLink);
    }

    public void clickStatus() {
        click(statusLink);
    }

    public void clickAlerts() {
        click(alertsLink);
    }

    public void clickAddTask() {
        click(addTaskBtn);
    }

    public void clickAdmin() {
        click(adminBtn);
    }

    public void logout() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(logoutBtn));
        try {
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public boolean isUserLoggedIn() {
        try {
            return driver.findElements(userBadge).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public String getLoggedInUsername() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(userBadge)).getText().trim();
    }

    private void click(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        try {
            element.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }
}
