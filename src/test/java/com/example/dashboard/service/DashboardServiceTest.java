package com.example.dashboard.service;

import com.example.dashboard.dto.DashboardFilter;
import com.example.dashboard.dto.DashboardSummary;
import com.example.dashboard.model.Project;
import com.example.dashboard.model.Task;
import com.example.dashboard.model.TaskStatus;
import com.example.dashboard.repository.ProjectRepository;
import com.example.dashboard.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private Project project;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.of(2026, 10, 5);
        project = new Project("Hospital Wing A", "North Sector", today.minusMonths(1), today.plusMonths(6));
        project.setId(1L);
    }

    @Test
    @DisplayName("computeSummary with empty list returns zero metrics")
    void computeSummaryWithEmptyListReturnsZeros() {
        DashboardSummary summary = dashboardService.computeSummary(Collections.emptyList(), today);

        assertThat(summary.getTotalTasks()).isEqualTo(0);
        assertThat(summary.getCompletedTasks()).isEqualTo(0);
        assertThat(summary.getInProgressTasks()).isEqualTo(0);
        assertThat(summary.getBlockedTasks()).isEqualTo(0);
        assertThat(summary.getDelayedTasks()).isEqualTo(0);
        assertThat(summary.getOverallCompletionPercentage()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("computeSummary computes accurate KPI counts and average progress percentage")
    void computeSummaryCalculatesAccurateMetrics() {
        Task t1 = new Task(project, "Piling", TaskStatus.COMPLETED, 100, today.minusDays(5), "Done");
        Task t2 = new Task(project, "Concrete", TaskStatus.IN_PROGRESS, 50, today.plusDays(10), "Pouring");
        Task t3 = new Task(project, "Electrical", TaskStatus.BLOCKED, 20, today.plusDays(5), "Awaiting permit");
        Task t4 = new Task(project, "Plumbing", TaskStatus.NOT_STARTED, 0, today.minusDays(2), "Delayed start");

        List<Task> tasks = Arrays.asList(t1, t2, t3, t4);
        DashboardSummary summary = dashboardService.computeSummary(tasks, today);

        assertThat(summary.getTotalTasks()).isEqualTo(4);
        assertThat(summary.getCompletedTasks()).isEqualTo(1);
        assertThat(summary.getInProgressTasks()).isEqualTo(1);
        assertThat(summary.getBlockedTasks()).isEqualTo(1);
        // t4 is NOT_STARTED with dueDate in the past (minusDays(2)) -> DELAYED. t1 is past due but COMPLETED -> not delayed.
        assertThat(summary.getDelayedTasks()).isEqualTo(1);
        // Average: (100 + 50 + 20 + 0) / 4 = 42.5
        assertThat(summary.getOverallCompletionPercentage()).isEqualTo(42.5);
    }

    @Test
    @DisplayName("Delayed task rule: Completed task with past dueDate is NOT counted as delayed")
    void completedTaskPastDueDateIsNotDelayed() {
        Task completedPastDue = new Task(project, "Inspection", TaskStatus.COMPLETED, 100, today.minusDays(10), "Approved");
        Task inProgressPastDue = new Task(project, "Roofing", TaskStatus.IN_PROGRESS, 60, today.minusDays(3), "Rain delay");

        List<Task> tasks = Arrays.asList(completedPastDue, inProgressPastDue);
        DashboardSummary summary = dashboardService.computeSummary(tasks, today);

        assertThat(summary.getTotalTasks()).isEqualTo(2);
        assertThat(summary.getDelayedTasks()).isEqualTo(1); // Only inProgressPastDue
    }

    @Test
    @DisplayName("search with empty filter returns all tasks ordered by dueDate")
    void searchWithEmptyFilterReturnsAll() {
        Task task = new Task(project, "Site Survey", TaskStatus.COMPLETED, 100, today, "Done");
        when(taskRepository.findAll(any(Sort.class))).thenReturn(List.of(task));

        List<Task> results = dashboardService.search(new DashboardFilter());

        assertThat(results).hasSize(1);
        verify(taskRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("search with filter uses specification query")
    @SuppressWarnings("unchecked")
    void searchWithFilterUsesSpecification() {
        DashboardFilter filter = new DashboardFilter("survey", 1L, TaskStatus.IN_PROGRESS, today, today.plusDays(7));
        Task task = new Task(project, "Site Survey", TaskStatus.IN_PROGRESS, 40, today, "Active");
        when(taskRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(task));

        List<Task> results = dashboardService.search(filter);

        assertThat(results).hasSize(1);
        verify(taskRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }
}
