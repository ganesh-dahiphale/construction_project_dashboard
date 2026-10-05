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
    private final By submitBtn = By.id("saveTaskBtn");
    private final By invalidFeedback = By.cssSelector(".invalid-feedback");

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

        // Select first available project if options exist
        WebElement projElem = driver.findElement(projectSelect);
        Select projSelect = new Select(projElem);
        if (!projSelect.getOptions().isEmpty()) {
            projSelect.selectByIndex(0);
        }

        if (status != null) {
            Select st = new Select(driver.findElement(statusSelect));
            st.selectByValue(status);
        }

        WebElement p = driver.findElement(percentInput);
        p.clear();
        p.sendKeys(String.valueOf(percentComplete));

        if (dueDate != null && !dueDate.isEmpty()) {
            WebElement d = driver.findElement(dueDateInput);
            d.clear();
            d.sendKeys(dueDate);
        }

        if (remarks != null) {
            WebElement r = driver.findElement(remarksTextarea);
            r.clear();
            r.sendKeys(remarks);
        }
    }

    public void submit() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitBtn));
        try {
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public boolean hasValidationErrors() {
        try {
            return driver.findElements(invalidFeedback).stream().anyMatch(WebElement::isDisplayed);
        } catch (Exception e) {
            return false;
        }
    }
}
