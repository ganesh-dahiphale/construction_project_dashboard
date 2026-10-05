package com.example.dashboard.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    private final By usernameInput = By.id("username");
    private final By passwordInput = By.id("password");
    private final By submitButton = By.cssSelector("button[type='submit']");
    private final By errorAlert = By.cssSelector(".alert-danger");
    private final By logoutAlert = By.cssSelector(".alert-info");

    public LoginPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public LoginPage open() {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
        return this;
    }

    public void login(String username, String password) {
        WebElement u = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
        u.clear();
        u.sendKeys(username);

        WebElement p = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        p.clear();
        p.sendKeys(password);

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        btn.click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(errorAlert),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".navbar"))
        ));
    }

    public boolean isErrorAlertDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(errorAlert)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorAlert)).getText();
    }
}
