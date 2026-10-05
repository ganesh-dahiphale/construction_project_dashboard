package com.example.dashboard.e2e;

import com.example.dashboard.e2e.pages.AlertsPage;
import com.example.dashboard.e2e.pages.DashboardPage;
import com.example.dashboard.e2e.pages.LoginPage;
import com.example.dashboard.e2e.pages.StatusPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
public class DrilldownAlertsJourneyTest extends BaseE2ETest {

    @Test
    @DisplayName("J5: Manager drills down into delayed tasks and inspects overdue alert badge on /alerts")
    void testDrilldownAndAlerts() {
        LoginPage loginPage = new LoginPage(driver, getBaseUrl());
        loginPage.open();
        loginPage.login(getProperty("e2e.manager.username", "manager"), getProperty("e2e.manager.password", "manager123"));

        // 1. Open delayed status drill-down
        StatusPage statusPage = new StatusPage(driver, getBaseUrl());
        statusPage.open("delayed");
        assertThat(driver.getCurrentUrl()).contains("/status/delayed");
        assertThat(statusPage.getTaskRowCount()).isGreaterThanOrEqualTo(1);

        // 2. Open /alerts view and verify overdue badge
        AlertsPage alertsPage = new AlertsPage(driver, getBaseUrl());
        alertsPage.open();
        assertThat(driver.getCurrentUrl()).contains("/alerts");
        assertThat(alertsPage.hasOverdueAlerts()).isTrue();
        assertThat(alertsPage.getOverdueBadgeCount()).isGreaterThanOrEqualTo(1);
    }
}
