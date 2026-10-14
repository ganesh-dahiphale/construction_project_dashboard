package com.example.dashboard.e2e;

import com.example.dashboard.e2e.pages.DashboardPage;
import com.example.dashboard.e2e.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
public class SearchDashboardJourneyTest extends BaseE2ETest {

    @Test
    @DisplayName("J4: Manager opens /dashboard, KPI indicator cards display positive metrics, search and status filtering filter results table")
    void testDashboardSearchAndIndicators() {
        LoginPage loginPage = new LoginPage(driver, getBaseUrl());
        loginPage.open();
        loginPage.login(getProperty("e2e.manager.username", "manager"), getProperty("e2e.manager.password", "manager123"));

        DashboardPage dashboardPage = new DashboardPage(driver, getBaseUrl());
        dashboardPage.open();

        // Assert KPI cards are present and loaded
        assertThat(dashboardPage.areKpiCardsPresent()).isTrue();
        int initialRowCount = dashboardPage.getTaskRowCount();
        assertThat(initialRowCount).isGreaterThan(0);

        // Filter by valid keyword
        dashboardPage.searchByKeyword("Concrete");
        int filteredRowCount = dashboardPage.getTaskRowCount();
        assertThat(filteredRowCount).isGreaterThan(0);

        // Filter by non-existent keyword returns 0 or empty state
        dashboardPage.searchByKeyword("NON_EXISTENT_KEYWORD_XYZ_999");
        assertThat(dashboardPage.getTaskRowCount()).isEqualTo(0);

        // Filter by status dropdown
        dashboardPage.open();
        dashboardPage.filterByStatus("COMPLETED");
        assertThat(dashboardPage.getTaskRowCount()).isGreaterThanOrEqualTo(1);
    }
}
