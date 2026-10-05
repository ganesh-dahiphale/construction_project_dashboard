package com.example.dashboard.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class TaskEditPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By statusSelect = By.id("status");
    private final By percentInput = By.id("percentComplete");
    private final By remarksTextarea = By.id("remarks");
    private final By submitBtn = By.id("updateTaskSubmitBtn");

    public TaskEditPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void updateStatus(String status) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(statusSelect));
        Select select = new Select(element);
        select.selectByValue(status);
    }

    public void updatePercentComplete(int percent) {
        WebElement p = wait.until(ExpectedConditions.visibilityOfElementLocated(percentInput));
        p.clear();
        p.sendKeys(String.valueOf(percent));
    }

    public void updateRemarks(String remarks) {
        WebElement r = wait.until(ExpectedConditions.visibilityOfElementLocated(remarksTextarea));
        r.clear();
        r.sendKeys(remarks);
    }

    public void submit() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitBtn));
        try {
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }
}
