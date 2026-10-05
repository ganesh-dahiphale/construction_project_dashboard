package com.example.dashboard;

import com.example.dashboard.model.Project;
import com.example.dashboard.model.Role;
import com.example.dashboard.model.Task;
import com.example.dashboard.model.TaskStatus;
import com.example.dashboard.model.User;
import com.example.dashboard.repository.ProjectRepository;
import com.example.dashboard.repository.TaskRepository;
import com.example.dashboard.repository.UserRepository;
import com.example.dashboard.service.AlertService;
import com.example.dashboard.service.DashboardService;
import com.example.dashboard.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class EdgeCasesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService taskService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private Project sampleProject;

    @BeforeEach
    void setUp() {
        sampleProject = projectRepository.findAll().stream().findFirst().orElseGet(() ->
                projectRepository.save(new Project("Test Project Alpha", "Zone 1", LocalDate.now(), LocalDate.now().plusMonths(3)))
        );
    }

    @Test
    @DisplayName("Edge Case 1: Empty search query in dashboard returns all matching criteria or empty results cleanly")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void emptySearchQueryShouldReturnAllTasksGracefully() throws Exception {
        mockMvc.perform(get("/dashboard")
                        .param("keyword", "")
                        .param("projectId", "")
                        .param("status", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("tasks"))
                .andExpect(model().attributeExists("summary"));

        com.example.dashboard.dto.DashboardFilter emptyFilter = new com.example.dashboard.dto.DashboardFilter();
        emptyFilter.setKeyword("NON_EXISTENT_KEYWORD_XYZ");
        List<Task> searchResults = dashboardService.search(emptyFilter);
        assertThat(searchResults).isEmpty();
    }

    @Test
    @DisplayName("Edge Case 2: Overdue boundary check - task due exactly today is NOT overdue, task due yesterday IS overdue unless completed")
    void overdueBoundaryDateCheck() {
        Task dueToday = new Task(sampleProject, "Inspection Today", TaskStatus.IN_PROGRESS, 50, LocalDate.now(), "On site");
        assertThat(dueToday.isOverdue()).isFalse();

        Task dueYesterday = new Task(sampleProject, "Inspection Yesterday", TaskStatus.IN_PROGRESS, 50, LocalDate.now().minusDays(1), "Delayed");
        assertThat(dueYesterday.isOverdue()).isTrue();

        Task completedPastDue = new Task(sampleProject, "Finished Past Due", TaskStatus.COMPLETED, 100, LocalDate.now().minusDays(5), "Completed");
        assertThat(completedPastDue.isOverdue()).isFalse();
    }

    @Test
    @DisplayName("Edge Case 3: Boundary percentComplete 0% and 100% behavior and status synchronization")
    void percentCompleteBoundaryValues() {
        Task task0 = new Task(sampleProject, "Excavation 0%", TaskStatus.NOT_STARTED, 0, LocalDate.now().plusDays(10), "Initial");
        Task saved0 = taskService.createTask(task0);
        assertThat(saved0.getPercentComplete()).isEqualTo(0);
        assertThat(saved0.getStatus()).isEqualTo(TaskStatus.NOT_STARTED);

        Task task100 = new Task(sampleProject, "Foundation 100%", TaskStatus.COMPLETED, 100, LocalDate.now().plusDays(10), "Done");
        Task saved100 = taskService.createTask(task100);
        assertThat(saved100.getPercentComplete()).isEqualTo(100);
        assertThat(saved100.getStatus()).isEqualTo(TaskStatus.COMPLETED);

        // Update task with COMPLETED automatically coerces progress to 100%
        Task updatedToCompleted = taskService.updateTask(saved0.getId(), TaskStatus.COMPLETED, 30, "Fast-tracked signoff", "admin");
        assertThat(updatedToCompleted.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(updatedToCompleted.getPercentComplete()).isEqualTo(100);
    }

    @Test
    @DisplayName("Edge Case 4: AlertService handles empty or zero alerts without null pointers")
    void alertServiceHandlesZeroAlertsSafely() {
        List<com.example.dashboard.dto.TaskAlertDto> overdueTasks = alertService.getOverdueTasks();
        List<Task> blockedTasks = alertService.getBlockedTasks();
        assertThat(overdueTasks).isNotNull();
        assertThat(blockedTasks).isNotNull();
    }

    @Test
    @DisplayName("Edge Case 5: Unauthorised role access boundaries and public vs protected security")
    void unauthorisedRoleAccessBoundaries() throws Exception {
        // Engineer cannot access /admin/projects or /dashboard
        mockMvc.perform(get("/admin/projects").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("engineer").roles("ENGINEER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("engineer").roles("ENGINEER")))
                .andExpect(status().isForbidden());

        // Manager cannot access /admin/projects
        mockMvc.perform(get("/admin/projects").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("manager").roles("MANAGER")))
                .andExpect(status().isForbidden());

        // Unauthenticated access to /tasks/new redirects to /login
        mockMvc.perform(get("/tasks/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
