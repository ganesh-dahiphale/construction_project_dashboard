package com.example.dashboard.e2e;

import com.example.dashboard.e2e.pages.LoginPage;
import com.example.dashboard.e2e.pages.NavBar;
import com.example.dashboard.e2e.pages.TaskFormPage;
import com.example.dashboard.e2e.pages.TaskListPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
public class AddTaskJourneyTest extends BaseE2ETest {

    @Test
    @DisplayName("J2-1: Submitting empty task form displays validation errors")
    void testEmptyTaskFormShowsValidationErrors() {
        LoginPage loginPage = new LoginPage(driver, getBaseUrl());
        loginPage.open();
        loginPage.login(getProperty("e2e.engineer.username", "engineer"), getProperty("e2e.engineer.password", "engineer123"));

        TaskFormPage formPage = new TaskFormPage(driver, getBaseUrl());
        formPage.open();
        formPage.submit();

        assertThat(formPage.hasValidationErrors()).isTrue();
    }

    @Test
    @DisplayName("J2-2: Engineer creates valid task and sees it in task list with success feedback")
    void testCreateValidTaskSuccess() {
        LoginPage loginPage = new LoginPage(driver, getBaseUrl());
        loginPage.open();
        loginPage.login(getProperty("e2e.engineer.username", "engineer"), getProperty("e2e.engineer.password", "engineer123"));

        NavBar navBar = new NavBar(driver);
        navBar.clickAddTask();

        String uniqueTitle = "E2E Piling Task - " + System.currentTimeMillis();
        String dueDate = LocalDate.now().plusDays(14).toString();

        TaskFormPage formPage = new TaskFormPage(driver, getBaseUrl());
        formPage.fillForm(uniqueTitle, "IN_PROGRESS", 45, dueDate, "Drilling phase initiated for block C");
        formPage.submit();

        TaskListPage listPage = new TaskListPage(driver, getBaseUrl());
        assertThat(listPage.isSuccessMessageDisplayed()).isTrue();
        assertThat(listPage.isTaskPresent(uniqueTitle)).isTrue();
    }
}
