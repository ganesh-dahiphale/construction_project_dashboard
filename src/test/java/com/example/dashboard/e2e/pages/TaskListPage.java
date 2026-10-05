package com.example.dashboard.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class TaskListPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    private final By taskTable = By.cssSelector("table");
    private final By taskRows = By.cssSelector("table tbody tr");
    private final By successAlert = By.cssSelector(".alert-success");
    private final By addTaskBtn = By.xpath("//a[contains(@href, '/tasks/new') and contains(., 'New Task')]");

    public TaskListPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public TaskListPage open() {
        driver.get(baseUrl + "/tasks");
        wait.until(ExpectedConditions.presenceOfElementLocated(taskTable));
        return this;
    }

    public boolean isSuccessMessageDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(successAlert)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getSuccessMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(successAlert)).getText().trim();
    }

    public int getTaskRowCount() {
        try {
            return driver.findElements(taskRows).size();
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isTaskPresent(String taskTitle) {
        try {
            By taskLocator = By.xpath("//table//tr[td[contains(., '" + taskTitle + "')]]");
            return wait.until(ExpectedConditions.presenceOfElementLocated(taskLocator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getTaskStatus(String taskTitle) {
        By statusBadge = By.xpath("//table//tr[td[contains(., '" + taskTitle + "')]]//span[contains(@class, 'badge')]");
        return wait.until(ExpectedConditions.visibilityOfElementLocated(statusBadge)).getText().trim();
    }

    public String getTaskProgress(String taskTitle) {
        By progressBar = By.xpath("//table//tr[td[contains(., '" + taskTitle + "')]]//div[contains(@class, 'progress-bar')]");
        return wait.until(ExpectedConditions.visibilityOfElementLocated(progressBar)).getText().trim();
    }

    public void clickEditTask(String taskTitle) {
        By editBtn = By.xpath("//table//tr[td[contains(., '" + taskTitle + "')]]//a[contains(@href, '/edit')]");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(editBtn));
        try {
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public void clickEditFirstTask() {
        By firstEditBtn = By.xpath("(//table//tr//a[contains(@href, '/edit')])[1]");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(firstEditBtn));
        try {
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }
}
