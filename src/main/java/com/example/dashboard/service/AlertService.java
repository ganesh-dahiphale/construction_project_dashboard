package com.example.dashboard.service;

import com.example.dashboard.dto.TaskAlertDto;
import com.example.dashboard.model.Task;
import com.example.dashboard.model.TaskStatus;
import com.example.dashboard.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AlertService {

    private final TaskRepository taskRepository;

    public AlertService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskAlertDto> getOverdueTasks() {
        return getOverdueTasks(LocalDate.now());
    }

    public List<TaskAlertDto> getOverdueTasks(LocalDate referenceDate) {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED
                        && t.getDueDate() != null
                        && t.getDueDate().isBefore(referenceDate))
                .sorted(Comparator.comparing(Task::getDueDate))
                .map(t -> new TaskAlertDto(
                        t,
                        ChronoUnit.DAYS.between(t.getDueDate(), referenceDate),
                        "OVERDUE"))
                .collect(Collectors.toList());
    }

    public List<Task> getBlockedTasks() {
        return taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.BLOCKED)
                .sorted(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == status)
                .sorted(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public List<Task> getDelayedTasks() {
        return getDelayedTasks(LocalDate.now());
    }

    public List<Task> getDelayedTasks(LocalDate referenceDate) {
        return taskRepository.findAll().stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED
                        && t.getDueDate() != null
                        && t.getDueDate().isBefore(referenceDate))
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());
    }
}
