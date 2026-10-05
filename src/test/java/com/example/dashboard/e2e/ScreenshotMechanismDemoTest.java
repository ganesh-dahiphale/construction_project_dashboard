package com.example.dashboard.e2e;

import com.example.dashboard.e2e.pages.LoginPage;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
@Tag("demo-failure")
public class ScreenshotMechanismDemoTest extends BaseE2ETest {

    @Test
    @DisplayName("Demo: Deliberate failure to trigger and verify ScreenshotOnFailureExtension")
    void testDeliberateFailureForScreenshotCapture() {
        boolean runDemo = Boolean.getBoolean("demo.failure")
                || "true".equalsIgnoreCase(System.getProperty("demo.failure"));
        Assumptions.assumeTrue(runDemo, "Demo failure test skipped because -Ddemo.failure is not set to true");

        LoginPage loginPage = new LoginPage(driver, getBaseUrl());
        loginPage.open();

        // Intentionally failing assertion to trigger ScreenshotOnFailureExtension
        assertThat(driver.getTitle()).isEqualTo("THIS TITLE WILL DELIBERATELY FAIL TO PROVE SCREENSHOT ON FAILURE MECHANISM");
    }
}
