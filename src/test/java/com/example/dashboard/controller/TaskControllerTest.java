package com.example.dashboard.controller;

import com.example.dashboard.config.WebMvcConfig;
import com.example.dashboard.model.Project;
import com.example.dashboard.model.Task;
import com.example.dashboard.model.TaskStatus;
import com.example.dashboard.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = TaskController.class, includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
        classes = WebMvcConfig.class))
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    private Project sampleProject;
    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleProject = new Project("Metro Station", "Sector 4", LocalDate.now(), LocalDate.now().plusMonths(6));
        sampleProject.setId(1L);

        sampleTask = new Task(sampleProject, "Piling Works", TaskStatus.IN_PROGRESS, 50, LocalDate.now().plusDays(10), "On track");
        sampleTask.setId(10L);
    }

    @Test
    @DisplayName("GET /tasks should return 200, view tasks/list and tasks in model")
    void listTasksShouldReturn200AndListView() throws Exception {
        when(taskService.getAllTasks()).thenReturn(Collections.singletonList(sampleTask));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/list"))
                .andExpect(model().attributeExists("tasks"));

        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    @DisplayName("GET /tasks/new should return 200, view tasks/form and form attributes in model")
    void newTaskFormShouldReturn200AndFormView() throws Exception {
        when(taskService.getAllProjects()).thenReturn(Collections.singletonList(sampleProject));

        mockMvc.perform(get("/tasks/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/form"))
                .andExpect(model().attributeExists("task"))
                .andExpect(model().attributeExists("projects"))
                .andExpect(model().attributeExists("statuses"));

        verify(taskService, times(1)).getAllProjects();
    }

    @Test
    @DisplayName("POST /tasks with valid data should save task and redirect to /tasks")
    void createTaskWithValidDataShouldRedirect() throws Exception {
        when(taskService.createTask(any(Task.class))).thenReturn(sampleTask);

        mockMvc.perform(post("/tasks")
                        .param("title", "Excavation Works")
                        .param("project", "1")
                        .param("status", "IN_PROGRESS")
                        .param("percentComplete", "40")
                        .param("dueDate", LocalDate.now().plusDays(5).toString())
                        .param("remarks", "Excavator on site"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(taskService, times(1)).createTask(any(Task.class));
    }

    @Test
    @DisplayName("POST /tasks with invalid data should return 200, view tasks/form and field errors")
    void createTaskWithInvalidDataShouldReturnFormWithErrors() throws Exception {
        when(taskService.getAllProjects()).thenReturn(Collections.singletonList(sampleProject));

        mockMvc.perform(post("/tasks")
                        .param("title", "")
                        .param("percentComplete", "150"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("task", "title"))
                .andExpect(model().attributeHasFieldErrors("task", "project"))
                .andExpect(model().attributeHasFieldErrors("task", "percentComplete"))
                .andExpect(model().attributeHasFieldErrors("task", "dueDate"));

        verify(taskService, never()).createTask(any(Task.class));
    }

    @Test
    @DisplayName("POST /tasks with percentComplete=100 and status=COMPLETED should redirect (boundary value)")
    void createTaskWithBoundaryPercentCompleteShouldRedirect() throws Exception {
        when(taskService.createTask(any(Task.class))).thenReturn(sampleTask);

        mockMvc.perform(post("/tasks")
                        .param("title", "Final Inspection Sign-off")
                        .param("project", "1")
                        .param("status", "COMPLETED")
                        .param("percentComplete", "100")
                        .param("dueDate", LocalDate.now().toString())
                        .param("remarks", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(taskService, times(1)).createTask(any(Task.class));
    }
}
