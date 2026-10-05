package com.example.dashboard.service;

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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
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
    @DisplayName("createTask should set lastUpdated and persist task")
    void createTaskShouldSetLastUpdatedAndSave() {
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task savedTask = taskService.createTask(sampleTask);

        assertThat(savedTask).isNotNull();
        assertThat(savedTask.getLastUpdated()).isNotNull();
        assertThat(savedTask.getTitle()).isEqualTo("Piling Works");
        verify(taskRepository, times(1)).save(sampleTask);
    }

    @Test
    @DisplayName("getAllTasks should return ordered list from repository")
    void getAllTasksShouldReturnList() {
        when(taskRepository.findAllByOrderByDueDateAsc()).thenReturn(Arrays.asList(sampleTask));

        List<Task> tasks = taskService.getAllTasks();

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Piling Works");
        verify(taskRepository, times(1)).findAllByOrderByDueDateAsc();
    }

    @Test
    @DisplayName("getTaskById should return task when found")
    void getTaskByIdShouldReturnTask() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));

        Optional<Task> result = taskService.getTaskById(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(10L);
        verify(taskRepository, times(1)).findById(10L);
    }

    @Test
    @DisplayName("getAllProjects should return projects list ordered by name")
    void getAllProjectsShouldReturnList() {
        when(projectRepository.findAllByOrderByNameAsc()).thenReturn(Arrays.asList(sampleProject));

        List<Project> projects = taskService.getAllProjects();

        assertThat(projects).hasSize(1);
        assertThat(projects.get(0).getName()).isEqualTo("Metro Station");
        verify(projectRepository, times(1)).findAllByOrderByNameAsc();
    }
}
