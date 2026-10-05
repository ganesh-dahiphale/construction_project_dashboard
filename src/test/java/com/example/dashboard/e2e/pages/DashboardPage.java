package com.example.dashboard.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    private final By totalTasksCard = By.id("totalTasksCount");
    private final By kpiCards = By.cssSelector(".metric-card");
    private final By keywordInput = By.id("keyword");
    private final By statusSelect = By.id("status");
    private final By filterSubmitBtn = By.id("filterSubmitBtn");
    private final By taskTableRows = By.cssSelector("#tasksTable tbody tr");
    private final By emptyState = By.id("emptyState");
    private final By delayedCardLink = By.cssSelector("#delayedCardLink, a[href*='/status/delayed']");

    public DashboardPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public DashboardPage open() {
        driver.get(baseUrl + "/dashboard");
        wait.until(ExpectedConditions.presenceOfElementLocated(keywordInput));
        return this;
    }

    public boolean areKpiCardsPresent() {
        try {
            return driver.findElements(kpiCards).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void searchByKeyword(String keyword) {
        WebElement form = driver.findElement(By.cssSelector("form[action*='/dashboard']"));
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(keywordInput));
        input.clear();
        if (keyword != null && !keyword.isEmpty()) {
            input.sendKeys(keyword);
        }
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(filterSubmitBtn));
        try {
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
        try {
            wait.until(ExpectedConditions.stalenessOf(form));
        } catch (Exception ignored) {}
        wait.until(ExpectedConditions.presenceOfElementLocated(keywordInput));
    }

    public void filterByStatus(String statusValue) {
        WebElement form = driver.findElement(By.cssSelector("form[action*='/dashboard']"));
        WebElement selectElem = wait.until(ExpectedConditions.visibilityOfElementLocated(statusSelect));
        Select select = new Select(selectElem);
        select.selectByValue(statusValue);
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(filterSubmitBtn));
        try {
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
        try {
            wait.until(ExpectedConditions.stalenessOf(form));
        } catch (Exception ignored) {}
        wait.until(ExpectedConditions.presenceOfElementLocated(statusSelect));
    }

    public void searchAndFilter(String keyword, String statusValue) {
        WebElement form = driver.findElement(By.cssSelector("form[action*='/dashboard']"));
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(keywordInput));
        input.clear();
        if (keyword != null && !keyword.isEmpty()) {
            input.sendKeys(keyword);
        }
        if (statusValue != null && !statusValue.isEmpty()) {
            WebElement selectElem = wait.until(ExpectedConditions.visibilityOfElementLocated(statusSelect));
            Select select = new Select(selectElem);
            select.selectByValue(statusValue);
        }
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(filterSubmitBtn));
        try {
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
        try {
            wait.until(ExpectedConditions.stalenessOf(form));
        } catch (Exception ignored) {}
        wait.until(ExpectedConditions.presenceOfElementLocated(keywordInput));
    }

    public int getTaskRowCount() {
        try {
            return driver.findElements(taskTableRows).size();
        } catch (Exception e) {
            return 0;
        }
    }

    public void clickDelayedTasksCard() {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(delayedCardLink));
        try {
            link.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
        }
    }
}
