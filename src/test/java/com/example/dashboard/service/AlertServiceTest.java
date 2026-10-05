package com.example.dashboard.service;

import com.example.dashboard.dto.TaskAlertDto;
import com.example.dashboard.model.Project;
import com.example.dashboard.model.Task;
import com.example.dashboard.model.TaskStatus;
import com.example.dashboard.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private AlertService alertService;

    private Project project;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.of(2026, 10, 5);
        project = new Project("Commercial Plaza", "Sector 4", today.minusMonths(2), today.plusMonths(4));
        project.setId(1L);
    }

    @Test
    @DisplayName("getOverdueTasks returns only uncompleted tasks with past dueDate and calculates daysOverdue")
    void getOverdueTasksCalculatesDaysOverdueAccurately() {
        Task overdueTask1 = new Task(project, "Piling", TaskStatus.IN_PROGRESS, 50, today.minusDays(5), "Late");
        Task overdueTask2 = new Task(project, "Excavation", TaskStatus.NOT_STARTED, 0, today.minusDays(10), "Delayed");
        Task completedPastDue = new Task(project, "Survey", TaskStatus.COMPLETED, 100, today.minusDays(7), "Finished");
        Task futureTask = new Task(project, "Roofing", TaskStatus.NOT_STARTED, 0, today.plusDays(5), "Upcoming");

        when(taskRepository.findAll()).thenReturn(Arrays.asList(overdueTask1, overdueTask2, completedPastDue, futureTask));

        List<TaskAlertDto> overdueList = alertService.getOverdueTasks(today);

        assertThat(overdueList).hasSize(2);
        // Sorted by dueDate ascending (oldest/most overdue first: minusDays(10) then minusDays(5))
        assertThat(overdueList.get(0).getTask().getTitle()).isEqualTo("Excavation");
        assertThat(overdueList.get(0).getDaysOverdue()).isEqualTo(10);
        assertThat(overdueList.get(1).getTask().getTitle()).isEqualTo("Piling");
        assertThat(overdueList.get(1).getDaysOverdue()).isEqualTo(5);
    }

    @Test
    @DisplayName("getBlockedTasks returns only tasks with BLOCKED status")
    void getBlockedTasksReturnsBlockedOnly() {
        Task blockedTask = new Task(project, "Electrical Permit", TaskStatus.BLOCKED, 20, today.plusDays(2), "Permit pending");
        Task inProgressTask = new Task(project, "Masonry", TaskStatus.IN_PROGRESS, 40, today.plusDays(3), "Working");

        when(taskRepository.findAll()).thenReturn(Arrays.asList(blockedTask, inProgressTask));

        List<Task> blocked = alertService.getBlockedTasks();

        assertThat(blocked).hasSize(1);
        assertThat(blocked.get(0).getTitle()).isEqualTo("Electrical Permit");
        assertThat(blocked.get(0).getStatus()).isEqualTo(TaskStatus.BLOCKED);
    }

    @Test
    @DisplayName("getTasksByStatus filters tasks strictly by given TaskStatus")
    void getTasksByStatusFiltersCorrectly() {
        Task t1 = new Task(project, "Task 1", TaskStatus.IN_PROGRESS, 50, today, "");
        Task t2 = new Task(project, "Task 2", TaskStatus.COMPLETED, 100, today, "");
        Task t3 = new Task(project, "Task 3", TaskStatus.IN_PROGRESS, 30, today, "");

        when(taskRepository.findAll()).thenReturn(Arrays.asList(t1, t2, t3));

        List<Task> inProgress = alertService.getTasksByStatus(TaskStatus.IN_PROGRESS);
        List<Task> completed = alertService.getTasksByStatus(TaskStatus.COMPLETED);

        assertThat(inProgress).hasSize(2);
        assertThat(completed).hasSize(1);
    }

    @Test
    @DisplayName("getDelayedTasks returns uncompleted tasks with past dueDate")
    void getDelayedTasksFiltersAccurately() {
        Task delayed = new Task(project, "Delayed Task", TaskStatus.NOT_STARTED, 0, today.minusDays(3), "");
        Task completed = new Task(project, "Completed Past Due", TaskStatus.COMPLETED, 100, today.minusDays(4), "");

        when(taskRepository.findAll()).thenReturn(Arrays.asList(delayed, completed));

        List<Task> delayedTasks = alertService.getDelayedTasks(today);

        assertThat(delayedTasks).hasSize(1);
        assertThat(delayedTasks.get(0).getTitle()).isEqualTo("Delayed Task");
    }
}
