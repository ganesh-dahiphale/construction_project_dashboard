package com.example.dashboard.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class TaskFormPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    private final By titleInput = By.id("title");
    private final By projectSelect = By.id("project");
    private final By statusSelect = By.id("status");
    private final By percentInput = By.id("percentComplete");
    private final By dueDateInput = By.id("dueDate");
    private final By remarksTextarea = By.id("remarks");
    private final By submitBtn = By.cssSelector("#taskForm #submitBtn, #taskForm button[type='submit'], #submitBtn");
    private final By invalidFeedback = By.cssSelector(".invalid-feedback, .alert-danger");

    public TaskFormPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public TaskFormPage open() {
        driver.get(baseUrl + "/tasks/new");
        wait.until(ExpectedConditions.visibilityOfElementLocated(titleInput));
        return this;
    }

    public void fillForm(String title, String status, int percentComplete, String dueDate, String remarks) {
        if (title != null) {
            WebElement t = wait.until(ExpectedConditions.visibilityOfElementLocated(titleInput));
            t.clear();
            t.sendKeys(title);
        }

        // Select first valid enabled project option
        WebElement projElem = wait.until(ExpectedConditions.visibilityOfElementLocated(projectSelect));
        Select projSelect = new Select(projElem);
        for (WebElement opt : projSelect.getOptions()) {
            if (opt.isEnabled() && opt.getAttribute("value") != null && !opt.getAttribute("value").trim().isEmpty()) {
                projSelect.selectByValue(opt.getAttribute("value"));
                break;
            }
        }

        if (status != null) {
            Select st = new Select(driver.findElement(statusSelect));
            st.selectByValue(status);
        }

        WebElement p = wait.until(ExpectedConditions.visibilityOfElementLocated(percentInput));
        p.clear();
        p.sendKeys(String.valueOf(percentComplete));

        if (dueDate != null && !dueDate.isEmpty()) {
            WebElement d = wait.until(ExpectedConditions.visibilityOfElementLocated(dueDateInput));
            try {
                d.clear();
                d.sendKeys(dueDate);
            } catch (Exception ignored) {}
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input', {bubbles: true})); arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
                    d, dueDate);
        }

        if (remarks != null) {
            WebElement r = wait.until(ExpectedConditions.visibilityOfElementLocated(remarksTextarea));
            r.clear();
            r.sendKeys(remarks);
        }
    }

    public void submit() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(submitBtn));
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", btn);
        } catch (Exception e) {
            btn.click();
        }
    }

    public boolean hasValidationErrors() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(invalidFeedback)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
