package com.example.dashboard.dto;

import com.example.dashboard.model.TaskStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class DashboardFilter {
    private String keyword;
    private Long projectId;
    private TaskStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDateTo;

    public DashboardFilter() {
    }

    public DashboardFilter(String keyword, Long projectId, TaskStatus status, LocalDate dueDateFrom, LocalDate dueDateTo) {
        this.keyword = keyword;
        this.projectId = projectId;
        this.status = status;
        this.dueDateFrom = dueDateFrom;
        this.dueDateTo = dueDateTo;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDate getDueDateFrom() {
        return dueDateFrom;
    }

    public void setDueDateFrom(LocalDate dueDateFrom) {
        this.dueDateFrom = dueDateFrom;
    }

    public LocalDate getDueDateTo() {
        return dueDateTo;
    }

    public void setDueDateTo(LocalDate dueDateTo) {
        this.dueDateTo = dueDateTo;
    }

    public boolean isEmpty() {
        return (keyword == null || keyword.trim().isEmpty())
                && projectId == null
                && status == null
                && dueDateFrom == null
                && dueDateTo == null;
    }
}
