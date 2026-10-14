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

    private final By homeLink = By.cssSelector(".navbar a[href='/']");
    private final By dashboardLink = By.cssSelector(".navbar a[href*='/dashboard']");
    private final By tasksLink = By.cssSelector(".navbar a[href$='/tasks'], .navbar a[href='/tasks']");
    private final By statusLink = By.cssSelector(".navbar a[href$='/status'], .navbar a[href='/status']");
    private final By alertsLink = By.cssSelector(".navbar a[href*='/alerts']");
    private final By addTaskBtn = By.cssSelector(".navbar a[href*='/tasks/new']");
    private final By adminBtn = By.cssSelector(".navbar a[href*='/admin']");
    private final By logoutBtn = By.cssSelector(".navbar form button, button[title='Logout']");
    private final By userBadge = By.cssSelector(".navbar .badge");

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
            return wait.until(ExpectedConditions.visibilityOfElementLocated(userBadge)).isDisplayed();
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
