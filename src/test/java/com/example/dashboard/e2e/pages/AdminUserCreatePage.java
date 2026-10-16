package com.example.dashboard.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AdminUserCreatePage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    private final By fullNameInput = By.cssSelector("[data-testid='user-fullname-input']");
    private final By emailInput = By.cssSelector("[data-testid='user-email-input']");
    private final By usernameInput = By.cssSelector("[data-testid='user-username-input']");
    private final By roleSelect = By.cssSelector("[data-testid='user-role-select']");
    private final By passwordInput = By.cssSelector("[data-testid='user-password-input']");
    private final By confirmPasswordInput = By.cssSelector("[data-testid='user-confirm-password-input']");
    private final By submitBtn = By.cssSelector("[data-testid='user-submit-btn']");

    public AdminUserCreatePage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public AdminUserCreatePage open() {
        driver.get(baseUrl + "/admin/users/new");
        wait.until(ExpectedConditions.visibilityOfElementLocated(fullNameInput));
        return this;
    }

    public AdminUsersPage registerUser(String fullName, String email, String username, String role, String password, String confirmPassword) {
        WebElement fn = wait.until(ExpectedConditions.visibilityOfElementLocated(fullNameInput));
        fn.clear();
        fn.sendKeys(fullName);

        WebElement em = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        em.clear();
        em.sendKeys(email);

        WebElement un = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
        un.clear();
        un.sendKeys(username);

        WebElement rs = wait.until(ExpectedConditions.visibilityOfElementLocated(roleSelect));
        Select select = new Select(rs);
        select.selectByValue(role);

        WebElement pwd = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        pwd.clear();
        pwd.sendKeys(password);

        WebElement cpwd = wait.until(ExpectedConditions.visibilityOfElementLocated(confirmPasswordInput));
        cpwd.clear();
        cpwd.sendKeys(confirmPassword);

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitBtn));
        try {
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }

        return new AdminUsersPage(driver, baseUrl);
    }
}
