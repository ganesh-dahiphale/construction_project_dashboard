package com.example.dashboard.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AdminUsersPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    private final By addUserBtn = By.cssSelector("[data-testid='add-user-btn']");
    private final By searchInput = By.cssSelector("[data-testid='user-search-input']");
    private final By searchBtn = By.cssSelector("[data-testid='user-search-btn']");
    private final By roleFilter = By.cssSelector("[data-testid='user-role-filter']");
    private final By statusFilter = By.cssSelector("[data-testid='user-status-filter']");
    private final By flashSuccess = By.cssSelector("[data-testid='flash-success']");
    private final By flashError = By.cssSelector("[data-testid='flash-error']");

    public AdminUsersPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public AdminUsersPage open() {
        driver.get(baseUrl + "/admin/users");
        wait.until(ExpectedConditions.visibilityOfElementLocated(addUserBtn));
        return this;
    }

    public AdminUserCreatePage clickAddUser() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(addUserBtn));
        btn.click();
        return new AdminUserCreatePage(driver, baseUrl);
    }

    public boolean isUserPresent(String username) {
        try {
            By userLocator = By.cssSelector("[data-testid='user-username-" + username + "']");
            return wait.until(ExpectedConditions.visibilityOfElementLocated(userLocator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserStatus(String username) {
        By statusLocator = By.cssSelector("[data-testid='user-status-" + username + "']");
        return wait.until(ExpectedConditions.visibilityOfElementLocated(statusLocator)).getText().trim();
    }

    public void changeStatus(String username, String newStatus) {
        By selectLocator = By.cssSelector("[data-testid='status-select-" + username + "']");
        WebElement selectElem = wait.until(ExpectedConditions.visibilityOfElementLocated(selectLocator));
        Select select = new Select(selectElem);
        select.selectByValue(newStatus);

        By submitLocator = By.cssSelector("[data-testid='status-submit-btn-" + username + "']");
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(submitLocator));
        try {
            submitBtn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(flashSuccess));
    }

    public void deleteUser(String username) {
        By deleteBtnLocator = By.cssSelector("[data-testid='delete-user-btn-" + username + "']");
        WebElement deleteBtn = wait.until(ExpectedConditions.elementToBeClickable(deleteBtnLocator));
        try {
            deleteBtn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);
        }

        By confirmBtnLocator = By.cssSelector("[data-testid='confirm-delete-btn-" + username + "']");
        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(confirmBtnLocator));
        try {
            confirmBtn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmBtn);
        }

        By userRowLocator = By.cssSelector("[data-testid='user-username-" + username + "']");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(userRowLocator));
    }

    public boolean isSuccessFlashDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(flashSuccess)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
