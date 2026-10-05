package com.example.dashboard.service;

import com.example.dashboard.model.Project;
import com.example.dashboard.model.Task;
import com.example.dashboard.repository.ProjectRepository;
import com.example.dashboard.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    public Task createTask(Task task) {
        task.setLastUpdated(LocalDateTime.now());
        if (task.getStatus() == TaskStatus.COMPLETED) {
            task.setPercentComplete(100);
        }
        return taskRepository.save(task);
    }

    public Task updateTask(Long id, TaskStatus status, Integer percentComplete, String remarks, String updatedBy) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + id));

        // Auto rule: setting COMPLETED forces percentComplete to 100
        if (status == TaskStatus.COMPLETED) {
            percentComplete = 100;
        } else if (percentComplete != null && percentComplete == 100 && status != TaskStatus.COMPLETED) {
            status = TaskStatus.COMPLETED;
        }

        task.setStatus(status);
        if (percentComplete != null) {
            task.setPercentComplete(percentComplete);
        }
        task.setRemarks(remarks);
        task.setUpdatedBy(updatedBy != null && !updatedBy.trim().isEmpty() ? updatedBy : "system");
        task.setLastUpdated(LocalDateTime.now());

        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return taskRepository.findAllByOrderByDueDateAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        return projectRepository.findAllByOrderByNameAsc();
    }
}
