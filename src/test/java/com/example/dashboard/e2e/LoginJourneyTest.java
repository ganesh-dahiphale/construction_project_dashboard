package com.example.dashboard.e2e;

import com.example.dashboard.e2e.pages.LoginPage;
import com.example.dashboard.e2e.pages.NavBar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
public class LoginJourneyTest extends BaseE2ETest {

    @Test
    @DisplayName("J1-1: Invalid login with incorrect password displays error alert")
    void testInvalidLoginShowsError() {
        LoginPage loginPage = new LoginPage(driver, getBaseUrl());
        loginPage.open();
        loginPage.login(getProperty("e2e.engineer.username", "engineer"), "wrongpassword123");

        assertThat(loginPage.isErrorAlertDisplayed()).isTrue();
        assertThat(driver.getCurrentUrl()).contains("/login?error=true");
    }

    @Test
    @DisplayName("J1-2: Valid login as Engineer navigates to tasks view and displays username")
    void testEngineerLoginSuccess() {
        LoginPage loginPage = new LoginPage(driver, getBaseUrl());
        loginPage.open();
        loginPage.login(getProperty("e2e.engineer.username", "engineer"), getProperty("e2e.engineer.password", "engineer123"));

        NavBar navBar = new NavBar(driver);
        assertThat(navBar.isUserLoggedIn()).isTrue();
        assertThat(navBar.getLoggedInUsername()).containsIgnoringCase("engineer");

        // Verify engineer navbar only shows accessible pages (Tasks, Add Task) and hides Dashboard, Status, Alerts, Admin
        assertThat(driver.findElements(By.cssSelector("[data-testid='nav-dashboard']"))).isEmpty();
        assertThat(driver.findElements(By.cssSelector("[data-testid='nav-status']"))).isEmpty();
        assertThat(driver.findElements(By.cssSelector("[data-testid='nav-alerts']"))).isEmpty();
        assertThat(driver.findElements(By.cssSelector("[data-testid='nav-admin-section']"))).isEmpty();
        assertThat(driver.findElements(By.cssSelector("[data-testid='nav-tasks']"))).isNotEmpty();
        assertThat(driver.findElements(By.cssSelector("[data-testid='nav-add-task']"))).isNotEmpty();
    }

    @Test
    @DisplayName("J1-3: Role Authorization - Engineer attempting to access /dashboard and /admin/projects receives 403 Forbidden")
    void testEngineerForbiddenPages() {
        LoginPage loginPage = new LoginPage(driver, getBaseUrl());
        loginPage.open();
        loginPage.login(getProperty("e2e.engineer.username", "engineer"), getProperty("e2e.engineer.password", "engineer123"));

        // Engineer accessing /dashboard
        navigateTo("/dashboard");
        waitForElementVisible(By.tagName("h1"));
        assertThat(driver.getTitle()).containsIgnoringCase("Access Denied");
        assertThat(driver.findElement(By.tagName("body")).getText()).contains("403");

        // Engineer accessing /admin/projects
        navigateTo("/admin/projects");
        waitForElementVisible(By.tagName("h1"));
        assertThat(driver.getTitle()).containsIgnoringCase("Access Denied");
        assertThat(driver.findElement(By.tagName("body")).getText()).contains("403");
    }

    @Test
    @DisplayName("J1-4: Valid login as Manager accesses Dashboard, Status, Alerts, and Tasks")
    void testManagerRoleAccess() {
        LoginPage loginPage = new LoginPage(driver, getBaseUrl());
        loginPage.open();
        loginPage.login(getProperty("e2e.manager.username", "manager"), getProperty("e2e.manager.password", "manager123"));

        NavBar navBar = new NavBar(driver);
        assertThat(navBar.isUserLoggedIn()).isTrue();

        // Verify manager cannot see Admin section, but sees Dashboard, Status, Alerts, Tasks
        assertThat(driver.findElements(By.cssSelector("[data-testid='nav-admin-section']"))).isEmpty();
        assertThat(driver.findElements(By.cssSelector("[data-testid='nav-dashboard']"))).isNotEmpty();
        assertThat(driver.findElements(By.cssSelector("[data-testid='nav-status']"))).isNotEmpty();
        assertThat(driver.findElements(By.cssSelector("[data-testid='nav-alerts']"))).isNotEmpty();

        navBar.clickDashboard();
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/dashboard"));
        assertThat(driver.getCurrentUrl()).contains("/dashboard");

        navBar.clickStatus();
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/status"));
        assertThat(driver.getCurrentUrl()).contains("/status");

        navBar.clickAlerts();
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/alerts"));
        assertThat(driver.getCurrentUrl()).contains("/alerts");
    }

    @Test
    @DisplayName("J1-5: Valid login as Admin has access to project administration (/admin/projects)")
    void testAdminRoleAccess() {
        LoginPage loginPage = new LoginPage(driver, getBaseUrl());
        loginPage.open();
        loginPage.login(getProperty("e2e.admin.username", "admin"), getProperty("e2e.admin.password", "admin123"));

        NavBar navBar = new NavBar(driver);
        assertThat(navBar.isUserLoggedIn()).isTrue();

        navigateTo("/admin/projects");
        assertThat(driver.getCurrentUrl()).contains("/admin/projects");
        assertThat(driver.findElement(By.tagName("h1")).getText()).contains("Project Administration");
    }
}
