package com.example.dashboard.controller;

import com.example.dashboard.config.WebMvcConfig;
import com.example.dashboard.dto.DashboardFilter;
import com.example.dashboard.dto.DashboardSummary;
import com.example.dashboard.model.Project;
import com.example.dashboard.model.Task;
import com.example.dashboard.model.TaskStatus;
import com.example.dashboard.service.DashboardService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DashboardController.class, includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = WebMvcConfig.class))
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    private Project sampleProject;
    private Task sampleTask;
    private DashboardSummary sampleSummary;

    @BeforeEach
    void setUp() {
        sampleProject = new Project("Metro Station Alpha", "Downtown", LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(6));
        sampleProject.setId(1L);

        sampleTask = new Task(sampleProject, "Foundation Excavation", TaskStatus.IN_PROGRESS, 60, LocalDate.now().plusDays(10), "On track");
        sampleTask.setId(10L);

        sampleSummary = new DashboardSummary(5, 2, 2, 1, 1, 65.0);

        when(dashboardService.computeSummary()).thenReturn(sampleSummary);
        when(dashboardService.getAllProjects()).thenReturn(List.of(sampleProject));
    }

    @Test
    @DisplayName("GET /dashboard returns 200 OK, dashboard view, and summary KPI attributes")
    void showDashboardReturnsOkAndPopulatesModel() throws Exception {
        when(dashboardService.search(any(DashboardFilter.class))).thenReturn(List.of(sampleTask));

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("summary", "tasks", "projects", "statuses", "today", "activeTab"))
                .andExpect(model().attribute("activeTab", "dashboard"))
                .andExpect(model().attribute("tasks", hasSize(1)))
                .andExpect(model().attribute("summary", hasProperty("totalTasks", is(5L))))
                .andExpect(model().attribute("summary", hasProperty("overallCompletionPercentage", is(65.0))))
                .andExpect(content().string(containsString("Executive Progress Dashboard")))
                .andExpect(content().string(containsString("Foundation Excavation")));
    }

    @Test
    @DisplayName("GET /dashboard with query params applies filter and renders matching results")
    void showDashboardWithFilterParamsReturnsFilteredTasks() throws Exception {
        when(dashboardService.search(any(DashboardFilter.class))).thenReturn(List.of(sampleTask));

        mockMvc.perform(get("/dashboard")
                        .param("keyword", "Excavation")
                        .param("projectId", "1")
                        .param("status", "IN_PROGRESS")
                        .param("dueDateFrom", LocalDate.now().toString())
                        .param("dueDateTo", LocalDate.now().plusDays(30).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("tasks", "summary"))
                .andExpect(model().attribute("tasks", hasSize(1)));
    }

    @Test
    @DisplayName("GET /dashboard with empty task list displays empty state message")
    void showDashboardWithNoTasksDisplaysEmptyState() throws Exception {
        when(dashboardService.search(any(DashboardFilter.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content().string(containsString("No matching tasks found")));
    }
}
