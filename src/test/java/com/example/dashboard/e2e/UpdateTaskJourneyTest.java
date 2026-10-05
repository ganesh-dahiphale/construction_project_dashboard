package com.example.dashboard.e2e;

import com.example.dashboard.e2e.pages.LoginPage;
import com.example.dashboard.e2e.pages.NavBar;
import com.example.dashboard.e2e.pages.TaskEditPage;
import com.example.dashboard.e2e.pages.TaskFormPage;
import com.example.dashboard.e2e.pages.TaskListPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
public class UpdateTaskJourneyTest extends BaseE2ETest {

    @Test
    @DisplayName("J3: Engineer updates task to COMPLETED; list shows COMPLETED and 100% progress")
    void testUpdateTaskStatusToCompleted() {
        LoginPage loginPage = new LoginPage(driver, getBaseUrl());
        loginPage.open();
        loginPage.login(getProperty("e2e.engineer.username", "engineer"), getProperty("e2e.engineer.password", "engineer123"));

        // 1. Create a fresh task to update
        NavBar navBar = new NavBar(driver);
        navBar.clickAddTask();

        String uniqueTitle = "E2E Task to Complete - " + System.currentTimeMillis();
        String dueDate = LocalDate.now().plusDays(7).toString();

        TaskFormPage formPage = new TaskFormPage(driver, getBaseUrl());
        formPage.fillForm(uniqueTitle, "IN_PROGRESS", 50, dueDate, "Midway inspection pending");
        formPage.submit();

        TaskListPage listPage = new TaskListPage(driver, getBaseUrl());
        assertThat(listPage.isTaskPresent(uniqueTitle)).isTrue();

        // 2. Click edit on the created task
        listPage.clickEditTask(uniqueTitle);

        // 3. Update status to COMPLETED
        TaskEditPage editPage = new TaskEditPage(driver);
        editPage.updateStatus("COMPLETED");
        editPage.updateRemarks("Final sign-off completed by certified site engineer");
        editPage.submit();

        // 4. Verify in task list
        assertThat(listPage.isSuccessMessageDisplayed()).isTrue();
        assertThat(listPage.isTaskPresent(uniqueTitle)).isTrue();
        assertThat(listPage.getTaskStatus(uniqueTitle)).containsIgnoringCase("Completed");
        assertThat(listPage.getTaskProgress(uniqueTitle)).contains("100%");
    }
}
