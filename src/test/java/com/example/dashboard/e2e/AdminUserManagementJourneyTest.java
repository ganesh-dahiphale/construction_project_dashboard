package com.example.dashboard.e2e;

import com.example.dashboard.e2e.pages.AdminUserCreatePage;
import com.example.dashboard.e2e.pages.AdminUsersPage;
import com.example.dashboard.e2e.pages.LoginPage;
import com.example.dashboard.e2e.pages.NavBar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
public class AdminUserManagementJourneyTest extends BaseE2ETest {

    @Test
    @DisplayName("J6: Admin User Management Journey - Register, Login, Inactivate, Reactivate, Delete, and Access Control")
    void testAdminUserManagementLifecycle() {
        String baseUrl = getBaseUrl();
        LoginPage loginPage = new LoginPage(driver, baseUrl);
        NavBar navBar = new NavBar(driver);

        String uniqueUsername = "eng_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUsername + "@construction.local";
        String engineerPassword = "Password123";
        String engineerFullName = "Site Engineer " + uniqueUsername;

        // Step 1: Admin logs in
        logoutAndOpenLogin();
        loginPage.login("admin", "admin123");
        assertThat(navBar.isUserLoggedIn()).isTrue();
        assertThat(navBar.getLoggedInUsername()).containsIgnoringCase("admin");

        // Verify Admin sees admin dropdown and Users link
        WebElement adminNav = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='nav-admin-section']")));
        assertThat(adminNav.isDisplayed()).isTrue();

        // Step 2: Navigate to Admin Users page and click Add User
        AdminUsersPage usersPage = new AdminUsersPage(driver, baseUrl).open();
        AdminUserCreatePage createPage = usersPage.clickAddUser();

        // Step 3: Admin registers new engineer
        createPage.registerUser(
                engineerFullName,
                uniqueEmail,
                uniqueUsername,
                "ENGINEER",
                engineerPassword,
                engineerPassword
        );

        // Step 4: Verify registered engineer appears in table with ACTIVE status
        usersPage.open();
        assertThat(usersPage.isUserPresent(uniqueUsername)).isTrue();
        assertThat(usersPage.getUserStatus(uniqueUsername)).containsIgnoringCase("Active");

        // Step 5: Admin logs out
        logoutAndOpenLogin();

        // Step 6: Newly created engineer logs in successfully
        loginPage.login(uniqueUsername, engineerPassword);
        assertThat(navBar.isUserLoggedIn()).isTrue();
        assertThat(navBar.getLoggedInUsername()).containsIgnoringCase(uniqueUsername);

        // Step 7: Non-admin engineer does not see admin section or Users link
        boolean hasAdminNav = driver.findElements(By.cssSelector("[data-testid='nav-admin-section']")).size() > 0;
        assertThat(hasAdminNav).isFalse();
        boolean hasUsersLink = driver.findElements(By.cssSelector("[data-testid='nav-users']")).size() > 0;
        assertThat(hasUsersLink).isFalse();

        // Direct navigation to /admin/users returns 403 Forbidden
        driver.get(baseUrl + "/admin/users");
        String pageSource = driver.getPageSource();
        assertThat(pageSource.contains("403") || pageSource.contains("Forbidden") || driver.getCurrentUrl().contains("login")).isTrue();

        // Step 8: Engineer logs out
        logoutAndOpenLogin();

        // Step 9: Admin logs in again
        loginPage.login("admin", "admin123");
        assertThat(navBar.isUserLoggedIn()).isTrue();

        // Step 10: Admin sets engineer status to INACTIVE
        usersPage.open();
        usersPage.changeStatus(uniqueUsername, "INACTIVE");
        assertThat(usersPage.getUserStatus(uniqueUsername)).containsIgnoringCase("Inactive");

        // Step 11: Admin logs out
        logoutAndOpenLogin();

        // Step 12: Inactive engineer attempts to log in -> blocked with inactive message
        loginPage.login(uniqueUsername, engineerPassword);
        assertThat(loginPage.isErrorAlertDisplayed()).isTrue();
        assertThat(loginPage.getErrorMessage()).containsIgnoringCase("Account is inactive or on leave");

        // Step 13: Admin logs in
        logoutAndOpenLogin();
        loginPage.login("admin", "admin123");
        assertThat(navBar.isUserLoggedIn()).isTrue();

        // Step 14: Admin sets status back to ACTIVE
        usersPage.open();
        usersPage.changeStatus(uniqueUsername, "ACTIVE");
        assertThat(usersPage.getUserStatus(uniqueUsername)).containsIgnoringCase("Active");

        // Step 15: Admin deletes the user via confirmation modal
        usersPage.deleteUser(uniqueUsername);
        assertThat(usersPage.isUserPresent(uniqueUsername)).isFalse();

        // Step 16: Admin logs out
        logoutAndOpenLogin();

        // Step 17: Deleted engineer cannot log in
        loginPage.login(uniqueUsername, engineerPassword);
        assertThat(loginPage.isErrorAlertDisplayed()).isTrue();
        assertThat(loginPage.getErrorMessage()).containsIgnoringCase("Invalid username or password");
    }

    private void logoutAndOpenLogin() {
        try {
            driver.manage().deleteAllCookies();
        } catch (Exception ignored) {
        }
        driver.get(getBaseUrl() + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
    }
}
