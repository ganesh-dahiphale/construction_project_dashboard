package com.example.dashboard.service;

import com.example.dashboard.model.Project;
import com.example.dashboard.repository.ProjectRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project sampleProject;

    @BeforeEach
    void setUp() {
        sampleProject = new Project("High-Rise Tower A", "Downtown", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        sampleProject.setId(1L);
    }

    @Test
    @DisplayName("getAllProjects should return list sorted by name")
    void getAllProjectsShouldReturnList() {
        when(projectRepository.findAllByOrderByNameAsc()).thenReturn(Arrays.asList(sampleProject));

        List<Project> projects = projectService.getAllProjects();

        assertThat(projects).hasSize(1);
        assertThat(projects.get(0).getName()).isEqualTo("High-Rise Tower A");
        verify(projectRepository, times(1)).findAllByOrderByNameAsc();
    }

    @Test
    @DisplayName("getProjectById should return project when exists")
    void getProjectByIdShouldReturnProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        Optional<Project> result = projectService.getProjectById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("High-Rise Tower A");
        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("createProject should save and return project")
    void createProjectShouldSaveAndReturn() {
        when(projectRepository.save(any(Project.class))).thenReturn(sampleProject);

        Project created = projectService.createProject(sampleProject);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(1L);
        verify(projectRepository, times(1)).save(sampleProject);
    }

    @Test
    @DisplayName("updateProject should update fields and save")
    void updateProjectShouldUpdateFields() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project updatedData = new Project("High-Rise Tower A (Phase 2)", "Downtown Sector 5", LocalDate.of(2026, 2, 1), LocalDate.of(2027, 2, 1));
        Project result = projectService.updateProject(1L, updatedData);

        assertThat(result.getName()).isEqualTo("High-Rise Tower A (Phase 2)");
        assertThat(result.getLocation()).isEqualTo("Downtown Sector 5");
        verify(projectRepository, times(1)).save(sampleProject);
    }

    @Test
    @DisplayName("updateProject for non-existent project throws IllegalArgumentException")
    void updateProjectNotFoundThrowsException() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        Project updatedData = new Project("New Name", "New Loc", LocalDate.now(), LocalDate.now().plusMonths(1));

        assertThrows(IllegalArgumentException.class, () -> projectService.updateProject(99L, updatedData));
    }
}
