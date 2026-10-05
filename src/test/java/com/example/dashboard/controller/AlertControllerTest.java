package com.example.dashboard.controller;

import com.example.dashboard.config.WebMvcConfig;
import com.example.dashboard.dto.TaskAlertDto;
import com.example.dashboard.model.Project;
import com.example.dashboard.model.Task;
import com.example.dashboard.model.TaskStatus;
import com.example.dashboard.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AlertController.class, includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = WebMvcConfig.class))
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlertService alertService;

    private Project sampleProject;
    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleProject = new Project("Tower B", "South Hub", LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(6));
        sampleProject.setId(2L);

        sampleTask = new Task(sampleProject, "Steel Framing", TaskStatus.IN_PROGRESS, 30, LocalDate.now().minusDays(4), "Delayed delivery");
        sampleTask.setId(5L);
    }

    @Test
    @DisplayName("GET /alerts returns 200 OK with overdue and blocked alert lists")
    void showAlertsPageWithActiveAlerts() throws Exception {
        TaskAlertDto overdueDto = new TaskAlertDto(sampleTask, 4, "OVERDUE");
        Task blockedTask = new Task(sampleProject, "Facade Glazing", TaskStatus.BLOCKED, 10, LocalDate.now().plusDays(2), "Permit issue");

        when(alertService.getOverdueTasks()).thenReturn(List.of(overdueDto));
        when(alertService.getBlockedTasks()).thenReturn(List.of(blockedTask));

        mockMvc.perform(get("/alerts"))
                .andExpect(status().isOk())
                .andExpect(view().name("alerts"))
                .andExpect(model().attributeExists("overdueTasks", "blockedTasks", "totalAlerts", "activeTab"))
                .andExpect(model().attribute("totalAlerts", 2))
                .andExpect(content().string(containsString("Active Alerts")))
                .andExpect(content().string(containsString("Steel Framing")))
                .andExpect(content().string(containsString("4 days late")))
                .andExpect(content().string(containsString("Facade Glazing")));
    }

    @Test
    @DisplayName("GET /alerts with no active alerts displays empty state banner")
    void showAlertsPageWithNoAlertsDisplaysEmptyState() throws Exception {
        when(alertService.getOverdueTasks()).thenReturn(Collections.emptyList());
        when(alertService.getBlockedTasks()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/alerts"))
                .andExpect(status().isOk())
                .andExpect(view().name("alerts"))
                .andExpect(model().attribute("totalAlerts", 0))
                .andExpect(content().string(containsString("No alerts!")));
    }
}
