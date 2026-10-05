package com.example.dashboard.service;

import com.example.dashboard.dto.DashboardFilter;
import com.example.dashboard.dto.DashboardSummary;
import com.example.dashboard.model.Project;
import com.example.dashboard.model.Task;
import com.example.dashboard.model.TaskStatus;
import com.example.dashboard.repository.ProjectRepository;
import com.example.dashboard.repository.TaskRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public DashboardService(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    public DashboardSummary computeSummary() {
        List<Task> allTasks = taskRepository.findAll();
        return computeSummary(allTasks, LocalDate.now());
    }

    public DashboardSummary computeSummary(List<Task> tasks, LocalDate referenceDate) {
        if (tasks == null || tasks.isEmpty()) {
            return new DashboardSummary(0, 0, 0, 0, 0, 0.0);
        }

        long total = tasks.size();
        long completed = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long inProgress = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long blocked = tasks.stream().filter(t -> t.getStatus() == TaskStatus.BLOCKED).count();
        long delayed = tasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getDueDate() != null && t.getDueDate().isBefore(referenceDate))
                .count();

        double avgPercent = tasks.stream()
                .mapToInt(Task::getPercentComplete)
                .average()
                .orElse(0.0);

        return new DashboardSummary(total, completed, inProgress, blocked, delayed, avgPercent);
    }

    public List<Task> search(DashboardFilter filter) {
        if (filter == null || filter.isEmpty()) {
            return taskRepository.findAll(Sort.by(Sort.Direction.ASC, "dueDate"));
        }

        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                String pattern = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate remarksLike = cb.like(cb.lower(root.get("remarks")), pattern);
                predicates.add(cb.or(titleLike, remarksLike));
            }

            if (filter.getProjectId() != null) {
                predicates.add(cb.equal(root.get("project").get("id"), filter.getProjectId()));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getDueDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), filter.getDueDateFrom()));
            }

            if (filter.getDueDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), filter.getDueDateTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return taskRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "dueDate"));
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }
}
