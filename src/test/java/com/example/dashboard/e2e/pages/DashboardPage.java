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

    private final By totalTasksCard = By.xpath("//div[contains(@class, 'card')]//p[contains(text(), 'Total Tasks')]/following-sibling::h2 | //div[contains(@class, 'card')]//span[contains(text(), 'Total')]/following-sibling::h3 | //h2[contains(@class, 'display') or contains(@class, 'fw-bold')]");
    private final By kpiCards = By.cssSelector(".card h2, .card h3, .card .display-6");
    private final By keywordInput = By.name("keyword");
    private final By statusSelect = By.name("status");
    private final By filterSubmitBtn = By.cssSelector("form button[type='submit']");
    private final By taskTableRows = By.cssSelector("table tbody tr");
    private final By delayedCardLink = By.xpath("//a[contains(@href, '/status/delayed')]");

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
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(keywordInput));
        input.clear();
        input.sendKeys(keyword);
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(filterSubmitBtn));
        try {
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public void filterByStatus(String statusValue) {
        WebElement selectElem = wait.until(ExpectedConditions.visibilityOfElementLocated(statusSelect));
        Select select = new Select(selectElem);
        select.selectByValue(statusValue);
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(filterSubmitBtn));
        try {
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
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
