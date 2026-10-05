package com.example.dashboard.controller;

import com.example.dashboard.config.WebMvcConfig;
import com.example.dashboard.model.Project;
import com.example.dashboard.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = AdminProjectController.class, includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
        classes = WebMvcConfig.class))
class AdminProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    private Project sampleProject;

    @BeforeEach
    void setUp() {
        sampleProject = new Project("Metro Hub", "Central Station", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        sampleProject.setId(1L);
    }

    @Test
    @DisplayName("GET /admin/projects should return 200, view admin/projects/list and projects in model")
    void listProjectsShouldReturn200() throws Exception {
        when(projectService.getAllProjects()).thenReturn(Collections.singletonList(sampleProject));

        mockMvc.perform(get("/admin/projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/projects/list"))
                .andExpect(model().attributeExists("projects"));

        verify(projectService, times(1)).getAllProjects();
    }

    @Test
    @DisplayName("GET /admin/projects/new should return 200, view admin/projects/form")
    void newProjectFormShouldReturn200() throws Exception {
        mockMvc.perform(get("/admin/projects/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/projects/form"))
                .andExpect(model().attributeExists("project"));
    }

    @Test
    @DisplayName("POST /admin/projects with valid data should create project and redirect")
    void createProjectValidShouldRedirect() throws Exception {
        when(projectService.createProject(any(Project.class))).thenReturn(sampleProject);

        mockMvc.perform(post("/admin/projects")
                        .param("name", "Solar Farm")
                        .param("location", "Sector 9")
                        .param("startDate", "2026-03-01")
                        .param("endDate", "2026-11-30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/projects"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(projectService, times(1)).createProject(any(Project.class));
    }

    @Test
    @DisplayName("POST /admin/projects with invalid data should return form view with errors")
    void createProjectInvalidShouldReturnForm() throws Exception {
        mockMvc.perform(post("/admin/projects")
                        .param("name", "")
                        .param("location", "Sector 9"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/projects/form"))
                .andExpect(model().hasErrors());

        verify(projectService, never()).createProject(any(Project.class));
    }

    @Test
    @DisplayName("GET /admin/projects/{id}/edit should return 200 and populate form")
    void editProjectFormShouldReturn200() throws Exception {
        when(projectService.getProjectById(1L)).thenReturn(Optional.of(sampleProject));

        mockMvc.perform(get("/admin/projects/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/projects/form"))
                .andExpect(model().attributeExists("project"));

        verify(projectService, times(1)).getProjectById(1L);
    }

    @Test
    @DisplayName("GET /admin/projects/{id}/edit for non-existent project should return 404")
    void editProjectNotFoundShouldReturn404() throws Exception {
        when(projectService.getProjectById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/projects/999/edit"))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).getProjectById(999L);
    }

    @Test
    @DisplayName("POST /admin/projects/{id}/edit with valid data should update and redirect")
    void updateProjectValidShouldRedirect() throws Exception {
        when(projectService.updateProject(eq(1L), any(Project.class))).thenReturn(sampleProject);

        mockMvc.perform(post("/admin/projects/1/edit")
                        .param("name", "Updated Metro Hub")
                        .param("location", "Central Station 2")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/projects"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(projectService, times(1)).updateProject(eq(1L), any(Project.class));
    }
}
