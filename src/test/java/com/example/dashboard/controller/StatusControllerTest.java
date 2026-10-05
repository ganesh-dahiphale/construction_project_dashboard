package com.example.dashboard.controller;

import com.example.dashboard.config.WebMvcConfig;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StatusController.class, includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = WebMvcConfig.class))
class StatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlertService alertService;

    private Project sampleProject;
    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleProject = new Project("Site Alpha", "Sector 1", LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(3));
        sampleProject.setId(1L);

        sampleTask = new Task(sampleProject, "Excavation", TaskStatus.IN_PROGRESS, 45, LocalDate.now().plusDays(5), "On track");
        sampleTask.setId(1L);
    }

    @Test
    @DisplayName("GET /status returns 200 OK with status index view and category counts")
    void showStatusIndexReturnsOk() throws Exception {
        when(alertService.getTasksByStatus(TaskStatus.NOT_STARTED)).thenReturn(List.of());
        when(alertService.getTasksByStatus(TaskStatus.IN_PROGRESS)).thenReturn(List.of(sampleTask));
        when(alertService.getTasksByStatus(TaskStatus.COMPLETED)).thenReturn(List.of());
        when(alertService.getTasksByStatus(TaskStatus.BLOCKED)).thenReturn(List.of());
        when(alertService.getDelayedTasks()).thenReturn(List.of());

        mockMvc.perform(get("/status"))
                .andExpect(status().isOk())
                .andExpect(view().name("status/index"))
                .andExpect(model().attributeExists("notStartedCount", "inProgressCount", "completedCount", "blockedCount", "delayedCount", "activeTab"))
                .andExpect(model().attribute("inProgressCount", 1))
                .andExpect(content().string(containsString("Status Drill-Down Categories")));
    }

    @Test
    @DisplayName("GET /status/IN_PROGRESS returns 200 OK with drilldown view for in-progress tasks")
    void showStatusDrilldownForInProgressReturnsMatchingTasks() throws Exception {
        when(alertService.getTasksByStatus(TaskStatus.IN_PROGRESS)).thenReturn(List.of(sampleTask));

        mockMvc.perform(get("/status/IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(view().name("status/drilldown"))
                .andExpect(model().attributeExists("tasks", "heading", "count", "activeTab"))
                .andExpect(model().attribute("count", 1))
                .andExpect(content().string(containsString("In Progress Tasks")))
                .andExpect(content().string(containsString("Excavation")));
    }

    @Test
    @DisplayName("GET /status/delayed returns 200 OK with delayed tasks drilldown")
    void showStatusDrilldownForDelayedReturnsDelayedTasks() throws Exception {
        Task delayedTask = new Task(sampleProject, "Roof Truss", TaskStatus.NOT_STARTED, 0, LocalDate.now().minusDays(3), "Material delay");
        when(alertService.getDelayedTasks()).thenReturn(List.of(delayedTask));

        mockMvc.perform(get("/status/delayed"))
                .andExpect(status().isOk())
                .andExpect(view().name("status/drilldown"))
                .andExpect(model().attribute("count", 1))
                .andExpect(content().string(containsString("Delayed Tasks")))
                .andExpect(content().string(containsString("Roof Truss")));
    }
}
